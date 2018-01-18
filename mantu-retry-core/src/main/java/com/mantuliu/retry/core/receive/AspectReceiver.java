package com.mantuliu.retry.core.receive;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mantuliu.retry.core.call.DefaultProcessorServiceImpl;
import com.mantuliu.retry.core.call.PersistenceService;
import com.mantuliu.retry.core.call.schedule.DefaultScheduleServiceImpl;
import com.mantuliu.retry.core.common.Commons;
import com.mantuliu.retry.core.common.ResponseEnum;
import com.mantuliu.retry.core.common.RetryIntercept;
import com.mantuliu.retry.core.common.RetryRepeatException;
import com.mantuliu.retry.core.common.RetryStrategy;
import com.mantuliu.retry.core.common.ServiceSpi;
import com.mantuliu.retry.core.common.SpringCommons;
import com.mantuliu.retry.core.entity.RetryDTO;
import com.mantuliu.retry.core.entity.RetryParameter;
import com.mantuliu.retry.core.entity.RetryStrategyDTO;
import com.mantuliu.retry.core.entity.RetryUnion;
import com.mantuliu.retry.core.util.ReflectionRetryUtils;

@Aspect
public class AspectReceiver implements ApplicationContextAware,InitializingBean{

	private static Logger logger = LoggerFactory.getLogger(AspectReceiver.class);
	private static ApplicationContext context;
	private Integer maxRetrySize;//系统支持的最大重试任务数量
	private String serviceName;//服务名称
	
	public Integer getMaxRetrySize() {
		return maxRetrySize;
	}

	public void setMaxRetrySize(Integer maxRetrySize) {
		this.maxRetrySize = maxRetrySize;
	}

