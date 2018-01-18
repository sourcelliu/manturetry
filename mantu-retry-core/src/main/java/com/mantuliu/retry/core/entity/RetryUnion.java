package com.mantuliu.retry.core.entity;

import java.io.Serializable;
import java.util.List;


public class RetryUnion implements Serializable{


    private RetryDTO retryDTO;
    private RetryStrategyDTO[] retryTypes;
    private int currentRetryType = 0;//执行的RetryType顺序
    private String retryKey;
    private List<Long> executedTime;
    public RetryDTO getRetryDTO() {
        return retryDTO;
    }
    public void setRetryDTO(RetryDTO retryDTO) {
        this.retryDTO = retryDTO;
    }
    public int getCurrentRetryType() {
        return currentRetryType;
    }
    public void setCurrentRetryType(int currentRetryType) {
        this.currentRetryType = currentRetryType;
    }
    public RetryStrategyDTO[] getRetryTypes() {
        return retryTypes;
    }
    public void setRetryTypes(RetryStrategyDTO[] retryTypes) {
        this.retryTypes = retryTypes;
    }
    public String getRetryKey() {
        return retryKey;
    }
    public void setRetryKey(String retryKey) {
        this.retryKey = retryKey;
    }
    public List<Long> getExecutedTime() {
        return executedTime;
    }
    public void setExecutedTime(List<Long> executedTime) {
        this.executedTime = executedTime;
    }
    
}
