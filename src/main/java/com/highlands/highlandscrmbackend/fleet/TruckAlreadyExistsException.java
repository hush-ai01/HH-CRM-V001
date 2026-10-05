package com.highlands.highlandscrmbackend.fleet;

public class TruckAlreadyExistsException extends RuntimeException {

    public TruckAlreadyExistsException(String message) {
        super(message);
    }
}