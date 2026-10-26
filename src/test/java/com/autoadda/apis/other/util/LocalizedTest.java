package com.autoadda.apis.other.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizedTest {

    @AfterEach
    void reset() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void returnsArabicForArabicLocale() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ar-AE"));
        assertThat(Localized.pick("Car Wash", "غسيل السيارة")).isEqualTo("غسيل السيارة");
    }

    @Test
    void fallsBackToEnglishWhenArabicMissing() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("ar"));
        assertThat(Localized.pick("Car Wash", null)).isEqualTo("Car Wash");
        assertThat(Localized.pick("Car Wash", " ")).isEqualTo("Car Wash");
    }

    @Test
    void returnsEnglishForOtherLocales() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertThat(Localized.pick("Car Wash", "غسيل السيارة")).isEqualTo("Car Wash");
    }
}
