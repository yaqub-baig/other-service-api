package com.autoadda.apis.other.util;

/**
 * Enumeration of pricing types for vendor quotations.
 */
public enum VendorPricingType {
    FIXED,                  // Single fixed total price
    RANGE,                  // Price range (min to max)
    BREAKUP,                // Itemized line items with individual prices
    INSPECTION_REQUIRED     // On-site inspection required before final pricing
}
