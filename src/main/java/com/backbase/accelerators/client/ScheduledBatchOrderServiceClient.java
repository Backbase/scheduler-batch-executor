package com.backbase.accelerators.client;

import com.backbase.accelerators.mapper.ScheduledBatchOrderMapper;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderApi;
import com.backbase.scheduler.batch.scheduled.v1.service.api.ScheduledBatchOrderHistoryServiceApi;
import com.backbase.scheduler.batch.scheduled.v1.service.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * This class is used to connect with Scheduled batch order service-api methods.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledBatchOrderServiceClient {

    private final ScheduledBatchOrderApi scheduledBatchOrderApi;

    private final ScheduledBatchOrderHistoryServiceApi scheduledBatchOrderTransactionsApi;
    private final ScheduledBatchOrderMapper scheduledBatchOrderMapper;

    public ScheduledBatchOrderItem createScheduledBatchOrder(
            PostBatchOrderRequest postBatchOrderRequest, String batchStatus, LocalDate nextExecutionDate, String createdBy) {

        log.debug("Creating scheduled Batch order record for batch order Id: {}", postBatchOrderRequest.getId());

        PostScheduledBatchOrderRequest scheduledBatchOrderRequest = scheduledBatchOrderMapper
                .toPostScheduledBatchOrderRequest(postBatchOrderRequest, nextExecutionDate, batchStatus, createdBy);

        PostScheduledBatchOrderResponse response = scheduledBatchOrderApi.postScheduledBatchOrder(scheduledBatchOrderRequest);
        log.debug("Scheduled Batch order created. TransactionId: {}", response.getBatchOrderItem());
        return response.getBatchOrderItem();
    }

    public void createScheduledBatchOrderHistory(
            String batchOrderId, String batchStatus, PostBatchOrderResponse response, String fileName) {

        log.debug("Creating scheduled Batch order History record for the batch order Id {}", batchOrderId);

        PostScheduledBatchOrderHistoryRequest scheduledBatchOrderRequest = scheduledBatchOrderMapper
                .toPostScheduledBatchOrderHistoryRequest(batchOrderId, batchStatus, response, fileName);
        try {
            scheduledBatchOrderTransactionsApi.postScheduledBatchOrderHistory(scheduledBatchOrderRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("Created scheduled Batch order History record Successfully for the batch order Id {}", batchOrderId);
    }

    public PutScheduledBatchOrderResponse updateScheduledBatchOrder(
            String batchOrderId, String batchStatus, PostBatchOrderResponse response, LocalDate nextExecutionDate, String fileName) {

        log.debug("Creating scheduled payment order transaction record for scheduled payment order: {}",
                batchOrderId);
        PutScheduledBatchOrderRequest scheduledBatchOrderRequest = scheduledBatchOrderMapper.toPutScheduledBatchOrderRequest(batchOrderId, batchStatus, response, nextExecutionDate, fileName);
        PutScheduledBatchOrderResponse putScheduledBatchOrderResponse = scheduledBatchOrderApi.putScheduledBatchOrder(scheduledBatchOrderRequest);
        log.debug("Updating the status of Scheduled Batch order  {}", putScheduledBatchOrderResponse.getId());
        return putScheduledBatchOrderResponse;
    }


    public List<ScheduledBatchOrderItem> getScheduledBatchOrders(List<ScheduledBatchOrderItem> scheduledBatchOrdersList, LocalDate nextExecutionDate, int from, int size) {
        log.info("Get scheduled batch payment orders for scheduled payment order");
        List<ScheduledBatchOrderItem> scheduledBatchOrderItemList = scheduledBatchOrderApi.getScheduledBatchOrders(nextExecutionDate, from, size);
        if (scheduledBatchOrderItemList != null && !scheduledBatchOrderItemList.isEmpty()) {
            log.info("Batch payment orders for scheduled payment order size retrieved : {}", scheduledBatchOrderItemList.size());
            scheduledBatchOrdersList.addAll(scheduledBatchOrderItemList);
            getScheduledBatchOrders(scheduledBatchOrdersList, nextExecutionDate, ++from, size);
        }
        return scheduledBatchOrdersList;
    }

    public int getScheduledBatchOrderHistoryCount(String batchOrderId, LocalDate toDate) {

        List<ScheduledBatchHistoryItem> batchHistoryItemList = scheduledBatchOrderTransactionsApi.getScheduledBatchOrderHistory(null,toDate,null,null,null,batchOrderId,0,10);
        return CollectionUtils.isEmpty(batchHistoryItemList) ? 0 : batchHistoryItemList.size();

    }
}
