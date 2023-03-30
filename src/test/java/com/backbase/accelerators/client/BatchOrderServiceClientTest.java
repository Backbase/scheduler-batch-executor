package com.backbase.accelerators.client;

import com.backbase.dbs.batch.service.v2.service.api.BatchOrdersApi;
import com.backbase.dbs.batch.service.v2.service.model.GetBaseBatchOrderResponse;
import com.backbase.dbs.user.manager.models.v2.GetUser;
import com.backbase.accelerators.common.client.UserManagementCommonClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
class BatchOrderServiceClientTest {

    @InjectMocks
    BatchOrderServiceClient batchOrderServiceClient;

    @Mock
    BatchOrdersApi serviceBatchOrdersApi;

    @Mock
    UserManagementCommonClient userManagementCommonClient;

    @Test
    void getBatchOrder(){
        GetBaseBatchOrderResponse getBaseBatchOrderResponse =
                new GetBaseBatchOrderResponse().id("id").approvalId("appId");
        Mockito.when(serviceBatchOrdersApi.getBatchOrderById(Mockito.any()))
                .thenReturn(Mono.just(getBaseBatchOrderResponse));

        GetBaseBatchOrderResponse response = batchOrderServiceClient.getBatchOrder("id");

        Assertions.assertNotNull(response);
        Assertions.assertEquals("id", response.getId());
        Assertions.assertEquals("appId", response.getApprovalId());
    }

    @Test
    void getCreatedUserForBatchOrder(){
        GetBaseBatchOrderResponse getBaseBatchOrderResponse =
                new GetBaseBatchOrderResponse().id("id").approvalId("appId");
        Mockito.when(serviceBatchOrdersApi.getBatchOrderById(Mockito.any()))
                .thenReturn(Mono.just(getBaseBatchOrderResponse));

        GetUser user = new GetUser().externalId("ext-id");
        Mockito.when(userManagementCommonClient.findUserByInternalId(Mockito.any()))
                .thenReturn(user);

        String response = batchOrderServiceClient.getCreatedUserForBatchOrder("id");

        Assertions.assertNotNull(response);
        Assertions.assertEquals("ext-id", response);
    }

}