package com.mantuliu.retry.core.common;

public class Commons {

    public static final String RETRY_KEY ="retry:";
    public static final String SUCCESS_KEY ="success:";
    public static final String DEAD_KEY ="dead:";
    public static Integer MAX_RETRY_SIZE = 10000;//系统支持的最大重试任务数量
    public static final Integer SCHEDULE_POOL_SIZE = 20;//调度线程池可执行线程数量
    public static final Integer PROCESS_POOL_SIZE = 20;//处理线程池可执行线程数量
    public static final String SCHEDULE_POOL_NAME = "retryschedulepool";//调度线程池名称
    public static final String PROCCESS_POOL_NAME = "retryproccesspool";//处理线程池名称
    public static String SERVICENAME = "default";
}
