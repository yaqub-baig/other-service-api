package com.autoadda.apis.other.util;

/**
 * Data types for service attribute values.
 */
public enum DataType {
    STRING,         // Text values
    NUMBER,         // Numeric values (integer or decimal)
    INTEGER,        // Whole numbers only
    DECIMAL,        // Any number
    BOOLEAN,        // True/false values
    DATE,           // ISO date (yyyy-MM-dd)
    SINGLE_SELECT,  // One of the attribute's options
    MULTI_SELECT,   // A list of the attribute's options
    JSON            // Complex nested data structures
}
