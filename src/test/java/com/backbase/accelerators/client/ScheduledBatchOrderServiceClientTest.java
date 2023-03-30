package com.backbase.accelerators.client;

import com.backbase.accelerators.mapper.ScheduledBatchOrderMapper;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderApi;
import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderHistoryServiceApi;
import com.backbase.scheduler.batch.scheduled.v1.service.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ScheduledBatchOrderServiceClientTest {

    @InjectMocks
    ScheduledBatchOrderServiceClient scheduledBatchOrderServiceClient;

    @Mock
    ScheduledBatchOrderApi scheduledBatchOrderApi;

    @Mock
    ScheduledBatchOrderHistoryServiceApi scheduledBatchOrderTransactionsApi;

    @Mock
    ScheduledBatchOrderMapper scheduledBatchOrderMapper;

    @Test
    void createScheduledBatchOrder(){

        PostScheduledBatchOrderRequest postScheduledBatchOrderRequest = new PostScheduledBatchOrderRequest();
        Mockito.when(scheduledBatchOrderMapper.toPostScheduledBatchOrderRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(postScheduledBatchOrderRequest);

        PostScheduledBatchOrderResponse postScheduledBatchOrderResponse =
                new PostScheduledBatchOrderResponse().batchOrderItem(new ScheduledBatchOrderItem().batchOrderId("batch-id"));
        Mockito.when(scheduledBatchOrderApi.postScheduledBatchOrder(postScheduledBatchOrderRequest))
                .thenReturn(postScheduledBatchOrderResponse);

        ScheduledBatchOrderItem item = scheduledBatchOrderServiceClient.
                createScheduledBatchOrder(new PostBatchOrderRequest(), "ACCEPTED", LocalDate.now(), "agent");

        Assertions.assertNotNull(item);
        Assertions.assertEquals("batch-id", item.getBatchOrderId());
    }

    @Test
    void createScheduledBatchOrderHistory(){

        PostScheduledBatchOrderHistoryRequest postScheduledBatchOrderHistoryRequest =
                new PostScheduledBatchOrderHistoryRequest();
        Mockito.when(scheduledBatchOrderMapper.toPostScheduledBatchOrderHistoryRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(postScheduledBatchOrderHistoryRequest);

        scheduledBatchOrderServiceClient.createScheduledBatchOrderHistory("batch-id", "READY", new PostBatchOrderResponse(), "file.txt");

        Mockito.verify(scheduledBatchOrderTransactionsApi, Mockito.times(1)).postScheduledBatchOrderHistory(Mockito.any());
    }

    @Test
    void createScheduledBatchOrderHistoryThrowsException(){

        PostScheduledBatchOrderHistoryRequest postScheduledBatchOrderHistoryRequest =
                new PostScheduledBatchOrderHistoryRequest();
        Mockito.when(scheduledBatchOrderMapper.toPostScheduledBatchOrderHistoryRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(postScheduledBatchOrderHistoryRequest);

        Mockito.when(scheduledBatchOrderTransactionsApi.postScheduledBatchOrderHistory(Mockito.any()))
                .thenThrow(RestClientException.class);

        scheduledBatchOrderServiceClient.createScheduledBatchOrderHistory("batch-id", "READY", new PostBatchOrderResponse(), "file.txt");

        Mockito.verify(scheduledBatchOrderTransactionsApi, Mockito.times(1)).postScheduledBatchOrderHistory(Mockito.any());
    }

    @Test
    void updateScheduledBatchOrder(){

        PutScheduledBatchOrderRequest putScheduledBatchOrderRequest = new PutScheduledBatchOrderRequest();
        Mockito.when(scheduledBatchOrderMapper.toPutScheduledBatchOrderRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(putScheduledBatchOrderRequest);

        PutScheduledBatchOrderResponse putScheduledBatchOrderResponse =
                new PutScheduledBatchOrderResponse().id("batch-id");
        Mockito.when(scheduledBatchOrderApi.putScheduledBatchOrder(putScheduledBatchOrderRequest))
                .thenReturn(putScheduledBatchOrderResponse);

        PutScheduledBatchOrderResponse response = scheduledBatchOrderServiceClient.
                updateScheduledBatchOrder("batch-id", "READY",  new PostBatchOrderResponse(), LocalDate.now(), "file.txt");

        Assertions.assertNotNull(response);
        Assertions.assertEquals("batch-id", response.getId());
    }

    @Test
    void getScheduledBatchOrders(){

        ScheduledBatchOrderItem scheduledBatchOrderItem = new ScheduledBatchOrderItem();
        Mockito.when(scheduledBatchOrderApi.getScheduledBatchOrders(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(scheduledBatchOrderItem)).thenReturn(List.of());

        List items = new ArrayList<ScheduledBatchOrderItem>();
        List<ScheduledBatchOrderItem> itemList = scheduledBatchOrderServiceClient
                .getScheduledBatchOrders(items, LocalDate.now(), 1,1);

        Assertions.assertNotNull(itemList);
        Assertions.assertEquals(1, itemList.size());
    }

    @Test
    void getScheduledBatchOrderHistoryCount(){

        List scheduledBatchHistoryItems = new ArrayList<ScheduledBatchHistoryItem>();
        scheduledBatchHistoryItems.add(new ScheduledBatchHistoryItem().batchOrderId("batch-id"));
        Mockito.when(scheduledBatchOrderTransactionsApi.getScheduledBatchOrderHistory(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(scheduledBatchHistoryItems);

        int count = scheduledBatchOrderServiceClient
                .getScheduledBatchOrderHistoryCount("batch-id", LocalDate.now());
        Assertions.assertEquals(1, count);
    }

    @Test
    void getScheduledBatchOrderHistoryCountReturn0(){

        List scheduledBatchHistoryItems = new ArrayList<ScheduledBatchHistoryItem>();
        Mockito.when(scheduledBatchOrderTransactionsApi.getScheduledBatchOrderHistory(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(scheduledBatchHistoryItems);

        int count = scheduledBatchOrderServiceClient
                .getScheduledBatchOrderHistoryCount("batch-id", LocalDate.now());
        Assertions.assertEquals(0, count);
    }
}