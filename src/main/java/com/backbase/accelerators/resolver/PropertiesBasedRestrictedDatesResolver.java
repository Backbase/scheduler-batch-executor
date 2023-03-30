package com.backbase.accelerators.resolver;


import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduled-batch.execution-date-validation", name = "restrictedDatesResolutionStrategy", havingValue = "PROPERTIES_BASED", matchIfMissing = true)
public class PropertiesBasedRestrictedDatesResolver implements RestrictedDatesResolver {

    private final ScheduledBatchOrderProperties scheduledPaymentOrderProperties;

    @Override
    public List<LocalDate> getRestrictedDates() {
        return scheduledPaymentOrderProperties.getExecutionDateValidation()
                .getRestrictedDates();
    }
}
