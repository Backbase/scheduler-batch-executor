package com.backbase.accelerators.client;

import com.backbase.dbs.batch.inbound.v2.service.api.BatchOrdersApi;
import com.backbase.dbs.batch.inbound.v2.service.model.BatchStatus;
import com.backbase.dbs.batch.inbound.v2.service.model.GetBatchOrderResponse;
import com.backbase.dbs.batch.inbound.v2.service.model.PutBatchOrderResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
class BatchOrderInboundClientTest {

    @InjectMocks
    BatchOrderInboundClient batchOrderInboundClient;
    @Mock
    BatchOrdersApi inboundBatchOrdersApi;

    @Test
    void updateBatchOrderStatus(){
        PutBatchOrderResponse putBatchOrderResponse = new PutBatchOrderResponse()
                .batchReference("batchRef")
                .bankStatus("ACCEPTED")
                .reasonDescription("description");

        Mockito.when(inboundBatchOrdersApi.putBatchOrder(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(Mono.just(putBatchOrderResponse));

        PutBatchOrderResponse response = batchOrderInboundClient
                .updateBatchOrderStatus("id-1", BatchStatus.ACCEPTED, BatchStatus.ACCEPTED, "");

        Assertions.assertNotNull(response);
        Assertions.assertEquals("ACCEPTED", response.getBankStatus());
        Assertions.assertEquals("description", response.getReasonDescription());
        Assertions.assertEquals("batchRef", response.getBatchReference());
    }

    @Test
    void updateBatchOrderStatusThrowsException(){

        Mockito.when(inboundBatchOrdersApi.putBatchOrder(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenThrow(RestClientException.class);

        Assertions.assertThrows(RestClientException.class, () -> batchOrderInboundClient
                .updateBatchOrderStatus("id-1", BatchStatus.REJECTED, BatchStatus.REJECTED, "abc"));
    }

    @Test
    void getBatchOrderResponseGivenBatchOrderId(){
        GetBatchOrderResponse getBatchOrderResponse = new GetBatchOrderResponse()
                .batchReference("batchRef")
                .bankStatus("ACCEPTED")
                .reasonDescription("description");

        Mockito.when(inboundBatchOrdersApi.getBatchOrder(Mockito.anyString()))
                .thenReturn(Mono.just(getBatchOrderResponse));

        GetBatchOrderResponse response = batchOrderInboundClient
                .getBatchOrderResponse("id-1");

        Assertions.assertNotNull(response);
        Assertions.assertEquals("ACCEPTED", response.getBankStatus());
        Assertions.assertEquals("description", response.getReasonDescription());
        Assertions.assertEquals("batchRef", response.getBatchReference());
    }

    @Test
    void getBatchOrderResponseThrowsException(){

        Mockito.when(inboundBatchOrdersApi.getBatchOrder(Mockito.anyString()))
                .thenThrow(RestClientException.class);

        Assertions.assertThrows(RestClientException.class, () -> batchOrderInboundClient
                .getBatchOrderResponse("id-1"));
    }

}