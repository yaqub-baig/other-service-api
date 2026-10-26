package com.autoadda.apis.other.util;

/**
 * Enumeration of quotation strategies for service categories.
 */
public enum QuotationType {
    OPTIONAL,   // Vendor can have a fixed price listing AND customers can optionally request a quote
    MANDATORY,  // No price is shown; a quote is required to get a price
    HYBRID      // A base price is shown, but customers can request a custom quote for additional work
}
