package com.backbase.accelerators.client;


import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.accessgroups.models.v2.GetServiceAgreement;
import com.backbase.dbs.accessgroups.models.v2.LegalEntityItem;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.user.manager.models.v2.GetUser;
import com.backbase.limit.v2.service.api.LimitsServiceApi;
import com.backbase.limit.v2.service.model.ConsumptionRollbackPostRequestBody;
import com.backbase.limit.v2.service.model.LimitsCheckPostRequestBody;
import com.backbase.limit.v2.service.model.LimitsCheckPostResponseBody;
import com.backbase.limit.v2.service.model.PaymentState;
import com.backbase.accelerators.common.client.LegalEntityClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * This class is used to connect with OOTB limit service service-api methods.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimitServiceClient {

    private final LimitsServiceApi limitsServiceApi;

    private final LegalEntityClient legalEntityClient;


    public void postLimitConsumptionRollback(String batchorderId) {
        ConsumptionRollbackPostRequestBody consumptionRollbackPostResponseBody = new ConsumptionRollbackPostRequestBody();
        consumptionRollbackPostResponseBody.paymentRef(batchorderId);
        try {
            limitsServiceApi.postConsumptionRollback(consumptionRollbackPostResponseBody);
        } catch (NotFoundException exception) {
            /**
             *  Do nothing when there is an error from limit service while rollback the limits.
             */
            log.error("Error While back the limits for the payment Id {}", batchorderId);
        }
    }

    public LimitsCheckPostResponseBody consumeLimit(PostBatchOrderRequest postBatchOrderRequest, GetUser user, String recurringBatchOrderId) {
        LegalEntityItem legalEntityItem = legalEntityClient.findLegalEntityById(user.getLegalEntityId());
        GetServiceAgreement serviceAgreement = legalEntityClient.findServiceAgreementByExternalId(legalEntityItem.getExternalId());

        LimitsCheckPostRequestBody limitsRequest = new LimitsCheckPostRequestBody();
        limitsRequest.refNo(recurringBatchOrderId)
                .effectiveDate(OffsetDateTime.now());
        limitsRequest.arrangementId(postBatchOrderRequest.getAccount().getArrangementId())
                .currency(postBatchOrderRequest.getTotalCreditInstructedAmount().getCurrencyCode())
                .amount(BigDecimal.valueOf(Double.valueOf(postBatchOrderRequest.getTotalInstructedAmount().getAmount())))
                .paymentType(postBatchOrderRequest.getType())
                .userBBID(user.getId())
                .state(PaymentState.APPROVED);
        limitsRequest.setServiceAgreementId(serviceAgreement.getId());

        limitsRequest.setLegalEntityId(legalEntityItem.getId());

        return limitsServiceApi.postLimitsCheck(limitsRequest);


    }

}
