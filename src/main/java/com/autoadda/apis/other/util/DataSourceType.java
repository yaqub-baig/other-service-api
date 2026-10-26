package com.autoadda.apis.other.util;

/**
 * Enumeration of data sources for filter options.
 */
public enum DataSourceType {
    STATIC,         // Predefined options stored in FilterOption table
    DATABASE,       // Dynamic options from database query
    API,            // Options fetched from external API
    LOCATION        // Options from location service (cities, areas)
}
