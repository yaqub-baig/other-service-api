package com.autoadda.apis.other.util;

/**
 * Who fills an attribute in: the customer when requesting, the provider when listing an offering, or both.
 */
public enum InputContext {
    CUSTOMER,
    PROVIDER,
    BOTH;

    public boolean appliesToProvider() {
        return this == PROVIDER || this == BOTH;
    }

    public boolean appliesToCustomer() {
        return this == CUSTOMER || this == BOTH;
    }
}
