package com.backbase.accelerators.config;

import com.backbase.limit.v2.service.api.LimitsServiceApi;
import com.backbase.vantage.service.v1.blackout.dates.BlackoutDatesServiceApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
class LimitServiceApiConfigTest {

    @InjectMocks
    LimitServiceApiConfig limitServiceApiConfig;

    @Mock
    RestTemplate restTemplate;

    @Test
    void createLimitsServiceApiBean(){
        LimitsServiceApi bean = limitServiceApiConfig.limitsServiceApi(restTemplate);
        Assertions.assertNotNull(bean);
    }
}