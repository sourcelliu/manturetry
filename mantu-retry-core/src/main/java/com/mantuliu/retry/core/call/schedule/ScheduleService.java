package com.mantuliu.retry.core.call.schedule;

import com.mantuliu.retry.core.entity.RetryUnion;

public interface ScheduleService {


    void scheduleRetry(RetryUnion retryUnion);
}
