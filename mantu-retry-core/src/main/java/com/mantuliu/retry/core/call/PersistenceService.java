package com.mantuliu.retry.core.call;

import com.mantuliu.retry.core.entity.RetryUnion;

public interface PersistenceService {

    void insert(RetryUnion retryUnion);
    void executeSuccess(RetryUnion retryUnion);
    void executeDead(RetryUnion retryUnion);
    void rebuildRetryTask();
}
