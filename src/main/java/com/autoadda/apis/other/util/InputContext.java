package com.autoadda.apis.other.util;

/**
 * Who fills an attribute in: the customer when requesting, the provider when listing an offering, or both.
 * VENDOR attributes describe the vendor's branch instead (bay count, mobile service, ...); they are
 * category-level, filled once when the vendor adds the service, and never appear on offerings or requests.
 */
public enum InputContext {
    CUSTOMER,
    PROVIDER,
    BOTH,
    VENDOR;

    public boolean appliesToProvider() {
        return this == PROVIDER || this == BOTH;
    }

    public boolean appliesToCustomer() {
        return this == CUSTOMER || this == BOTH;
    }
}
