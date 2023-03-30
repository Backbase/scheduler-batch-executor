package com.backbase.accelerators.config;


import com.backbase.accelerators.constants.WeekendExecutionStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties("scheduled-batch")
public class ScheduledBatchOrderProperties {

    private String achBatchQueueName;
    private List<String> achBatchDebitTypes;

    private List<String> achBatchCreditTypes;

    private long achBatchDebitMinusBusinessDays;

    private long achBatchCreditMinusBusinessDays;

    private int pageSize;
    private boolean limitChecksEnabled;

    private boolean balanceCheckEnabled;

    private boolean createdUserValidCheckEnabled;

    private int maxFailureCountPerSchedule;

    @DateTimeFormat(pattern = "H:mm")
    private LocalTime cutOffTime;
    private ExecutionDateValidationProperties executionDateValidation;

    @Data
    public static class ExecutionDateValidationProperties {

        private WeekendExecutionStrategy weekendExecutionStrategy;

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private List<LocalDate> restrictedDates;
    }
}
