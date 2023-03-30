package com.backbase.accelerators.model;

import lombok.Data;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RestrictedDate {

    private String eventName;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate blackoutDate;
}
