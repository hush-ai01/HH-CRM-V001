package com.highlands.highlandscrmbackend.common.exception;

public class GradeAlreadyExistsException extends RuntimeException {

    public GradeAlreadyExistsException(String message) {
        super(message);
    }
}