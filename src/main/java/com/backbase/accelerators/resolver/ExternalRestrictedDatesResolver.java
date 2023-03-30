package com.backbase.accelerators.resolver;

import com.backbase.accelerators.client.BlackoutDatesServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduled-batch.execution-date-validation", name = "restrictedDatesResolutionStrategy", havingValue = "EXTERNAL", matchIfMissing = false)
public class ExternalRestrictedDatesResolver implements RestrictedDatesResolver {

    private final BlackoutDatesServiceClient blackoutDatesServiceClient;

    private static final String ACH_PMT_TYPE = "ACH";

    @Override
    public List<LocalDate> getRestrictedDates() {
        return blackoutDatesServiceClient.getBlackoutDates(ACH_PMT_TYPE).getBlackoutDates();
    }
}
