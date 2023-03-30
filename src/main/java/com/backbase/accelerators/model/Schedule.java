package com.backbase.accelerators.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * Required to calculate next execution date
 *
 * @author gurupadam
 */
@Data
public class Schedule {

    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer repetition;
    private String endType;
    private Integer whenExecute;

}