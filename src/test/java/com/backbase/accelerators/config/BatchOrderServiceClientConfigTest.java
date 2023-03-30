package com.backbase.accelerators.config;

import com.backbase.dbs.batch.service.v2.service.api.BatchOrdersApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;

@ExtendWith(SpringExtension.class)
class BatchOrderServiceClientConfigTest {

    @InjectMocks
    BatchOrderServiceClientConfig batchOrderServiceClientConfig;

    @Mock
    WebClient webClient;

    @Mock
    DateFormat dateFormat;

    @Test
    void createServiceBatchOrderAPiBean(){
        BatchOrdersApi bean = batchOrderServiceClientConfig.serviceBatchOrderAPi();
        Assertions.assertNotNull(bean);
    }
}