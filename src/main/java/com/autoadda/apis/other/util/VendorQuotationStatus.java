package com.autoadda.apis.other.util;

/**
 * Enumeration of statuses for vendor quotations.
 */
public enum VendorQuotationStatus {
    DRAFT,      // Vendor is preparing the quote
    SENT,       // Quote has been sent to the customer
    ACCEPTED,   // Customer has accepted this quote
    REJECTED,   // Customer has rejected this quote
    EXPIRED,    // Quote validity period has passed
    WITHDRAWN   // Vendor withdrew the quote
}
