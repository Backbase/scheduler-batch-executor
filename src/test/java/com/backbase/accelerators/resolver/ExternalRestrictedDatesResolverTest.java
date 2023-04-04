package com.backbase.accelerators.resolver;

import com.backbase.accelerators.model.RestrictedDates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ExternalRestrictedDatesResolverTest {

    @InjectMocks
    ExternalRestrictedDatesResolver externalRestrictedDatesResolver;



    @Test
    void getRestrictedDates(){
        RestrictedDates dates = new RestrictedDates();
        dates.setBlackoutDates(List.of(LocalDate.EPOCH));

        List<LocalDate> localDates = externalRestrictedDatesResolver.getRestrictedDates();
        Assertions.assertNotNull(localDates);
        Assertions.assertEquals(0, localDates.size());
    }

}