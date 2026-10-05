package com.highlands.highlandscrmbackend.fleet;

public class DriverAlreadyExistsException extends RuntimeException {

    public DriverAlreadyExistsException(String message) {
        super(message);
    }
}