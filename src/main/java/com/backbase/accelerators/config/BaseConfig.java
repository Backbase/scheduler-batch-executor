package com.backbase.accelerators.config;

import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.Pattern;

public class BaseConfig {

    @Value("${backbase.communication.http.default-scheme:http}")
    @Pattern(regexp = "https?")
    protected String scheme;
}
