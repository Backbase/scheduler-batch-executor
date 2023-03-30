package com.backbase.accelerators.client;

import com.backbase.dbs.batch.inbound.v2.service.api.BatchOrdersApi;
import com.backbase.dbs.batch.inbound.v2.service.model.BatchStatus;
import com.backbase.dbs.batch.inbound.v2.service.model.GetBatchOrderResponse;
import com.backbase.dbs.batch.inbound.v2.service.model.PutBatchOrderRequest;
import com.backbase.dbs.batch.inbound.v2.service.model.PutBatchOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

/**
 * This class is used to connect with OOTB batch order integration-inbound-api methods.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchOrderInboundClient {

    private final BatchOrdersApi inboundBatchOrdersApi;

    public PutBatchOrderResponse updateBatchOrderStatus(String batchOrderId, BatchStatus expectedStatus, BatchStatus batchStatus, String reasonText) {
        PutBatchOrderResponse putBatchOrderResponse = null;
        try {
            PutBatchOrderRequest putBatchOrderRequest = new PutBatchOrderRequest().status(batchStatus)
                    .bankStatus(batchStatus.getValue())
                    .reasonDescription(reasonText);
            log.debug("Expected Status {} and Request {} ", expectedStatus.toString(), putBatchOrderRequest);
            putBatchOrderResponse =  inboundBatchOrdersApi.putBatchOrder(batchOrderId, expectedStatus.getValue(), putBatchOrderRequest).block();
        } catch (Exception ex) {
            log.error(ExceptionUtils.getRootCauseMessage(ex));
            throw ex;
        }
        return putBatchOrderResponse;
    }

    public GetBatchOrderResponse getBatchOrderResponse(String batchOrderId){
        GetBatchOrderResponse batchOrderResponse = null;
        try {
            log.debug("In GetBatchOrderResponse for batchOrderId :{} ", batchOrderId);
            batchOrderResponse = inboundBatchOrdersApi.getBatchOrder(batchOrderId).block();

        }  catch (Exception ex) {
             log.error(ExceptionUtils.getRootCauseMessage(ex));
             throw ex;
        }
        return batchOrderResponse;
    }
}
