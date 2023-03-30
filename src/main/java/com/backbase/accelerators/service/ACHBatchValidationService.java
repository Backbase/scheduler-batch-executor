package com.backbase.accelerators.service;


import com.backbase.arrangement.integration.listener.client.v2.persistence.arrangements.ArrangementsApi;
import com.backbase.dbs.batch.outbound.v2.service.model.BatchStatus;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import com.backbase.dbs.user.manager.models.v2.GetIdentity;
import com.backbase.dbs.user.manager.models.v2.GetUser;
import com.backbase.limit.v2.service.model.LimitsCheckPostResponseBody;
import com.backbase.pandp.arrangement.query.rest.spec.v2.arrangements.AccountArrangementItem;
import com.backbase.scheduler.batch.scheduled.v1.service.model.ScheduledBatchOrderItem;
import com.backbase.accelerators.client.LimitServiceClient;
import com.backbase.accelerators.client.ScheduledBatchOrderServiceClient;
import com.backbase.accelerators.common.client.UserManagementCommonClient;
import com.backbase.accelerators.common.util.JsonUtil;
import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import com.backbase.accelerators.util.ACHBatchDateCalculator;
import com.backbase.accelerators.util.BatchConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ACHBatchValidationService {

    private final ScheduledBatchOrderProperties scheduledBatchOrderProperties;
    private final ArrangementsApi arrangementsApi;
    private final UserManagementCommonClient userManagementCommonClient;
    private final LimitServiceClient limitServiceClient;
    private final ScheduledBatchOrderServiceClient scheduledBatchOrderServiceClient;


    private final ACHBatchDateCalculator achBatchDateCalculator;


    public PostBatchOrderResponse validateBatchOrder(PostBatchOrderRequest postBatchOrderRequest, ScheduledBatchOrderItem scheduledBatchOrderItem) {
        PostBatchOrderResponse response = null;
        if (null == isBalanceCheckSuccess(postBatchOrderRequest))
            response = validateRecurringPayment(postBatchOrderRequest, scheduledBatchOrderItem);

        return response;
    }

    private PostBatchOrderResponse validateRecurringPayment(PostBatchOrderRequest postBatchOrderRequest, ScheduledBatchOrderItem scheduledBatchOrderItem) {
        PostBatchOrderResponse response = null;
        if (BatchConstants.PMT_MODE_RECURRING.equalsIgnoreCase(scheduledBatchOrderItem.getPmtMode())
                && !isFirstRecurringPayment(postBatchOrderRequest.getId())) {
            response = isValidUser(scheduledBatchOrderItem.getCreatedBy());
            if (null == response)
                response = limitConsumption(postBatchOrderRequest, scheduledBatchOrderItem.getCreatedBy());
        }
        return response;
    }

    private PostBatchOrderResponse isBalanceCheckSuccess(PostBatchOrderRequest postBatchOrderRequest) {
        PostBatchOrderResponse response = null;
        if (!scheduledBatchOrderProperties.isBalanceCheckEnabled() && !scheduledBatchOrderProperties.getAchBatchCreditTypes().contains(postBatchOrderRequest.getType())) {
            return response;
        }
        String arrangementInternalId = postBatchOrderRequest.getAccount().getArrangementId();
        AccountArrangementItem arrangementItem = arrangementsApi.getArrangementById(arrangementInternalId, true);
        log.info("Checking Arrangement Item Details" + JsonUtil.convertObjectToJson(arrangementItem));
        if ((arrangementItem.getAvailableBalance()
                .doubleValue()) <= Double.valueOf(
                postBatchOrderRequest.getTotalInstructedAmount().getAmount())) {
            response = new PostBatchOrderResponse()
                    .status(BatchStatus.REJECTED)
                    .reasonText("Insufficient Funds").
                    reasonDescription("Instructed amount is greater then availableBalance from the account " + postBatchOrderRequest.getAccount().getArrangementId());
        }
        return response;
    }

    private PostBatchOrderResponse isValidUser(String userExternalId) {
        PostBatchOrderResponse response = null;
        if (!scheduledBatchOrderProperties.isCreatedUserValidCheckEnabled()) {
            return response;
        }
        GetIdentity identityUser = userManagementCommonClient.findIdentityByExternalId(userExternalId);

        if (GetIdentity.StatusEnum.DISABLED.equals(identityUser.getStatus())) {
            response = new PostBatchOrderResponse()
                    .status(BatchStatus.REJECTED)
                    .reasonText("User Inactive").
                    reasonDescription("The User Who created a Batch Order is inactive.");
            return response;
        }
        return response;
    }

    private PostBatchOrderResponse limitConsumption(PostBatchOrderRequest postBatchOrderRequest, String externalUserId) {
        PostBatchOrderResponse response = null;
        if (!scheduledBatchOrderProperties.isLimitChecksEnabled()) {
            return response;
        }
        GetUser getUser = userManagementCommonClient.findUserByExternalId(externalUserId);

        LimitsCheckPostResponseBody responseBody = limitServiceClient.consumeLimit(postBatchOrderRequest, getUser, achBatchDateCalculator.appendYearMonthDate(postBatchOrderRequest.getId()));

        if (null != responseBody && (!CollectionUtils.isEmpty(responseBody.getBreachReport()))) {
            response = new PostBatchOrderResponse()
                    .status(BatchStatus.REJECTED)
                    .reasonText("Limit is breached")
                    .reasonDescription(responseBody.getBreachReport().get(0).getAlias());
        }
        return response;
    }

    public boolean isFirstRecurringPayment(String batchOrderId) {
        LocalDate date = (LocalDate.now()).minusDays(1);
        return (scheduledBatchOrderServiceClient.getScheduledBatchOrderHistoryCount(batchOrderId, date) == 0) ;
    }
}


