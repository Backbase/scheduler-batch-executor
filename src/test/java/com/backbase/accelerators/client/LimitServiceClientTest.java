package com.backbase.accelerators.client;

import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.accessgroups.models.v2.GetServiceAgreement;
import com.backbase.dbs.accessgroups.models.v2.LegalEntityItem;
import com.backbase.dbs.batch.outbound.v2.service.model.Currency;
import com.backbase.dbs.batch.outbound.v2.service.model.OriginatorAccountIdentification;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.user.manager.models.v2.GetUser;
import com.backbase.limit.v2.service.api.LimitsServiceApi;
import com.backbase.limit.v2.service.model.LimitsCheckPostResponseBody;
import com.backbase.limit.v2.service.model.Payment;
import com.backbase.accelerators.common.client.LegalEntityClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
class LimitServiceClientTest {

    @InjectMocks
    LimitServiceClient limitServiceClient;

    @Mock
    LimitsServiceApi limitsServiceApi;

    @Mock
    LegalEntityClient legalEntityClient;

    @Test
    void postLimitConsumptionRollback(){
       limitServiceClient.postLimitConsumptionRollback("id");
       Mockito.verify(limitsServiceApi, Mockito.times(1)).postConsumptionRollback(Mockito.any());
    }

    @Test
    void postLimitConsumptionRollbackThrowsException(){

        Mockito.when(limitsServiceApi.postConsumptionRollback(Mockito.any()))
                .thenThrow(NotFoundException.class);

        limitServiceClient.postLimitConsumptionRollback("batchOrderId");

        Mockito.verify(limitsServiceApi, Mockito.times(1)).postConsumptionRollback(Mockito.any());
    }

    @Test
    void consumeLimit(){

        LegalEntityItem legalEntityItem = new LegalEntityItem().id("entity-id");
        Mockito.when(legalEntityClient.findLegalEntityById(Mockito.any())).thenReturn(legalEntityItem);

        GetServiceAgreement getServiceAgreement = new GetServiceAgreement().id("agreement-id");
        Mockito.when(legalEntityClient.findServiceAgreementByExternalId(Mockito.any())).thenReturn(getServiceAgreement);

        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest()
                .account(new OriginatorAccountIdentification().arrangementId("arr-id"))
                .totalCreditInstructedAmount(new Currency().currencyCode("USD"))
                .totalInstructedAmount(new Currency().amount("30"))
                .type("type");

        GetUser user = new GetUser().id("user_id");

        LimitsCheckPostResponseBody limitsCheckPostResponseBody = new LimitsCheckPostResponseBody();
        limitsCheckPostResponseBody.setPayment(new Payment().amount(BigDecimal.TEN));

        Mockito.when(limitsServiceApi.postLimitsCheck(Mockito.any())).thenReturn(limitsCheckPostResponseBody);

        LimitsCheckPostResponseBody response = limitServiceClient.consumeLimit(postBatchOrderRequest, user, "id");

        Assertions.assertNotNull(response);
        Assertions.assertEquals(BigDecimal.TEN, response.getPayment().getAmount());
    }

}