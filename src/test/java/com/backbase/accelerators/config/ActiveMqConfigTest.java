package com.backbase.accelerators.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ActiveMqConfigTest {

    @InjectMocks
    ActiveMqConfig activeMqConfig;

    @Mock
    ActiveMQProperties activeMQProperties;

    @Test
    void createConnectionFactoryBean(){

        Mockito.when(activeMQProperties.getBrokerUrl()).thenReturn("http://sample");
        Mockito.when(activeMQProperties.getUser()).thenReturn("user");
        Mockito.when(activeMQProperties.getPassword()).thenReturn("****");

        ActiveMQConnectionFactory connectionFactory = activeMqConfig.connectionFactory();

        Assertions.assertNotNull(connectionFactory);

    }

    @Test
    void createJmsTemplateBean(){
        Mockito.when(activeMQProperties.getBrokerUrl()).thenReturn("http://sample");
        Mockito.when(activeMQProperties.getUser()).thenReturn("user");
        Mockito.when(activeMQProperties.getPassword()).thenReturn("****");

        Assertions.assertNotNull(activeMqConfig.jmsTemplate());
    }

    @Test
    void createJmsListenerContainerFactoryBean(){
        Mockito.when(activeMQProperties.getBrokerUrl()).thenReturn("http://sample");
        Mockito.when(activeMQProperties.getUser()).thenReturn("user");
        Mockito.when(activeMQProperties.getPassword()).thenReturn("****");

        Assertions.assertNotNull(activeMqConfig.jmsListenerContainerFactory());
    }

    @Test
    void createMessageConverterBean(){
        Assertions.assertNotNull(activeMqConfig.messageConverter());
    }
}