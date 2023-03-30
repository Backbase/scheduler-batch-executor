package com.backbase.accelerators.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RestrictedDates {

    private String cutOffTime;
    private List<LocalDate> blackoutDates;
}
