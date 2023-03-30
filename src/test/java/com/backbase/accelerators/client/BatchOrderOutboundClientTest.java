package com.backbase.accelerators.client;

import com.backbase.dbs.batch.outbound.v2.service.api.BatchOrdersApi;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
class BatchOrderOutboundClientTest {

    @InjectMocks
    BatchOrderOutboundClient batchOrderOutboundClient;

    @Mock
    BatchOrdersApi outboundBatchOrdersApi;

    @Test
    void postBatchOrderGivenAdditionsIsNullInRequest(){
        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest();
        PostBatchOrderResponse postBatchOrderResponse = new PostBatchOrderResponse();
        Map additions = new HashMap<String, String>();
        additions.put("isSchedulerReq", "true");
        postBatchOrderResponse.additions(additions);

        Mockito.when(outboundBatchOrdersApi.postBatches(Mockito.any()))
                .thenReturn(Mono.just(postBatchOrderResponse));

        PostBatchOrderResponse response = batchOrderOutboundClient
                .postBatchOrder(postBatchOrderRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("true", response.getAdditions().get("isSchedulerReq"));
    }

    @Test
    void postBatchOrderGivenAdditionsIsNotNullInRequest(){
        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest();
        postBatchOrderRequest.setAdditions(new HashMap<>());
        PostBatchOrderResponse postBatchOrderResponse = new PostBatchOrderResponse();
        Map additions = new HashMap<String, String>();
        additions.put("isSchedulerReq", "true");
        postBatchOrderResponse.additions(additions);

        Mockito.when(outboundBatchOrdersApi.postBatches(Mockito.any()))
                .thenReturn(Mono.just(postBatchOrderResponse));

        PostBatchOrderResponse response = batchOrderOutboundClient
                .postBatchOrder(postBatchOrderRequest);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("true", response.getAdditions().get("isSchedulerReq"));
    }
}