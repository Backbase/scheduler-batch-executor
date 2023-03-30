package com.backbase.accelerators.service;

import com.backbase.arrangement.integration.listener.client.v2.persistence.arrangements.ArrangementsApi;
import com.backbase.dbs.batch.outbound.v2.service.model.*;
import com.backbase.dbs.user.manager.models.v2.GetIdentity;
import com.backbase.limit.v2.service.model.BreachReport;
import com.backbase.limit.v2.service.model.LimitsCheckPostResponseBody;
import com.backbase.pandp.arrangement.query.rest.spec.v2.arrangements.AccountArrangementItem;
import com.backbase.scheduler.batch.scheduled.v1.service.model.ScheduledBatchOrderItem;
import com.backbase.accelerators.client.LimitServiceClient;
import com.backbase.accelerators.client.ScheduledBatchOrderServiceClient;
import com.backbase.accelerators.common.client.UserManagementCommonClient;
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

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ACHBatchValidationServiceTest {

    @InjectMocks
    ACHBatchValidationService achBatchValidationService;

    @Mock
    ScheduledBatchOrderProperties scheduledBatchOrderProperties;

    @Mock
    ArrangementsApi arrangementsApi;

    @Mock
    UserManagementCommonClient userManagementCommonClient;

    @Mock
    LimitServiceClient limitServiceClient;

    @Mock
    ScheduledBatchOrderServiceClient scheduledBatchOrderServiceClient;

    @Mock
    ACHBatchDateCalculator achBatchDateCalculator;

    @Test
    void validateBatchOrderGivenBalanceCheckNotEnabledAndLimitChecksEnabledNotEnableAndCreatedUserValidCheckIsNotEnabled(){
        Mockito.when(scheduledBatchOrderProperties.isBalanceCheckEnabled()).thenReturn(false);
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderServiceClient.getScheduledBatchOrderHistoryCount(Mockito.any(), Mockito.any()))
                .thenReturn(1);
        Mockito.when(scheduledBatchOrderProperties.isCreatedUserValidCheckEnabled()).thenReturn(false);
        Mockito.when(scheduledBatchOrderProperties.isLimitChecksEnabled()).thenReturn(false);

        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id").type("debit");
        ScheduledBatchOrderItem scheduledBatchOrderItem = new ScheduledBatchOrderItem().batchOrderId("batch-id")
                .pmtMode(BatchConstants.PMT_MODE_RECURRING);

        Assertions.assertNull(achBatchValidationService.validateBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem));
    }

    @Test
    void validateBatchOrderGivenBalanceCheckIsEnabled(){
        Mockito.when(scheduledBatchOrderProperties.isBalanceCheckEnabled()).thenReturn(true);
        AccountArrangementItem arrangementItem = new AccountArrangementItem();
        arrangementItem.setAvailableBalance(BigDecimal.ZERO);
        Mockito.when(arrangementsApi.getArrangementById(Mockito.any(), Mockito.any())).thenReturn(arrangementItem);

        PostBatchOrderRequest postBatchOrderRequest =
                new PostBatchOrderRequest().id("id").type("debit")
                        .account(new OriginatorAccountIdentification().arrangementId("arr-id"))
                        .totalInstructedAmount(new Currency().amount("10"));
        ScheduledBatchOrderItem scheduledBatchOrderItem = new ScheduledBatchOrderItem().batchOrderId("batch-id")
                .pmtMode(BatchConstants.PMT_MODE_RECURRING);

        PostBatchOrderResponse postBatchOrderResponse = achBatchValidationService.validateBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem);

        Assertions.assertNull(postBatchOrderResponse);
    }

    @Test
    void validateBatchOrderGivenCreatedUserValidCheckEnabled(){
        Mockito.when(scheduledBatchOrderProperties.isBalanceCheckEnabled()).thenReturn(false);
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderServiceClient.getScheduledBatchOrderHistoryCount(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(scheduledBatchOrderProperties.isCreatedUserValidCheckEnabled()).thenReturn(true);

        GetIdentity identityUser = new GetIdentity().status(GetIdentity.StatusEnum.DISABLED);
        Mockito.when(userManagementCommonClient.findIdentityByExternalId(Mockito.any())).thenReturn(identityUser);

        PostBatchOrderRequest postBatchOrderRequest =
                new PostBatchOrderRequest().id("id").type("debit")
                        .account(new OriginatorAccountIdentification().arrangementId("arr-id"))
                        .totalInstructedAmount(new Currency().amount("10"));
        ScheduledBatchOrderItem scheduledBatchOrderItem = new ScheduledBatchOrderItem().batchOrderId("batch-id")
                .pmtMode(BatchConstants.PMT_MODE_RECURRING);

        PostBatchOrderResponse postBatchOrderResponse = achBatchValidationService.validateBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem);

        Assertions.assertNotNull(postBatchOrderResponse);
        Assertions.assertEquals(BatchStatus.REJECTED, postBatchOrderResponse.getStatus());
        Assertions.assertEquals("User Inactive", postBatchOrderResponse.getReasonText());
    }

    @Test
    void validateBatchOrderGivenCreatedUserValidCheckEnabledAndInActiveIdentity(){
        Mockito.when(scheduledBatchOrderProperties.isBalanceCheckEnabled()).thenReturn(false);
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderServiceClient.getScheduledBatchOrderHistoryCount(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(scheduledBatchOrderProperties.isCreatedUserValidCheckEnabled()).thenReturn(true);

        GetIdentity identityUser = new GetIdentity().status(GetIdentity.StatusEnum.INACTIVE);
        Mockito.when(userManagementCommonClient.findIdentityByExternalId(Mockito.any())).thenReturn(identityUser);

        PostBatchOrderRequest postBatchOrderRequest =
                new PostBatchOrderRequest().id("id").type("debit")
                        .account(new OriginatorAccountIdentification().arrangementId("arr-id"))
                        .totalInstructedAmount(new Currency().amount("10"));
        ScheduledBatchOrderItem scheduledBatchOrderItem = new ScheduledBatchOrderItem().batchOrderId("batch-id")
                .pmtMode(BatchConstants.PMT_MODE_RECURRING);

        PostBatchOrderResponse postBatchOrderResponse = achBatchValidationService.validateBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem);

        Assertions.assertNull(postBatchOrderResponse);
    }

    @Test
    void validateBatchOrderGivenLimitChecksEnabled(){
        Mockito.when(scheduledBatchOrderProperties.isBalanceCheckEnabled()).thenReturn(false);
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderServiceClient.getScheduledBatchOrderHistoryCount(Mockito.any(), Mockito.any()))
                .thenReturn(1);
        Mockito.when(scheduledBatchOrderProperties.isCreatedUserValidCheckEnabled()).thenReturn(false);
        Mockito.when(scheduledBatchOrderProperties.isLimitChecksEnabled()).thenReturn(true);

        LimitsCheckPostResponseBody responseBody = new LimitsCheckPostResponseBody();
        responseBody.setBreachReport(List.of(new BreachReport()));
        Mockito.when(limitServiceClient.consumeLimit(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(responseBody);

        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id").type("debit");
        ScheduledBatchOrderItem scheduledBatchOrderItem = new ScheduledBatchOrderItem().batchOrderId("batch-id")
                .pmtMode(BatchConstants.PMT_MODE_RECURRING);

        PostBatchOrderResponse postBatchOrderResponse = achBatchValidationService.validateBatchOrder(postBatchOrderRequest, scheduledBatchOrderItem);
        Assertions.assertNotNull(postBatchOrderResponse);
        Assertions.assertEquals(BatchStatus.REJECTED, postBatchOrderResponse.getStatus());
        Assertions.assertEquals("Limit is breached", postBatchOrderResponse.getReasonText());
    }


}