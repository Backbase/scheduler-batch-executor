package com.backbase.accelerators.client;

import com.backbase.dbs.batch.service.v2.service.api.BatchOrdersApi;
import com.backbase.dbs.batch.service.v2.service.model.GetBaseBatchOrderResponse;
import com.backbase.dbs.user.manager.models.v2.GetUser;
import com.backbase.accelerators.common.client.UserManagementCommonClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class is used to connect with OOTB batch order service-api methods.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchOrderServiceClient {

    private final BatchOrdersApi serviceBatchOrdersApi;

    private final UserManagementCommonClient userManagementCommonClient;

    public GetBaseBatchOrderResponse getBatchOrder(String batchOrderId) {
        return serviceBatchOrdersApi.getBatchOrderById(batchOrderId).block();
    }

    public String getCreatedUserForBatchOrder(String batchOrderId) {
        GetBaseBatchOrderResponse respone = getBatchOrder(batchOrderId);
        GetUser user = userManagementCommonClient.findUserByInternalId(respone.getCreatedBy());
        return user.getExternalId();
    }
}
