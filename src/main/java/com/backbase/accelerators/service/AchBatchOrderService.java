package com.backbase.accelerators.service;

import com.backbase.accelerators.client.*;
import com.backbase.dbs.batch.inbound.v2.service.model.BatchStatus;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import com.backbase.scheduler.batch.scheduled.v1.service.model.ScheduledBatchOrderItem;
import com.backbase.accelerators.client.*;
import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import com.backbase.accelerators.mapper.ScheduleMapper;
import com.backbase.accelerators.model.Schedule;
import com.backbase.accelerators.util.ACHBatchDateCalculator;
import com.backbase.accelerators.util.BatchConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchBatchOrderService {
    private final ScheduledBatchOrderServiceClient scheduledBatchOrderServiceClient;
    private final BatchOrderOutboundClient batchOrderOutboundClient;
    private final BatchOrderInboundClient batchOrderInboundClient;
    private final ACHBatchDateCalculator achBatchUtil;
    private final BatchOrderServiceClient batchOrderServiceClient;
    private final LimitServiceClient limitServiceClient;
    private final ScheduledBatchOrderProperties scheduledProperties;
    private final ScheduleMapper scheduleMapper;
    private final ACHBatchValidationService achBatchValidationService;

    public boolean processSchedulerBatchOrder(PostBatchOrderRequest postBatchOrderRequest, ScheduledBatchOrderItem scheduledBatchOrderItem) {
        PostBatchOrderResponse validateResponse = achBatchValidationService.validateBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem);
        if (null != validateResponse) {

            updateFailureStatus(scheduledBatchOrderItem, validateResponse, Boolean.FALSE, Boolean.TRUE);
            log.info(" Validation Failure");
            return true;
        }
        log.debug(" ===== Request to process the scheduled batch order ==== {}", postBatchOrderRequest);

        return submitBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem, false);
    }

    public void processClientBatchOrder(PostBatchOrderRequest postBatchOrderRequest) {
        log.debug(" ===== Request to process the batch order ==== {}", postBatchOrderRequest);

        LocalDate nextExecutionDate = achBatchUtil.calculateNextExecutionDate(postBatchOrderRequest.getType(), postBatchOrderRequest.getRequestedExecutionDate());
        String createdBy = batchOrderServiceClient.getCreatedUserForBatchOrder(postBatchOrderRequest.getId());

        ScheduledBatchOrderItem scheduledBatchOrderItem = scheduledBatchOrderServiceClient.createScheduledBatchOrder(postBatchOrderRequest, BatchStatus.ACKNOWLEDGED.getValue(), nextExecutionDate, createdBy);

        if (achBatchUtil.canSubmitToCGI(nextExecutionDate)) {
            submitBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem, Boolean.TRUE);
        }
    }

    private boolean submitBatchOrder(PostBatchOrderRequest postBatchOrderRequest, ScheduledBatchOrderItem scheduledBatchOrderItem, boolean isRealtimePayment) {
        boolean batchOrderSubmittted = false;
        PostBatchOrderResponse response = null;
        try {
            response = batchOrderOutboundClient.postBatchOrder(postBatchOrderRequest);
            if (com.backbase.dbs.batch.outbound.v2.service.model.BatchStatus.REJECTED.equals(response.getStatus())) {
                updateFailureStatus(scheduledBatchOrderItem, response, isRealtimePayment, Boolean.FALSE);
            } else {
                String fileName = (null != response.getAdditions() && null != response.getAdditions().get(BatchConstants.BATCH_FILE_NAME)) ? response.getAdditions().get(BatchConstants.BATCH_FILE_NAME) : null;
                updateSuccessStatus(scheduledBatchOrderItem, fileName);
                batchOrderSubmittted = true;
            }
        } catch (Exception e) {
            updateFailureStatus(scheduledBatchOrderItem, e, isRealtimePayment, Boolean.FALSE);
        }
        return batchOrderSubmittted;
    }

    private void updateSuccessStatus(ScheduledBatchOrderItem scheduledBatchOrderItem, String fileName) {
        LocalDate nextExecutionDate = null;
        if (isRecurringBatch(scheduledBatchOrderItem.getPmtMode())) {
            nextExecutionDate = getRecurringNextExecutionDate(scheduledBatchOrderItem);
        }
        if (null == nextExecutionDate)
            batchOrderInboundClient.updateBatchOrderStatus(scheduledBatchOrderItem.getBatchOrderId(), BatchStatus.DOWNLOADING, BatchStatus.ACCEPTED, null);
        scheduledBatchOrderServiceClient.updateScheduledBatchOrder(scheduledBatchOrderItem.getBatchOrderId(), BatchStatus.ACCEPTED.getValue(), null, nextExecutionDate, fileName);
    }


    private void updateFailureStatus(ScheduledBatchOrderItem scheduledBatchOrderItem, PostBatchOrderResponse response, boolean isRealtimePayment, boolean isValidationFailure) {

        updateFailureStatus(scheduledBatchOrderItem, isRealtimePayment, response, isValidationFailure);
    }

    private void updateFailureStatus(ScheduledBatchOrderItem scheduledBatchOrderItem, Exception exception, boolean isRealtimePayment, boolean isValidationFailure) {
        PostBatchOrderResponse response = new PostBatchOrderResponse()
                .reasonDescription(exception.getMessage())
                .reasonDescription((null != exception.getCause()) ? exception.getCause().getMessage() : null);
        updateFailureStatus(scheduledBatchOrderItem, isRealtimePayment, response, isValidationFailure);
    }

    private void rollBackLimits(String batchOrderId, String pmtMode) {
        if (isRecurringBatch(pmtMode) && !achBatchValidationService.isFirstRecurringPayment(batchOrderId)) {
            batchOrderId = achBatchUtil.appendYearMonthDate(batchOrderId);
            limitServiceClient.postLimitConsumptionRollback(batchOrderId);
        }
    }

    private void updateFailureStatus(ScheduledBatchOrderItem scheduledBatchOrderItem, boolean isRealtimePayment,
                                     PostBatchOrderResponse response, boolean isValidationFailure) {
        LocalDate nextExecutionDate = null;
        if (isRealtimePayment) {
            /** Not need to rollback the limits here. Because OOTB will take care of it. when the status is rejected.*/
            batchOrderInboundClient.updateBatchOrderStatus(scheduledBatchOrderItem.getBatchOrderId(),
                    BatchStatus.DOWNLOADING, BatchStatus.REJECTED, response.getReasonText());
            scheduledBatchOrderServiceClient.updateScheduledBatchOrder(scheduledBatchOrderItem.getBatchOrderId(),
                    BatchStatus.REJECTED.getValue(), response, null, null);

        } else {
            if (achBatchUtil.hasCutOffTimePassed(scheduledProperties.getCutOffTime()) || isValidationFailure) {
                rollBackLimits(scheduledBatchOrderItem.getBatchOrderId(), scheduledBatchOrderItem.getPmtMode());
                if (isRecurringBatch(scheduledBatchOrderItem.getPmtMode())) {
                    nextExecutionDate = getRecurringNextExecutionDate(scheduledBatchOrderItem);
                }
            /* The nextExecutionDate will be null only if we have reached the end date for recurring payment
             and for non-recurring payments the end date will also be null so we can
             safely assume that we are going to update only when it is null */

                if (nextExecutionDate == null) {
                    batchOrderInboundClient.updateBatchOrderStatus(scheduledBatchOrderItem.getBatchOrderId(), BatchStatus.DOWNLOADING, BatchStatus.REJECTED, response.getReasonText());
                }

                scheduledBatchOrderServiceClient.updateScheduledBatchOrder(scheduledBatchOrderItem.getBatchOrderId(), BatchStatus.REJECTED.getValue(), response, nextExecutionDate, null);
            } else {

                if (isRecurringBatch(scheduledBatchOrderItem.getPmtMode())) {
                    rollBackLimits(scheduledBatchOrderItem.getBatchOrderId(), scheduledBatchOrderItem.getPmtMode());
                }
                scheduledBatchOrderServiceClient.createScheduledBatchOrderHistory(scheduledBatchOrderItem.getBatchOrderId(), BatchStatus.REJECTED.getValue(), response, null);
            }
        }
    }

    private boolean isRecurringBatch(String pmtMode) {
        return pmtMode.equals(BatchConstants.PMT_MODE_RECURRING);
    }


    private LocalDate getRecurringNextExecutionDate(ScheduledBatchOrderItem scheduledBatchOrderItem) {
        Schedule schedule = scheduleMapper.toSchedule(scheduledBatchOrderItem);
        return achBatchUtil.calculateRecurringNextExecutionDate(schedule, scheduledBatchOrderItem.getPmtType(), scheduledBatchOrderItem.getNextExecutionDate());
    }


}

