package com.backbase.accelerators.client;

import com.backbase.dbs.batch.outbound.v2.service.api.BatchOrdersApi;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to connect with OOTB batch order integration-outbound-api methods.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchOrderOutboundClient {

    private static final String IS_SCHEDULER_REQUEST = "isSchedulerReq";
    private final BatchOrdersApi ouboundBatchOrdersApi;

    public PostBatchOrderResponse postBatchOrder(PostBatchOrderRequest postBatchOrderRequest) {
        Map<String, String> additions = postBatchOrderRequest.getAdditions() == null ? new HashMap<>() : postBatchOrderRequest.getAdditions();
        additions.put(IS_SCHEDULER_REQUEST, "true");
        postBatchOrderRequest.setAdditions(additions);
        return ouboundBatchOrdersApi.postBatches(postBatchOrderRequest).block();
    }
}
