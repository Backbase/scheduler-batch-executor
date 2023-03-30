package com.backbase.accelerators.service;

import com.backbase.dbs.batch.inbound.v2.service.model.GetBatchOrderResponse;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.scheduler.batch.scheduled.v1.service.model.ScheduledBatchOrderItem;
import com.backbase.accelerators.client.BatchOrderInboundClient;
import com.backbase.accelerators.client.ScheduledBatchOrderServiceClient;
import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import com.backbase.accelerators.mapper.ScheduledBatchOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledBatchPaymentExecutorService {


    private final ScheduledBatchOrderServiceClient scheduledBatchOrderServiceClient;
    private final BatchOrderInboundClient batchOrderInboundClient;
    private final ScheduledBatchOrderMapper scheduledBatchOrderMapper;
    private final AchBatchOrderService achBatchOrderService;
    private final ScheduledBatchOrderProperties scheduledProperties;

    private static final int FROM_COUNT = 0;

    @Scheduled(cron = "${scheduled-batch.cron-expression}")
    public void execute() {
        int failureCount = 0;
        log.info("current Time {}", LocalDateTime.now());
        List<ScheduledBatchOrderItem> scheduledBatchOrderItems = getScheduledBatchOrders();
        if (scheduledBatchOrderItems != null && !scheduledBatchOrderItems.isEmpty()) {
            for (ScheduledBatchOrderItem scheduledBatchOrderItem : scheduledBatchOrderItems) {
                if (failureCount < scheduledProperties.getMaxFailureCountPerSchedule()) {
                    GetBatchOrderResponse response = batchOrderInboundClient.getBatchOrderResponse(scheduledBatchOrderItem.getBatchOrderId());
                    PostBatchOrderRequest batchOrderRequest = scheduledBatchOrderMapper.toPostBatchOrderRequest(response);
                    boolean batchOrderSubmitSuccess = achBatchOrderService.processSchedulerBatchOrder(batchOrderRequest, scheduledBatchOrderItem);
                    if (!batchOrderSubmitSuccess) {
                        failureCount++;
                        log.error("Failure count is reached to {}. Not processing any more records", failureCount);
                    }
                }
            }
        }
    }


    private List<ScheduledBatchOrderItem> getScheduledBatchOrders() {
        List<ScheduledBatchOrderItem> scheduledBatchOrderItems = new ArrayList<>();
        LocalDate now = LocalDate.now();
        return scheduledBatchOrderServiceClient.getScheduledBatchOrders(scheduledBatchOrderItems, now, FROM_COUNT, scheduledProperties.getPageSize());
    }

}
