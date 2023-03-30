package com.backbase.accelerators.config;


import com.backbase.scheduler.batch.scheduled.v1.service.ApiClient;
import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderApi;
import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderHistoryServiceApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER;
import static com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration.INTER_SERVICE_REST_TEMPLATE_BEAN_NAME;

@Configuration
public class ScheduledBatchOrderServiceApiConfig extends BaseConfig {

    private static final String SCHEDULED_BATCH_ORDER_SERVICE_ID = "scheduler-batch-order-service";


    @Bean
    public ScheduledBatchOrderApi scheduledBatchOrderApi(
            @Qualifier(INTER_SERVICE_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {

        return new ScheduledBatchOrderApi(createAPiClient(restTemplate));
    }

    @Bean
    public ScheduledBatchOrderHistoryServiceApi scheduledBatchOrderTransactionsApi(
            @Qualifier(INTER_SERVICE_REST_TEMPLATE_BEAN_NAME) RestTemplate restTemplate) {

        return new ScheduledBatchOrderHistoryServiceApi(createAPiClient(restTemplate));
    }

    private ApiClient createAPiClient(RestTemplate restTemplate) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(scheme + "://" + SCHEDULED_BATCH_ORDER_SERVICE_ID);
        apiClient.addDefaultHeader(INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
        return apiClient;
    }
}
