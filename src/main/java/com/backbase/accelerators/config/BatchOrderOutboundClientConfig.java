package com.backbase.accelerators.config;

import com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration;
import com.backbase.buildingblocks.context.ContextScoped;
import com.backbase.buildingblocks.webclient.WebClientConstants;
import com.backbase.dbs.batch.outbound.v2.service.ApiClient;
import com.backbase.dbs.batch.outbound.v2.service.api.BatchOrdersApi;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;

@Getter
@Setter
@ContextScoped
@Configuration
public class BatchOrderOutboundClientConfig extends BaseConfig {


    private static final String PAYMENT_BATCH_SERVICE_ID = "payment-batch-integration-outbound-service";

    @Autowired
    @Qualifier(WebClientConstants.INTER_SERVICE_WEB_CLIENT_NAME)
    private WebClient webClient;

    @Autowired
    private DateFormat dateFormat;

    @Bean("ouboundBatchOrdersApi")
    public BatchOrdersApi ouboundBatchOrdersApi() {
        return new BatchOrdersApi(createApiClient());
    }

    private ApiClient createApiClient() {
        ApiClient apiClient = new ApiClient(webClient, null, dateFormat);
        apiClient.setBasePath(String.format("%s://%s", scheme, PAYMENT_BATCH_SERVICE_ID));
        apiClient.addDefaultHeader(HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
        return apiClient;
    }
}
