package com.backbase.accelerators.config;

import com.backbase.vantage.service.v1.blackout.dates.BlackoutDatesServiceApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;

@ExtendWith(SpringExtension.class)
class BlackoutDatesClientConfigTest {

    @InjectMocks
    BlackoutDatesClientConfig blackoutDatesClientConfig;

    @Mock
    WebClient webClient;

    @Mock
    DateFormat dateFormat;

    @Test
    void createOuboundBatchOrdersApiBean(){
        BlackoutDatesServiceApi bean = blackoutDatesClientConfig.ouboundBatchOrdersApi();
        Assertions.assertNotNull(bean);
    }
}