package com.hwiyn.billrecord;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;

class ApplicationStructureTest {

    @Test
    void applicationUsesRequestedBasePackage() {
        assertThat(BillRecordApiApplication.class.getPackageName())
                .isEqualTo("com.hwiyn.billrecord");
    }

    @Test
    void applicationConfiguresUtcDefaultTimeZone() {
        TimeZone originalTimeZone = TimeZone.getDefault();

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

            BillRecordApiApplication.configureUtcTimeZone();

            assertThat(TimeZone.getDefault().getID()).isEqualTo("UTC");
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }
}