	public void afterPropertiesSet() throws Exception {
		if(Objects.nonNull(this.getMaxRetrySize()) && this.getMaxRetrySize()>0) {
            Commons.MAX_RETRY_SIZE=this.getMaxRetrySize();
        }
		ServiceSpi.processorService = new DefaultProcessorServiceImpl();
        ServiceSpi.scheduleService = new DefaultScheduleServiceImpl();
        try{
	        Object persistenceService = SpringCommons.getBean(PersistenceService.class);
	        if(Objects.nonNull(persistenceService)){
	        	ServiceSpi.persistenceService = (PersistenceService) persistenceService;
	        }
        }catch(Exception ex){
        	logger.info("no PersistenceService object.");
        }
        new Thread(new Runnable() {
        	public void run(){
        		ServiceSpi.persistenceService.rebuildRetryTask();
        	}
        },"thread-retryrebuild").start();
        
	}

	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		SpringCommons.setContext(arg0);
	}

    @Pointcut(value = "@annotation(com.mantuliu.retry.core.common.RetryIntercept)")
    private void pointcut() {
    }
    
	@AfterThrowing(value = "pointcut() && @annotation(retry)" , throwing = "ex")
	public void catchRetry(JoinPoint jp,RetryIntercept retry,Exception ex){
		Class<? extends Throwable>[] exClasses = retry.retryFor();
		for(Class classObj : exClasses){
			Class[] a2 = {classObj};
			Class[] a1 = {ex.getClass()};
			if(ReflectionRetryUtils.equalsClass(a1, a2)){
				makeRetry(jp,retry);
			}
		}
	}
	
	private void makeRetry(JoinPoint jp,RetryIntercept retryMantu){
		List<Object> objs = java.util.Arrays.asList(jp.getArgs());
		ArrayList<Object> objList = new ArrayList(objs);
		RetryDTO retry = new RetryDTO();
        RetryStrategy[] retryStrategy = retryMantu.retryStrategy();
        RetryStrategyDTO[] retryTypes = new RetryStrategyDTO[retryStrategy.length];
        for(int i =0 ; i<retryStrategy.length; i++){
        	RetryStrategy item = retryStrategy[i];
        	RetryStrategyDTO retryType = new RetryStrategyDTO();
        	retryType.setCounts(item.counts());
        	retryType.setInterval(item.interval());
        	retryType.setUnit(item.unit());
        	retryTypes[i] = retryType;
        }
        retry.setClassName(jp.getSignature().getDeclaringTypeName());
        retry.setMethodName(jp.getSignature().getName());
        retry.setParameters(objList);
        receive(retry,retryTypes);
	}
	
    public void receive(RetryDTO retry,RetryStrategyDTO[] retryTypes) {
        logger.debug("receive retry parameter.RetryDTO is : {},RetryStrategyDTO is : {}",JSON.toJSONString(retry),JSON.toJSON(retryTypes));
        String threadName =Thread.currentThread().getName();
        if(threadName.startsWith("retryproccesspool")) {
            throw new RetryRepeatException("this retry from retry-frame");
        }
        
        ResponseEnum checkEnum = checkLegal(retry,retryTypes);
        if(!checkEnum.equals(ResponseEnum.SUCCESS)) {
        	logger.warn("receive retry parmeter check fails.response is : {},RetryDTO is : {},RetryStrategyDTO is : {}",checkEnum,JSON.toJSONString(retry),JSON.toJSON(retryTypes));
            return ;
        }
        if(retry.getParas()==null || retry.getParas().size()<retry.getParameters().size()){
        	ArrayList<RetryParameter> paras = new ArrayList<RetryParameter>();
        	retry.setParas(paras);
        }
        List<RetryParameter> paras = retry.getParas();
        paras.clear();
        List<Object> objs= retry.getParameters();
        for(Object obj : objs){
        	RetryParameter para = new RetryParameter();
        	para.setClassname(obj.getClass().getName());
        	para.setValue(JSONObject.toJSONString(obj));
        	paras.add(para);
        }

        retry.setServiceName(this.getServiceName());
        RetryUnion retryUnion = new RetryUnion();
        retryUnion.setExecutedTime(new ArrayList<Long>());
        retryUnion.setRetryDTO(retry);
        retryUnion.setRetryTypes(retryTypes);
        retryUnion.setRetryKey(Commons.RETRY_KEY+this.getServiceName()+":"+UUID.randomUUID().toString().replaceAll("-", ""));
        ServiceSpi.persistenceService.insert(retryUnion);
        ServiceSpi.scheduleService.scheduleRetry(retryUnion);
        return ;
    }
    
    private ResponseEnum checkLegal(RetryDTO retry,RetryStrategyDTO[] retryTypes) {
        if(Objects.isNull(retryTypes)|| retryTypes.length<1) {
            logger.info("retry have not retry type . RetryDTO is : {},RetryStrategyDTO is : {}",JSON.toJSONString(retry),JSON.toJSON(retryTypes));
            return ResponseEnum.RetryStatyge_IS_NULL;
        }
        for(RetryStrategyDTO item : retryTypes) {
            if(item.getCounts()<=0) {
                logger.info("register retry have not retry type . parameter retryCounts is :{} retrydto is : {}",item.getCounts(),retry.toString());
                return ResponseEnum.RetryStatyge_NUMS_ZERO;
            }
            if(item.getInterval()<0) {
            	logger.info("register retry have not retry type . parameter retryInterval is :{} retrydto is : {}",item.getInterval(),retry.toString());
                return ResponseEnum.RetryStatyge_NUMS_ZERO;
            }
        }
        if(StringUtils.isEmpty(retry.getClassName())) {
            logger.info("register retry parameter:{},the wrong is because classname is empty ",retry.toString());
            return ResponseEnum.SPRING_CALSS_NOT_DEFINE;
        }else if(StringUtils.isEmpty(retry.getMethodName())) {
            logger.info("register retry parameter:{},the wrong is because methodname is empty ",retry.toString());
            return ResponseEnum.SPRING_METHOD_NOT_DEFINE;
        }
        Class<?> className = null;
        try {
            className = Class.forName(retry.getClassName());
        } catch (ClassNotFoundException e) {
            logger.info("register retry parameter:{},the wrong is because class not found ",retry.toString());
            return ResponseEnum.SPRING_CALSS_NOT_DEFINE;
        }
        ResponseEnum checkEnum = checkParameters(retry.getParameters());
        if(!checkEnum.equals(ResponseEnum.SUCCESS)) {
            return checkEnum;
        }
        Object obj = SpringCommons.getBean(className);
        if(obj==null) {
            logger.info("register retry parameter:{},the wrong is because spring don't have the object ",retry.toString());
            return ResponseEnum.SPRING_CALSS_NOT_DEFINE;
        }
        try {
            List<Object> objs = retry.getParameters();
            Class[] classes = new Class[objs.size()];
            for(int i =0;i<objs.size();i++) {
                classes[i] = objs.get(i).getClass();
            }
            Method method = ReflectionRetryUtils.findMethod(className, retry.getMethodName(), classes);
            if(method == null) {
                logger.info("register retry parameter:{},the wrong is because the object don't have the method ",retry.toString());
                return ResponseEnum.SPRING_METHOD_NOT_DEFINE;
            }
        } catch (SecurityException e) {
            logger.info("register retry parameter:{},the wrong is because don't have the method permission ",retry.toString());
            return ResponseEnum.SPRING_METHOD_NOT_DEFINE;
        }
        return ResponseEnum.SUCCESS;
    }

    private ResponseEnum checkParameters(List<Object> params) {
        if(params == null || params.size()==0) {
            return ResponseEnum.SPRING_PARAMETER_IS_NULL;
        }
        for(Object obj : params) {
            if(!(obj instanceof Serializable)) {
                return ResponseEnum.PARAMETERS_HASNO_SERIALIZABLE;
            }
        }
        return ResponseEnum.SUCCESS;
    }

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
		Commons.SERVICENAME = serviceName;
	}
}
