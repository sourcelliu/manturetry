package com.mantuliu.retry.core.call;

import com.mantuliu.retry.core.entity.RetryUnion;

public interface ProcessorService {

    void process(RetryUnion retryUnion);
}
