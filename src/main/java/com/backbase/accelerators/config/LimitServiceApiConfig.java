package com.backbase.accelerators.config;

import com.backbase.limit.v2.service.ApiClient;
import com.backbase.limit.v2.service.api.LimitsServiceApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER;
import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTER_SERVICE_REST_TEMPLATE_BEAN_NAME;

@Configuration
public class LimitServiceApiConfig extends BaseConfig{

    private static final String LIMIT_SERVICE_ID = "limit";

    @Bean
    public LimitsServiceApi limitsServiceApi(@Qualifier(INTER_SERVICE_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(scheme + "://" + LIMIT_SERVICE_ID);
        apiClient.addDefaultHeader(INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());

        return new LimitsServiceApi(apiClient);
    }
}
