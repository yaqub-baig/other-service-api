package com.autoadda.apis.other.util;

/**
 * Enumeration of statuses for customer quotation requests.
 */
public enum QuotationRequestStatus {
    REQUESTED,          // Initial state when customer submits request
    SENT_TO_VENDORS,    // Request has been broadcast to matching vendors
    QUOTES_RECEIVED,    // One or more vendors have responded
    ACCEPTED,           // Customer has accepted a vendor quote
    EXPIRED,            // Request has expired without acceptance
    CANCELLED           // Customer cancelled the request
}
