package com.backbase.accelerators.config;

import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderApi;
import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderHistoryServiceApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
class ScheduledBatchOrderServiceApiConfigTest {

    @InjectMocks
    ScheduledBatchOrderServiceApiConfig scheduledBatchOrderServiceApiConfig;

    @Mock
    RestTemplate restTemplate;

    @Test
    void createScheduledBatchOrderApiBean(){
        ScheduledBatchOrderApi bean = scheduledBatchOrderServiceApiConfig.scheduledBatchOrderApi(restTemplate);
        Assertions.assertNotNull(bean);
    }

    @Test
    void createScheduledBatchOrderTransactionsApiBean(){
        ScheduledBatchOrderHistoryServiceApi bean = scheduledBatchOrderServiceApiConfig.scheduledBatchOrderTransactionsApi(restTemplate);
        Assertions.assertNotNull(bean);
    }

}