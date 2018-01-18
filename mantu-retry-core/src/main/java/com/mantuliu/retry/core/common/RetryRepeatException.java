package com.mantuliu.retry.core.common;

public class RetryRepeatException extends RuntimeException {


    public IllegalArgumentException ill;
    
    public RetryRepeatException() {
        super();
    }
    
    public RetryRepeatException(String s) {
        super(s);
    }
    
    public RetryRepeatException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RetryRepeatException(Throwable cause) {
        super(cause);
    }
}
