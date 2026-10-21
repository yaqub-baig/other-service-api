package com.autoadda.apis.other.util;

import com.autoadda.apis.other.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * Attribute and filter codes become JSON paths and generated column names, so they are limited to
 * lowercase letters, digits and underscores.
 */
public final class AttributeCodes {

    private static final Pattern VALID = Pattern.compile("[a-z][a-z0-9_]{0,49}");

    private AttributeCodes() {
    }

    public static boolean isValid(String code) {
        return code != null && VALID.matcher(code).matches();
    }

    public static String requireValid(String code) {
        if (!isValid(code)) {
            throw new BusinessException("Invalid code '" + code
                    + "': use 1-50 lowercase letters, digits or underscores, starting with a letter");
        }
        return code;
    }
}
