package com.backbase.accelerators.resolver;

import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
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
class PropertiesBasedRestrictedDatesResolverTest {

    @InjectMocks
    PropertiesBasedRestrictedDatesResolver propertiesBasedRestrictedDatesResolver;

    @Mock
    ScheduledBatchOrderProperties scheduledBatchOrderProperties;

    @Test
    void getRestrictedDates(){

        ScheduledBatchOrderProperties.ExecutionDateValidationProperties executionDateValidationProperties
                = new ScheduledBatchOrderProperties.ExecutionDateValidationProperties();
        executionDateValidationProperties.setRestrictedDates(List.of(LocalDate.EPOCH));
        Mockito.when(scheduledBatchOrderProperties.getExecutionDateValidation()).thenReturn(executionDateValidationProperties);

        List<LocalDate> localDates = propertiesBasedRestrictedDatesResolver.getRestrictedDates();
        Assertions.assertNotNull(localDates);
        Assertions.assertEquals(1, localDates.size());
    }

}