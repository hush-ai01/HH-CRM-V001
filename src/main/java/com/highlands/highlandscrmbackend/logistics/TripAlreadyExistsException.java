package com.highlands.highlandscrmbackend.logistics;

public class TripAlreadyExistsException extends RuntimeException {

    public TripAlreadyExistsException(String message) {
        super(message);
    }
}