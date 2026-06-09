package com.hwiyn.billrecord;

import java.time.ZoneOffset;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BillRecordApiApplication {

    public static void main(String[] args) {
        configureUtcTimeZone();
        SpringApplication.run(BillRecordApiApplication.class, args);
    }

    static void configureUtcTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }
}
