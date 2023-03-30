package com.backbase.accelerators.service;

import com.backbase.accelerators.mapper.ScheduledBatchOrderMapper;
import com.backbase.dbs.batch.inbound.v2.service.model.GetBatchOrderResponse;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.scheduler.batch.scheduled.v1.service.model.ScheduledBatchOrderItem;
import com.backbase.accelerators.client.BatchOrderInboundClient;
import com.backbase.accelerators.client.ScheduledBatchOrderServiceClient;
import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
class ScheduledBatchPaymentExecutorServiceTest {

    @InjectMocks
    ScheduledBatchPaymentExecutorService scheduledBatchPaymentExecutorService;

    @Mock
    ScheduledBatchOrderServiceClient scheduledBatchOrderServiceClient;

    @Mock
    BatchOrderInboundClient batchOrderInboundClient;

    @Mock
    ScheduledBatchOrderMapper scheduledBatchOrderMapper;

    @Mock
    AchBatchOrderService achBatchOrderService;

    @Mock
    ScheduledBatchOrderProperties scheduledProperties;

    @Test
    void execute(){
        List<ScheduledBatchOrderItem> itemList = List.of(new ScheduledBatchOrderItem());
        Mockito.when(scheduledBatchOrderServiceClient.getScheduledBatchOrders(Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(itemList);
        Mockito.when(scheduledProperties.getMaxFailureCountPerSchedule()).thenReturn(1);
        Mockito.when(batchOrderInboundClient.getBatchOrderResponse(Mockito.any())).thenReturn(new GetBatchOrderResponse());
        Mockito.when(scheduledBatchOrderMapper.toPostBatchOrderRequest(Mockito.any())).thenReturn(new PostBatchOrderRequest());
        Mockito.when(achBatchOrderService.processSchedulerBatchOrder(Mockito.any(), Mockito.any())).thenReturn(false);
        scheduledBatchPaymentExecutorService.execute();

        Mockito.verify(achBatchOrderService, Mockito.times(1)).processSchedulerBatchOrder(Mockito.any(), Mockito.any());
    }

}