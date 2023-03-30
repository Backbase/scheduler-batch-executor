package com.backbase.accelerators.resolver;

import java.time.LocalDate;
import java.util.List;

public interface RestrictedDatesResolver {

    List<LocalDate> getRestrictedDates();
}
