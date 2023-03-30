package com.backbase.accelerators.listener;

import com.backbase.accelerators.service.AchBatchOrderService;
import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ACHBatchOrderMessageListenerTest {

    @InjectMocks
    ACHBatchOrderMessageListener achBatchOrderMessageListener;

    @Mock
    AchBatchOrderService achBatchOrderService;

    @Mock
    Message<String> message;

    @Test
    void receiveMessageActiveMQ() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PostBatchOrderRequest postBatchOrderRequest = new PostBatchOrderRequest().id("id").type("type");
        Mockito.when(message.getPayload()).thenReturn(objectMapper.writeValueAsString(postBatchOrderRequest));
        achBatchOrderMessageListener.receiveMessageActiveMQ(message);

        Mockito.verify(achBatchOrderService, Mockito.times(1)).processClientBatchOrder(Mockito.any());
    }

    @Test
    void receiveMessageActiveMQThrowsException() {

        Mockito.when(message.getPayload()).thenReturn("payload");

        achBatchOrderMessageListener.receiveMessageActiveMQ(message);
        Mockito.verify(achBatchOrderService, Mockito.times(0)).processClientBatchOrder(Mockito.any());
    }

}