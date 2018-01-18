package com.mantuliu.retry.core.call.schedule;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mantuliu.retry.core.common.Commons;
import com.mantuliu.retry.core.common.ServiceSpi;
import com.mantuliu.retry.core.entity.RetryStrategyDTO;
import com.mantuliu.retry.core.entity.RetryUnion;
import com.mantuliu.retry.core.util.RetryProcessTaskFactory;


public class DefaultScheduleServiceImpl implements ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultScheduleServiceImpl.class);
    private final ScheduledThreadPoolExecutor scheduledExecutorSrvice = new ScheduledThreadPoolExecutor(Commons.SCHEDULE_POOL_SIZE,new RetryProcessTaskFactory(Commons.SCHEDULE_POOL_NAME));
    
    @Override
    public void scheduleRetry(RetryUnion retryUnion) {
    	
    	RetryStrategyDTO[] retryTypes = retryUnion.getRetryTypes();
        if(retryTypes!=null) {
            for(int i = 0;i<retryTypes.length;i++) {
            	RetryStrategyDTO evenItem = retryTypes[i];
                if(evenItem.getHasRetryCounts().get()<evenItem.getCounts()) {
                    retryUnion.setCurrentRetryType(i);
                    boolean isExecuteNow = false;
                    if(evenItem.getInterval()<=0) {
                    	isExecuteNow = true;
                    }else {
                        List<Long> timeLists = retryUnion.getExecutedTime();
                        if(timeLists!=null && timeLists.size()>0) {
                            TimeUnit unit = evenItem.getUnit();
                            long time = timeLists.get(timeLists.size()-1);
                            long nowTime = System.currentTimeMillis();
                            //过期立即执行
                            if(unit.equals(TimeUnit.SECONDS)) {
                                if((nowTime-time)/1000-evenItem.getInterval()>=0) {
                                    isExecuteNow=true;
                                }
                            }else if(unit.equals(TimeUnit.MINUTES)) {
                                if((nowTime-time)/1000/60-evenItem.getInterval()>=0) {
                                    isExecuteNow=true;
                                }
                            }else if(unit.equals(TimeUnit.HOURS)) {
                                if((nowTime-time)/1000/60/60-evenItem.getInterval()>=0) {
                                    isExecuteNow=true;
                                }
                            }else if(unit.equals(TimeUnit.DAYS)) {
                                if((nowTime-time)/1000/60/60/24-evenItem.getInterval()>=0) {
                                    isExecuteNow=true;
                                }
                            }else if (unit.equals(TimeUnit.MILLISECONDS)) {
                                if(nowTime - time - evenItem.getInterval() > 0) {
                                    isExecuteNow=true;
                                }
                            }else if (unit.equals(TimeUnit.MICROSECONDS)) {
                                if((nowTime - time)*1000 - evenItem.getInterval() > 0) {
                                    isExecuteNow=true;
                                }
                            }else if (unit.equals(TimeUnit.NANOSECONDS)) {
                                if((nowTime - time)*1000*1000 - evenItem.getInterval() > 0) {
                                    isExecuteNow=true;
                                }
                            }
                        }
                        int queueSize = scheduledExecutorSrvice.getQueue().size();
                        if(queueSize > Commons.MAX_RETRY_SIZE) {
                            logger.warn("retry framework warn.scheduledExecutorSrvice's queue size is overflow now the queue size is {} ",queueSize);
                            return;
                        }
                    }
                    if(isExecuteNow) {
                    	scheduledExecutorSrvice.schedule(new ScheduleTask(retryUnion), 0L, TimeUnit.SECONDS);
                    }else {
                        scheduledExecutorSrvice.schedule(new ScheduleTask(retryUnion), evenItem.getInterval(), evenItem.getUnit());
                    }
                    return;
                }
            }
        }
        
        ServiceSpi.persistenceService.executeDead(retryUnion);
    }

    private class ScheduleTask implements Runnable{
        private RetryUnion retryUnion;
        private ScheduleTask(RetryUnion retryUnion) {
            this.retryUnion=retryUnion;
        }
        
        @Override
        public void run() {
            ServiceSpi.processorService.process(retryUnion);
        }
    }
}
