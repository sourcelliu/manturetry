package com.mantuliu.retry.core.common;

public enum ResponseEnum {

    SUCCESS("0","success"),
    ERROR("99999","error"),
    SPRING_CALSS_NOT_DEFINE("00001","spring容器没有此类的对象"),
    SPRING_METHOD_NOT_DEFINE("00002","目标对象没有此方法或此方法的参数不正确"),
    SPRING_PARAMETER_IS_NULL("00003","目标方法的参数为空"),
    BLOCKQUEUE_IS_FULL("00004","重试队列已满"),
    SERVICENAME_IS_NULL("00005","服务名称为空"),
    RetryStatyge_IS_NULL("00006","重试策略都为空"),
    RetryStatyge_NUMS_ZERO("00007","重试策略中的重试次数为0"),
    RetryStatyge_EVENSPACETIME_ZERO("00008","均匀间隔重试的时间小于等于0"),
    RetryStatyge_EVENSPACETIMEUNIT_NULL("00009","均匀间隔重试的的时间单位没有设置"),
    PARAMETERS_HASNO_SERIALIZABLE("00010","目标方法的部分参数没有实现Serializable接口");
    
    private String respCode;
    private String respContent;
    
    ResponseEnum(String respCode,String respContent){
        this.setRespCode(respCode);
        this.setRespContent(respContent);
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespContent() {
        return respContent;
    }

    public void setRespContent(String respContent) {
        this.respContent = respContent;
    }
}
