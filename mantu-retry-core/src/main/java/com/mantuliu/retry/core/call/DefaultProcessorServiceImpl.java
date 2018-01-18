package com.mantuliu.retry.core.call;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.mantuliu.retry.core.common.Commons;
import com.mantuliu.retry.core.common.RetryRepeatException;
import com.mantuliu.retry.core.common.ServiceSpi;
import com.mantuliu.retry.core.common.SpringCommons;
import com.mantuliu.retry.core.entity.RetryDTO;
import com.mantuliu.retry.core.entity.RetryParameter;
import com.mantuliu.retry.core.entity.RetryStrategyDTO;
import com.mantuliu.retry.core.entity.RetryUnion;
import com.mantuliu.retry.core.util.ReflectionRetryUtils;
import com.mantuliu.retry.core.util.RetryProcessTaskFactory;


public class DefaultProcessorServiceImpl implements ProcessorService{

    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessorServiceImpl.class);
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(Commons.PROCESS_POOL_SIZE, Commons.PROCESS_POOL_SIZE, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue(Commons.MAX_RETRY_SIZE),new RetryProcessTaskFactory(Commons.PROCCESS_POOL_NAME));

    @Override
    public void process(RetryUnion retryUnion) {
        executor.execute(new ProcessTask(retryUnion));
    }
    
    private class ProcessTask implements Runnable{
        private RetryUnion retryUnion;
        private ProcessTask(RetryUnion retryUnion) {
            this.retryUnion=retryUnion;
        }
        
        @Override
        public void run() {
            RetryDTO retry = null;
            RetryStrategyDTO[] retryTypes = null;
            RetryStrategyDTO retryItem = null;
            int hasRetryTimes = 0;
            try {
                retry = retryUnion.getRetryDTO();
                retryTypes = retryUnion.getRetryTypes();
                int currentType = retryUnion.getCurrentRetryType();
                if(retryTypes != null && retryTypes.length>currentType) {
                    retryItem = retryTypes[currentType];
                    if(retryItem.getHasRetryCounts().getAndIncrement() >= retryItem.getCounts()) {
                        logger.warn("retrytype's retry times is overflow {}",retry);
                        return;
                    }
                    for(int i=0;i<=currentType;i++) {
                    	RetryStrategyDTO item = retryTypes[i];
                        hasRetryTimes =hasRetryTimes+item.getHasRetryCounts().get();
                    }
                }else {
                    logger.warn("retryTypes is null or retryTypes's length is overflow {}",retry);
                    return;
                }
                Class<?> className = null;
                Method method = null;
                Object obj = null;
                className = Class.forName(retry.getClassName());
                List<Object> objs = retry.getParameters();
                objs.clear();
                List<RetryParameter> params = retry.getParas();
                Class[] classes = new Class[params.size()];
                for(int i =0;i<params.size();i++) {
                    classes[i] = Class.forName(params.get(i).getClassname());
                    objs.add(JSONObject.parseObject(params.get(i).getValue(), Class.forName(params.get(i).getClassname())));
                }
                method = ReflectionRetryUtils.findMethod(className, retry.getMethodName(), classes);
                if(method == null) {
                    logger.info("register retry parameter:{},the fails is because the object don't have the method ",retry.toString());
                    throw new NoSuchMethodException("no such method");
                }
                obj = SpringCommons.getBean(className);
                method.invoke(obj, retry.getParameters().toArray());
                logger.info("ProcessTask execute succeed {} {}",retry.toString(),Thread.currentThread().getName());
                retryUnion.getExecutedTime().add(System.currentTimeMillis());
                
                ServiceSpi.persistenceService.executeSuccess(retryUnion);
            }catch(RetryRepeatException rre) {
                retryUnion.getExecutedTime().add(System.currentTimeMillis());
                ServiceSpi.persistenceService.insert(retryUnion);
                logger.info("previous call is failure throws RetryRepeatException {} have retry {} times ",retry.toString(),retryItem.getHasRetryCounts());
                ServiceSpi.scheduleService.scheduleRetry(retryUnion);
            }catch(Throwable ex) {
                Throwable cause = ex.getCause();
                if(cause!=null && cause.getClass().equals(RetryRepeatException.class)) {
                    logger.info("previous call is failure throws RetryRepeatException {} have retry {} times ",retry.toString(),retryItem.getHasRetryCounts());
                }
                else {
                    logger.warn("retry fail the info is {}, have retry {} times ",retryUnion.getRetryDTO().toString(),retryItem.getHasRetryCounts(),ex);
                }
                retryUnion.getExecutedTime().add(System.currentTimeMillis());
                ServiceSpi.persistenceService.insert(retryUnion);
                ServiceSpi.scheduleService.scheduleRetry(retryUnion);
            }
        }
    }
}

