package com.backbase.accelerators.resolver;

import com.backbase.accelerators.client.BlackoutDatesServiceClient;
import com.backbase.accelerators.model.RestrictedDates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ExternalRestrictedDatesResolverTest {

    @InjectMocks
    ExternalRestrictedDatesResolver externalRestrictedDatesResolver;

    @Mock
    BlackoutDatesServiceClient blackoutDatesServiceClient;

    @Test
    void getRestrictedDates(){
        RestrictedDates dates = new RestrictedDates();
        dates.setBlackoutDates(List.of(LocalDate.EPOCH));
        Mockito.when(blackoutDatesServiceClient.getBlackoutDates(Mockito.any())).thenReturn(dates);

        List<LocalDate> localDates = externalRestrictedDatesResolver.getRestrictedDates();
        Assertions.assertNotNull(localDates);
        Assertions.assertEquals(1, localDates.size());
    }

}