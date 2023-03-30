package com.backbase.accelerators.client;

import com.backbase.accelerators.model.RestrictedDates;
import com.backbase.vantage.service.model.v1.blackout.dates.BlackoutDates;
import com.backbase.vantage.service.model.v1.blackout.dates.BlackoutDatesCutoffTimes;
import com.backbase.vantage.service.v1.blackout.dates.BlackoutDatesServiceApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
class BlackoutDatesServiceClientTest {

    @InjectMocks
    BlackoutDatesServiceClient blackoutDatesServiceClient;

    @Mock
    BlackoutDatesServiceApi blackoutDatesServiceApi;

    @Test
    void getBlackoutDatesGivenPaymentType(){

        List blackoutDatesList = new ArrayList();
        blackoutDatesList.add("12/25/2023");
        blackoutDatesList.add("07/14/2023");
        BlackoutDates blackoutDates = new BlackoutDates()
                .blackoutDates(blackoutDatesList)
                .cutoffTimes(new BlackoutDatesCutoffTimes().ach("ach"));
        Mockito.when(blackoutDatesServiceApi.getBlackoutDates(Mockito.any()))
                .thenReturn(Mono.just(blackoutDates));

        RestrictedDates restrictedDates = blackoutDatesServiceClient.getBlackoutDates("paymentType");

        Assertions.assertNotNull(restrictedDates);
        Assertions.assertEquals(2, restrictedDates.getBlackoutDates().size());
    }

    @Test
    void getBlackoutDatesThrowsException(){

        Mockito.when(blackoutDatesServiceApi.getBlackoutDates(Mockito.any()))
                .thenThrow(RestClientException.class);

        Assertions.assertThrows(RestClientException.class,
                () -> blackoutDatesServiceClient.getBlackoutDates("paymentType"));
    }

}