package com.backbase.accelerators.service;

import com.backbase.accelerators.client.*;
import com.backbase.accelerators.mapper.ScheduleMapper;
import com.backbase.dbs.batch.outbound.v2.service.model.BatchStatus;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import com.backbase.scheduler.batch.scheduled.v1.service.model.ScheduledBatchOrderItem;
import com.backbase.accelerators.client.*;
import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import com.backbase.accelerators.util.ACHBatchDateCalculator;
import com.backbase.accelerators.util.BatchConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
class AchBatchOrderServiceTest {

    @InjectMocks
    AchBatchOrderService achBatchOrderService;

    @Mock
    ScheduledBatchOrderServiceClient scheduledBatchOrderServiceClient;
    @Mock
    BatchOrderOutboundClient batchOrderOutboundClient;
    @Mock
    BatchOrderInboundClient batchOrderInboundClient;
    @Mock
    ACHBatchDateCalculator achBatchUtil;
    @Mock
    BatchOrderServiceClient batchOrderServiceClient;
    @Mock
    LimitServiceClient limitServiceClient;
    @Mock
    ScheduledBatchOrderProperties scheduledProperties;
    @Mock
    ScheduleMapper scheduleMapper;
    @Mock
    ACHBatchValidationService achBatchValidationService;

    @Test
    void processSchedulerBatchOrderGivenPostBatchOrderResponseIsNotNull(){
        PostBatchOrderResponse postBatchOrderResponse =
                new PostBatchOrderResponse().reasonText("reason");

        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id");

        ScheduledBatchOrderItem scheduledBatchOrderItem =
                new ScheduledBatchOrderItem().batchOrderId("batch-id").pmtMode(BatchConstants.PMT_MODE_RECURRING);

        Mockito.when(achBatchValidationService.validateBatchOrder(Mockito.any(), Mockito.any()))
                .thenReturn(postBatchOrderResponse);

        Mockito.when(achBatchValidationService.isFirstRecurringPayment(Mockito.any()))
                .thenReturn(false);

        Mockito.when(achBatchUtil.appendYearMonthDate(Mockito.any()))
                .thenReturn("batch-id");

        Assertions.assertTrue(achBatchOrderService.processSchedulerBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem));
    }

    @Test
    void processSchedulerBatchOrderGivenPostBatchOrderResponseNullAndBatchStatusRejected(){

        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id");

        ScheduledBatchOrderItem scheduledBatchOrderItem =
                new ScheduledBatchOrderItem().batchOrderId("batch-id").pmtMode(BatchConstants.PMT_MODE_RECURRING);

        Mockito.when(achBatchValidationService.validateBatchOrder(Mockito.any(), Mockito.any()))
                .thenReturn(null);

        Mockito.when(batchOrderOutboundClient.postBatchOrder(Mockito.any()))
                .thenReturn(new PostBatchOrderResponse().status(BatchStatus.REJECTED));

        Assertions.assertFalse(achBatchOrderService.processSchedulerBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem));
    }

    @Test
    void processSchedulerBatchOrderGivenPostBatchOrderResponseNullAndBatchStatusNotRejected(){

        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id");

        ScheduledBatchOrderItem scheduledBatchOrderItem =
                new ScheduledBatchOrderItem().batchOrderId("batch-id").pmtMode(BatchConstants.PMT_MODE_RECURRING);

        Mockito.when(achBatchValidationService.validateBatchOrder(Mockito.any(), Mockito.any()))
                .thenReturn(null);

        Mockito.when(batchOrderOutboundClient.postBatchOrder(Mockito.any()))
                .thenReturn(new PostBatchOrderResponse().status(BatchStatus.ACCEPTED));

        Assertions.assertTrue(achBatchOrderService.processSchedulerBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem));
    }

    @Test
    void processSchedulerBatchOrderGivenPostBatchOrderReturnNull(){

        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id");

        ScheduledBatchOrderItem scheduledBatchOrderItem =
                new ScheduledBatchOrderItem().batchOrderId("batch-id").pmtMode(BatchConstants.PMT_MODE_RECURRING);

        Mockito.when(achBatchValidationService.validateBatchOrder(Mockito.any(), Mockito.any()))
                .thenReturn(null);

        Mockito.when(batchOrderOutboundClient.postBatchOrder(Mockito.any()))
                .thenReturn(null);

        Assertions.assertFalse(achBatchOrderService.processSchedulerBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem));
    }

    @Test
    void processClientBatchOrder(){
        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id");

        Mockito.when(achBatchUtil.calculateNextExecutionDate(Mockito.any(), Mockito.any())).thenReturn(LocalDate.EPOCH);
        Mockito.when(batchOrderServiceClient.getCreatedUserForBatchOrder(Mockito.any())).thenReturn("user");

        ScheduledBatchOrderItem scheduledBatchOrderItem =
                new ScheduledBatchOrderItem().batchOrderId("batch-id").pmtMode(BatchConstants.PMT_MODE_RECURRING);
        Mockito.when(scheduledBatchOrderServiceClient.createScheduledBatchOrder(Mockito.any(), Mockito.any(), Mockito.any(),Mockito.any()))
                .thenReturn(scheduledBatchOrderItem);

        Mockito.when(achBatchUtil.canSubmitToCGI(Mockito.any())).thenReturn(true);

        achBatchOrderService.processClientBatchOrder(postBatchOrderRequest);

        Mockito.verify(batchOrderInboundClient, Mockito.times(1))
                .updateBatchOrderStatus(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(scheduledBatchOrderServiceClient, Mockito.times(1))
                .updateScheduledBatchOrder(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}