package com.backbase.accelerators.util;

import com.backbase.accelerators.resolver.RestrictedDatesResolver;
import com.backbase.accelerators.config.ScheduledBatchOrderProperties;
import com.backbase.accelerators.model.Schedule;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ACHBatchDateCalculatorTest {

    @InjectMocks
    ACHBatchDateCalculator achBatchDateCalculator;

    @Mock
    ScheduledBatchOrderProperties scheduledBatchOrderProperties;

    @Mock
    RestrictedDatesResolver restrictedDatesResolver;

    @Test
    void getAchTypeBusinessDaysGivenCreditTypeMatch(){
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditMinusBusinessDays()).thenReturn(10L);

        Assertions.assertEquals(10L, achBatchDateCalculator.getAchTypeBusinessDays("credit"));
    }

    @Test
    void getAchTypeBusinessDaysGivenCreditTypeDoesNotMatch(){
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("debit"));
        Mockito.when(scheduledBatchOrderProperties.getAchBatchDebitMinusBusinessDays()).thenReturn(10L);

        Assertions.assertEquals(10L, achBatchDateCalculator.getAchTypeBusinessDays("credit"));
    }

    @Test
    void calculateNextExecutionDate(){
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditMinusBusinessDays()).thenReturn(1L);
        achBatchDateCalculator.calculateNextExecutionDate("credit", LocalDate.now());
        Mockito.verify(restrictedDatesResolver, Mockito.times(1)).getRestrictedDates();
    }

    @Test
    void calculateOriginalExecutionDate(){
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditMinusBusinessDays()).thenReturn(1L);
        LocalDate localDate = achBatchDateCalculator.calculateOriginalExecutionDate("credit", LocalDate.now(), LocalDate.now());
        Assertions.assertEquals(LocalDate.now().plusDays(1L), localDate);
    }

    @Test
    void calculateOriginalExecutionDateGivenStartDateIsAfterOriginalExecutionDate(){
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditTypes()).thenReturn(List.of("credit"));
        Mockito.when(scheduledBatchOrderProperties.getAchBatchCreditMinusBusinessDays()).thenReturn(1L);
        LocalDate localDate = achBatchDateCalculator.calculateOriginalExecutionDate("credit", LocalDate.now(), LocalDate.now().plusDays(1L));
        Assertions.assertEquals(LocalDate.now().plusDays(1L), localDate);
    }

    @Test
    void canSubmitToCGI(){
        Assertions.assertEquals(true, achBatchDateCalculator.canSubmitToCGI(LocalDate.now()));
    }

    @Test
    void calculateRecurringNextExecutionDateGivenDailyFrequency(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.DAILY);
        schedule.setStartDate(LocalDate.now());
        schedule.setRepetition(2);
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertEquals(LocalDate.now().plusDays(1), localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateGivenIsItAfterEndExecutionDate(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.DAILY);
        schedule.setStartDate(LocalDate.now());
        schedule.setRepetition(1);
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertNull(localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateGivenScheduleEndDateIsNotNull(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.DAILY);
        schedule.setStartDate(LocalDate.now());
        schedule.setEndDate(LocalDate.now());
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertNull(localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateGivenWeeklyFrequency(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.WEEKLY);
        schedule.setStartDate(LocalDate.now());
        schedule.setRepetition(2);
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertEquals(LocalDate.now().plusDays(7), localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateGivenBiWeeklyFrequency(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.BIWEEKLY);
        schedule.setStartDate(LocalDate.now());
        schedule.setRepetition(2);
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertEquals(LocalDate.now().plusDays(14), localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateGivenMonthlyFrequency(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.MONTHLY);
        schedule.setStartDate(LocalDate.now());
        schedule.setRepetition(2);
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertEquals(LocalDate.now().plusDays(31), localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateGivenQuarterlyFrequency(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.QUARTERLY);
        schedule.setStartDate(LocalDate.now());
        schedule.setRepetition(2);
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertEquals(LocalDate.now().plusDays(92), localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateGivenYearlyFrequency(){
        Schedule schedule = new Schedule();
        schedule.setFrequency(BatchConstants.YEARLY);
        schedule.setStartDate(LocalDate.now());
        schedule.setRepetition(2);
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertEquals(LocalDate.now().plusDays(366), localDate);
    }

    @Test
    void calculateRecurringNextExecutionDateReturnNullGivenInvalidFrequency(){
        Schedule schedule = new Schedule();
        schedule.setFrequency("invalid");
        LocalDate localDate = achBatchDateCalculator.calculateRecurringNextExecutionDate(schedule, "credit", LocalDate.now());
        Assertions.assertNull(localDate);
    }

    @Test
    void hasCutOffTimePassed(){
        Assertions.assertTrue(achBatchDateCalculator.hasCutOffTimePassed(LocalTime.MIN));
    }

    @Test
    void appendYearMonthDate(){
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonth().getValue();
        int day = LocalDate.now().getDayOfMonth();
        Assertions.assertEquals("id" + year + StringUtils.EMPTY + month + StringUtils.EMPTY + day, achBatchDateCalculator.appendYearMonthDate("id"));
    }

}