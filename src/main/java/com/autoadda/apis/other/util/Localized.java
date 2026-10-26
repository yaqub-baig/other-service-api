package com.autoadda.apis.other.util;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Picks the Arabic or English text for the current request's Accept-Language,
 * falling back to English when no Arabic text exists.
 */
public final class Localized {

    private Localized() {
    }

    public static boolean isArabic() {
        return "ar".equals(LocaleContextHolder.getLocale().getLanguage());
    }

    public static String pick(String english, String arabic) {
        return isArabic() && arabic != null && !arabic.isBlank() ? arabic : english;
    }
}
