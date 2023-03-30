package com.backbase.accelerators.util;

import com.backbase.accelerators.model.Schedule;
import com.backbase.accelerators.resolver.RestrictedDatesResolver;
import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ACHBatchDateCalculator {

    private static final int NUMBER_ONE = 1;

    private static final int NUMBER_ZERO = 0;


    private final ScheduledBatchOrderProperties scheduledBatchOrderProperties;

    private final RestrictedDatesResolver restrictedDatesResolver;

    public long getAchTypeBusinessDays(String batchType) {
        return scheduledBatchOrderProperties.getAchBatchCreditTypes().contains(batchType)
                ? scheduledBatchOrderProperties.getAchBatchCreditMinusBusinessDays() : scheduledBatchOrderProperties.getAchBatchDebitMinusBusinessDays();
    }

    public LocalDate calculateNextExecutionDate(String batchType, LocalDate requestedExecutionDate) {
        long minusBusinessDays = getAchTypeBusinessDays(batchType);
        LocalDate localDate = calculateBusinessDays(minusBusinessDays, requestedExecutionDate);
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isAfter(localDate) || currentDate.isEqual(localDate)) {
            localDate = currentDate;
        }
        return localDate;
    }

    public LocalDate calculateOriginalExecutionDate(String batchType, LocalDate originalExecutionDate, LocalDate startDate) {
        long plusBusinessDays = getAchTypeBusinessDays(batchType);
        if (startDate.isAfter(originalExecutionDate)) {
            return startDate;
        }

        LocalDate achEarlyDate = originalExecutionDate;
        while (plusBusinessDays > NUMBER_ZERO) {
            achEarlyDate = achEarlyDate.plusDays(NUMBER_ONE);

            if (!isRestrictedDate(achEarlyDate)) {
                plusBusinessDays = plusBusinessDays - NUMBER_ONE;
            }
        }
        return achEarlyDate;
    }

    public LocalDate calculateBusinessDays(long businessDays, LocalDate requestedExecutionDate) {
        LocalDate achEarlyDate = requestedExecutionDate;
        while (businessDays > NUMBER_ZERO) {
            achEarlyDate = achEarlyDate.minusDays(NUMBER_ONE);

            if (!isRestrictedDate(achEarlyDate)) {
                businessDays = businessDays - NUMBER_ONE;
            }
        }
        return achEarlyDate;
    }

    private LocalDate calculateNextExecutionDateByFrequency(Schedule schedule, ChronoUnit chronoUnit, long amountToAdd, String batchType, LocalDate nextExecutionDate1) {
        /* Because the next execution date is manipulated based on ACH type, get the Original date it suppose to execute */

        LocalDate recurringDate = calculateOriginalExecutionDate(batchType, nextExecutionDate1, schedule.getStartDate());
        recurringDate = recurringDate.plus(amountToAdd, chronoUnit);
        LocalDate nextExecutionDate = calculateNextExecutionDate(batchType, recurringDate);
        LocalDate endDate = schedule.getEndDate();

        /* check if repetition reached maximum or not */
        Integer repetition = schedule.getRepetition();
        LocalDate startDate = schedule.getStartDate();
        if (repetition != null && repetition.intValue() > NUMBER_ZERO) {
            int value = (int) (repetition.intValue() * amountToAdd);
            LocalDate endExecutionDate = startDate.plus(value - 1L, chronoUnit);
            if (nextExecutionDate.isAfter(endExecutionDate)) {
                return null;
            }
        }
        /* check if the next Execution Date is after the endDte that means no more payments to execute.*/
        if (endDate != null && nextExecutionDate.isAfter(endDate)) {

            nextExecutionDate = null;
        }
        /* Never it keep calculate the next execution Date*/
        return nextExecutionDate;
    }


    /**
     * Determine whether ACH Batch Order can Submit CGI right now or not.
     *
     * @param executionDate
     * @return
     */
    public boolean canSubmitToCGI(LocalDate executionDate) {
         return (LocalDate.now().isAfter(executionDate) || LocalDate.now().isEqual(executionDate));
    }

    private boolean isRestrictedDate(LocalDate executionDate) {
        log.info("Checking if execution date {} falls on the following restricted dates of client which includes week ends as well.",
                executionDate);

        return restrictedDatesResolver.getRestrictedDates()
                .parallelStream()
                .anyMatch(restrictedDate -> restrictedDate.equals(executionDate));
    }

    public LocalDate calculateRecurringNextExecutionDate(Schedule schedule, String batchType, LocalDate nextExecutionDate) {
        log.info("NextExecutionDateUtil :: calculateNextExecutionDate :: schedule : {}", schedule);

        switch (schedule.getFrequency()) {
            case BatchConstants.DAILY:
                return calculateNextExecutionDateByFrequency(schedule, ChronoUnit.DAYS, NUMBER_ONE, batchType, nextExecutionDate);
            case BatchConstants.WEEKLY:
                return calculateNextExecutionDateByFrequency(schedule, ChronoUnit.WEEKS, NUMBER_ONE, batchType, nextExecutionDate);
            case BatchConstants.BIWEEKLY:
                return calculateNextExecutionDateByFrequency(schedule, ChronoUnit.WEEKS, 2, batchType, nextExecutionDate);
            case BatchConstants.MONTHLY:
                return calculateNextExecutionDateByFrequency(schedule, ChronoUnit.MONTHS, NUMBER_ONE, batchType, nextExecutionDate);
            case BatchConstants.QUARTERLY:
                return calculateNextExecutionDateByFrequency(schedule, ChronoUnit.MONTHS, 3, batchType, nextExecutionDate);
            case BatchConstants.YEARLY:
                return calculateNextExecutionDateByFrequency(schedule, ChronoUnit.YEARS, NUMBER_ONE, batchType, nextExecutionDate);
            default:
                return null;
        }

    }

    public boolean hasCutOffTimePassed(LocalTime cutOffTime) {
        return LocalTime.now().isAfter(cutOffTime);
    }

    public String appendYearMonthDate(String batchOrderId) {
        LocalDate date = LocalDate.now();
        return batchOrderId+date.getYear()+ StringUtils.EMPTY +date.getMonth().getValue()+StringUtils.EMPTY+date.getDayOfMonth();
    }
}
