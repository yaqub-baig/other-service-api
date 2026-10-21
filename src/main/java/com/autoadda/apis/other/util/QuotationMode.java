package com.autoadda.apis.other.util;

/**
 * How a catalog service is bought. Set on a service to override its category's quotation scope.
 */
public enum QuotationMode {
    DIRECT,     // Booked at a listed price; quotation requests are not accepted
    OPTIONAL,   // Booked directly, or the customer may ask for a quote
    REQUIRED    // A quote must be accepted before booking
}
