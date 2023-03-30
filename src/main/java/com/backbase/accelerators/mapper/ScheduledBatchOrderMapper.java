package com.backbase.accelerators.mapper;

import com.backbase.dbs.batch.inbound.v2.service.model.GetBatchOrderResponse;
import com.backbase.dbs.batch.inbound.v2.service.model.SchemeNames;
import com.backbase.dbs.batch.outbound.v2.service.model.IntegrationSchemeNames;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderResponse;
import com.backbase.scheduler.batch.scheduled.v1.service.model.PostScheduledBatchOrderHistoryRequest;
import com.backbase.scheduler.batch.scheduled.v1.service.model.PostScheduledBatchOrderRequest;
import com.backbase.scheduler.batch.scheduled.v1.service.model.PutScheduledBatchOrderRequest;
import com.backbase.accelerators.util.BatchConstants;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.Map;

import static java.util.Objects.isNull;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScheduledBatchOrderMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "batchOrderId", source = "scheduledBatchOrderRequest.id")
    @Mapping(target = "reqExecutionDate", source = "scheduledBatchOrderRequest.requestedExecutionDate")
    @Mapping(target = "pmtType", source = "scheduledBatchOrderRequest.type")

    @Mapping(target = "accountNumber", source = "scheduledBatchOrderRequest.account.identification.identification")
    @Mapping(target = "nextExecutionDate", source = "nextExecutionDate")
    @Mapping(target = "status", source = "batchStatus")
    @Mapping(target = "createdBy", source = "createdBy")
    PostScheduledBatchOrderRequest toPostScheduledBatchOrderRequest(PostBatchOrderRequest scheduledBatchOrderRequest,
                                                                    LocalDate nextExecutionDate, String batchStatus, String createdBy);

    @Mapping(target = "batchOrderId", source = "batchOrderId")
    @Mapping(target = "status", source = "batchStatus")
    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "errorDescription", source = "response.reasonDescription")
    @Mapping(target = "reasonText", source = "response.reasonText")
    PostScheduledBatchOrderHistoryRequest toPostScheduledBatchOrderHistoryRequest(String batchOrderId, String batchStatus, PostBatchOrderResponse response, String fileName);


    @Mapping(target = "status", source = "status")
    @Mapping(target = "batchOrderId", source = "batchOrderId")
    @Mapping(target = "errorDescription", source = "response.reasonDescription")
    @Mapping(target = "reasonText", source = "response.reasonText")
    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "nextExecutionDate", source = "nextExecutionDate")
    PutScheduledBatchOrderRequest toPutScheduledBatchOrderRequest(String batchOrderId, String status, PostBatchOrderResponse response, LocalDate nextExecutionDate, String fileName);


    @AfterMapping
    default void recurringFieldsMapping(PostBatchOrderRequest source, @MappingTarget PostScheduledBatchOrderRequest target) {
        target.setPmtMode(BatchConstants.PMT_MODE_SINGLE);
        if (!isNull(source.getAdditions())) {
            Map<String,String> additions = source.getAdditions();

            if (null != additions.get(BatchConstants.RECUR_FREQUENCY)) {
                target.setFrequency(additions.get(BatchConstants.RECUR_FREQUENCY));
                if (!additions.get(BatchConstants.RECUR_FREQUENCY).equals(BatchConstants.RECUR_FREQUENCY_TYPE_ONCE)) {
                    target.setPmtMode(BatchConstants.PMT_MODE_RECURRING);
                }
            }
            if (null != additions.get(BatchConstants.RECUR_START_DATE)) {
                target.setStartDate(LocalDate.parse(additions.get(BatchConstants.RECUR_START_DATE)));
            }
            if (null != additions.get(BatchConstants.RECUR_END_DATE)) {
                target.setEndDate(LocalDate.parse(additions.get(BatchConstants.RECUR_END_DATE)));
            }
            if (null != additions.get(BatchConstants.RECUR_REPETITION)) {
                target.setRepetition(Integer.valueOf(additions.get(BatchConstants.RECUR_REPETITION)));
            }
            if (null != additions.get(BatchConstants.RECUR_WHEN_EXECUTE)) {
                target.setWhenExecute(Integer.valueOf(additions.get(BatchConstants.RECUR_WHEN_EXECUTE)));
            }
            if (null != additions.get(BatchConstants.RECUR_END_TYPE)) {
                target.setEndType(additions.get(BatchConstants.RECUR_END_TYPE));
            }
        }
    }


    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "creditDebitMixedIndicator", source = "creditDebitMixedIndicator")
    @Mapping(target = "entryClass", source = "entryClass")
    @Mapping(target = "totalTransactionsCount", source = "totalTransactionsCount")
    @Mapping(target = "totalCreditTransactionsCount", source = "totalCreditTransactionsCount")
    @Mapping(target = "totalDebitTransactionsCount", source = "totalDebitTransactionsCount")
    @Mapping(target = "totalInstructedAmount", source = "totalInstructedAmount")
    @Mapping(target = "totalCreditInstructedAmount", source = "totalCreditInstructedAmount")
    @Mapping(target = "totalDebitInstructedAmount", source = "totalDebitInstructedAmount")
    @Mapping(target = "account.name", source = "account.name")
    @Mapping(target = "account.retrieved", source = "account.retrieved")
    @Mapping(target = "account.additions", source = "account.additions")
    @Mapping(target = "account.arrangementId", source = "account.arrangementId")
    @Mapping(target = "account.identification.identification", source = "account.identification.identification")
    @Mapping(target = "account.identification.additions", source = "account.identification.additions")
    @Mapping(target = "companyId", source = "companyId")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "bankBranchCode", source = "bankBranchCode")
    @Mapping(target = "requestedExecutionDate", source = "requestedExecutionDate")
    @Mapping(target = "confidentialType", source = "confidentialType")
    @Mapping(target = "additions", source = "additions")
    PostBatchOrderRequest toPostBatchOrderRequest(GetBatchOrderResponse source);

    // Here we are mapping the extra source enums to mappingConstants.null.
    @ValueMapping(source = "IBAN", target = "IBAN")
    @ValueMapping(source = "BBAN", target = "BBAN")
    @ValueMapping(source = "ID", target = MappingConstants.NULL)
    @ValueMapping(source = "EXTERNAL_ID", target = MappingConstants.NULL)
    @ValueMapping(source = "EMAIL", target = MappingConstants.NULL)
    @ValueMapping(source = "MOBILE", target = MappingConstants.NULL)
    IntegrationSchemeNames toIntegrationSchemeNames(SchemeNames schemeNames);

    @AfterMapping
    default void afterMapping(@MappingTarget PostBatchOrderRequest target, GetBatchOrderResponse source){

        target.getAccount().getIdentification().setSchemeName(IntegrationSchemeNames.fromValue
                (source.getAccount().getIdentification().getSchemeName().getValue()));
    }



}
