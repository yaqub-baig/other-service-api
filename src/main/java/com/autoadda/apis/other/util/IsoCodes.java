package com.autoadda.apis.other.util;

import com.autoadda.apis.other.exception.BusinessException;

import java.util.Currency;
import java.util.Locale;
import java.util.Set;

/**
 * Normalises and checks ISO 3166-1 alpha-2 country codes and ISO 4217 currency codes.
 */
public final class IsoCodes {

    private static final Set<String> COUNTRIES = Set.of(Locale.getISOCountries());

    private IsoCodes() {
    }

    public static String country(String code) {
        String normalized = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
        if (normalized == null || !COUNTRIES.contains(normalized)) {
            throw new BusinessException("Invalid country code: " + code);
        }
        return normalized;
    }

    public static String currency(String code) {
        String normalized = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
        try {
            return Currency.getInstance(normalized).getCurrencyCode();
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException("Invalid currency code: " + code);
        }
    }
}
