package com.mantuliu.retry.core.common;

import com.mantuliu.retry.core.call.MockPersistenceServiceImpl;
import com.mantuliu.retry.core.call.PersistenceService;
import com.mantuliu.retry.core.call.ProcessorService;
import com.mantuliu.retry.core.call.schedule.ScheduleService;

public class ServiceSpi {
    public static ScheduleService scheduleService = null;
    public static ProcessorService processorService = null;
    public static PersistenceService persistenceService = new MockPersistenceServiceImpl();
}
