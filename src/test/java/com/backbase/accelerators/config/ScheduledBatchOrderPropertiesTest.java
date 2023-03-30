package com.backbase.accelerators.config;

import com.backbase.accelerators.constants.WeekendExecutionStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(SpringExtension.class)
class ScheduledBatchOrderPropertiesTest {

    @InjectMocks
    ScheduledBatchOrderProperties scheduledBatchOrderProperties;

    @BeforeEach
    void init() {
        scheduledBatchOrderProperties.setAchBatchQueueName("achName");
        scheduledBatchOrderProperties.setAchBatchDebitTypes(List.of("debit"));
        scheduledBatchOrderProperties.setAchBatchCreditTypes(List.of("credit"));
        scheduledBatchOrderProperties.setAchBatchDebitMinusBusinessDays(10);
        scheduledBatchOrderProperties.setAchBatchCreditMinusBusinessDays(10);
        scheduledBatchOrderProperties.setPageSize(10);
        scheduledBatchOrderProperties.setLimitChecksEnabled(true);
        scheduledBatchOrderProperties.setBalanceCheckEnabled(true);
        scheduledBatchOrderProperties.setCreatedUserValidCheckEnabled(false);
        scheduledBatchOrderProperties.setMaxFailureCountPerSchedule(10);
        scheduledBatchOrderProperties.setCutOffTime(LocalTime.MIDNIGHT);
        ScheduledBatchOrderProperties.ExecutionDateValidationProperties properties =
                new ScheduledBatchOrderProperties.ExecutionDateValidationProperties();
        properties.setRestrictedDates(List.of(LocalDate.now()));
        properties.setWeekendExecutionStrategy(WeekendExecutionStrategy.ALLOW);
        scheduledBatchOrderProperties.setExecutionDateValidation(properties);
    }

    @Test
    void VerifyThatVantageConfigurationPropertiesAreSet() {
        Assertions.assertEquals("achName", scheduledBatchOrderProperties.getAchBatchQueueName());
        Assertions.assertEquals("debit", scheduledBatchOrderProperties.getAchBatchDebitTypes().get(0));
        Assertions.assertEquals("credit", scheduledBatchOrderProperties.getAchBatchCreditTypes().get(0));
        Assertions.assertEquals(10, scheduledBatchOrderProperties.getAchBatchDebitMinusBusinessDays());
        Assertions.assertEquals(10, scheduledBatchOrderProperties.getAchBatchCreditMinusBusinessDays());
        Assertions.assertEquals(10, scheduledBatchOrderProperties.getPageSize());
        Assertions.assertEquals(true, scheduledBatchOrderProperties.isLimitChecksEnabled());
        Assertions.assertEquals(true, scheduledBatchOrderProperties.isBalanceCheckEnabled());
        Assertions.assertEquals(false, scheduledBatchOrderProperties.isCreatedUserValidCheckEnabled());
        Assertions.assertEquals(10, scheduledBatchOrderProperties.getMaxFailureCountPerSchedule());
        Assertions.assertEquals(LocalTime.MIDNIGHT, scheduledBatchOrderProperties.getCutOffTime());
        Assertions.assertEquals(1, scheduledBatchOrderProperties.getExecutionDateValidation().getRestrictedDates().size());
        Assertions.assertEquals(WeekendExecutionStrategy.ALLOW, scheduledBatchOrderProperties.getExecutionDateValidation().getWeekendExecutionStrategy());
    }

}