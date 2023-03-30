package com.backbase.accelerators.listener;


import com.backbase.dbs.batch.outbound.v2.service.model.PostBatchOrderRequest;
import com.backbase.accelerators.service.AchBatchOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
@Component
@Slf4j
@RequiredArgsConstructor
public class ACHBatchOrderMessageListener {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final AchBatchOrderService achBatchOrderService;

    @JmsListener(destination = "${scheduled-batch.achBatchQueueName}")
    public void receiveMessageActiveMQ(final Message<String> message) {
        log.debug("==== Listening the message {} from queue ", message);
        PostBatchOrderRequest postBatchOrderRequest = null;
        try {
            //Added thread sleep to avoid the Listener code catch on the same session.
            if (StringUtils.isNotEmpty(message.getPayload())) {
                postBatchOrderRequest = objectMapper.readValue(StringUtils.normalizeSpace(message.getPayload()), PostBatchOrderRequest.class);
                achBatchOrderService.processClientBatchOrder(postBatchOrderRequest);
            }
            log.debug("==== Listening the message {} from queue completed ", message);
        } catch (Exception ex) {
            log.error(ExceptionUtils.getRootCauseMessage(ex));
        }
    }
}
