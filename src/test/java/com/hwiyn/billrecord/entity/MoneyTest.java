package com.hwiyn.billrecord.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void defaultsToCnyAndStoresTwoDecimalPlaces() {
        Money money = Money.of(new BigDecimal("12.30"));

        assertThat(money.getAmount()).isEqualByComparingTo("12.30");
        assertThat(money.getAmount().scale()).isEqualTo(2);
        assertThat(money.getCurrency()).isEqualTo("CNY");
    }

    @Test
    void rejectsMoreThanTwoFractionDigits() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("12.345")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scale");
    }

    @Test
    void rejectsValuesThatDoNotFitDecimal19And2() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("123456789012345678.90")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("decimal(19,2)");
    }
}
