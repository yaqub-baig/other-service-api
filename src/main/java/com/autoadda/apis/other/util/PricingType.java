package com.autoadda.apis.other.util;

/**
 * Enumeration of pricing strategies for vendor services.
 */
public enum PricingType {
    FIXED,          // Fixed price
    RANGE,          // Price range (minPrice to maxPrice)
    QUOTE_BASED,    // Price determined through quotation
    NEGOTIABLE      // Price is negotiable
}
