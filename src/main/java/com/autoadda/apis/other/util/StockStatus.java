package com.autoadda.apis.other.util;

/**
 * Enumeration of stock/availability status for service offerings.
 */
public enum StockStatus {
    IN_STOCK,           // Available immediately
    LIMITED_STOCK,      // Limited availability
    OUT_OF_STOCK,       // Currently unavailable
    PRE_ORDER,          // Available for pre-order
    DISCONTINUED        // No longer offered
}
