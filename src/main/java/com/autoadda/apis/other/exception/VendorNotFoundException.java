package com.autoadda.apis.other.exception;


/**
 * throw exception when username not exist
 */
public class VendorNotFoundException extends RuntimeException {

    private String message;

    public VendorNotFoundException() {
        super();
    }

    public VendorNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public VendorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

