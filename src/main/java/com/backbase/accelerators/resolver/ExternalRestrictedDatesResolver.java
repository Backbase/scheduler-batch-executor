package com.backbase.accelerators.resolver;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduled-batch.execution-date-validation", name = "restrictedDatesResolutionStrategy", havingValue = "EXTERNAL", matchIfMissing = false)
public class ExternalRestrictedDatesResolver implements RestrictedDatesResolver {


    private static final String ACH_PMT_TYPE = "ACH";

    /* Implement this method if we blackout dates are coming from third party API*/
    @Override
    public List<LocalDate> getRestrictedDates() {
        return Arrays.asList();
    }
}
