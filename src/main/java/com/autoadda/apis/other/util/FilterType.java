package com.autoadda.apis.other.util;

/**
 * Enumeration of supported filter UI types.
 */
public enum FilterType {
    DROPDOWN,       // Single select dropdown
    MULTI_SELECT,   // Multiple select checkboxes
    RANGE,          // Min-max range slider (for numeric values)
    TEXT,           // Free text input
    LOCATION,       // Location picker (city/area)
    AUTOCOMPLETE,   // Autocomplete text input
    DATE_RANGE,     // Date range picker
    BOOLEAN         // Yes/No toggle
}
