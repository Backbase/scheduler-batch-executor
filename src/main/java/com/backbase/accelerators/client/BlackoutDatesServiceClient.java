package com.backbase.accelerators.client;


import com.backbase.accelerators.model.RestrictedDates;
import com.backbase.vantage.service.model.v1.blackout.dates.BlackoutDates;
import com.backbase.vantage.service.model.v1.blackout.dates.ValidateBlackoutDate;
import com.backbase.vantage.service.v1.blackout.dates.BlackoutDatesServiceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to connect with custom created blackout dates service-api methods.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlackoutDatesServiceClient {

    private final BlackoutDatesServiceApi blackoutDatesServiceApi;


    private static final String CST_TIME = "America/Chicago";
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/d/yyyy");


    @Cacheable(value = "apimblackoutDates", unless = "#result.blackoutDates==null or #result.blackoutDates.isEmpty() or #result.cutOffTime==null or #result.cutOffTime.isEmpty()")
    public RestrictedDates getBlackoutDates(String paymentType) {
        RestrictedDates restrictedDates = new RestrictedDates();
        log.info("apimblackoutDates cache is empty. Hence trying to read from the blackoutdate Service.");
        List<LocalDate> blackoutDatesList = new ArrayList<>();
        try {
            BlackoutDates blackoutDates = blackoutDatesServiceApi.getBlackoutDates(paymentType).block();
            if (blackoutDates != null && !blackoutDates.getBlackoutDates().isEmpty()) {

                blackoutDatesList.addAll(blackoutDates.getBlackoutDates()
                        .stream().map(date -> LocalDate.parse(date, dateTimeFormatter)).toList());
            }
            restrictedDates.setBlackoutDates(blackoutDatesList);
            restrictedDates.setCutOffTime(blackoutDates.getCutoffTimes().getAch());
        } catch (Exception ex) {
            log.error("Error While getting blackoutdates  {}", paymentType);
            log.error(ExceptionUtils.getRootCauseMessage(ex));
            throw ex;
        }
        return restrictedDates;
    }
}
