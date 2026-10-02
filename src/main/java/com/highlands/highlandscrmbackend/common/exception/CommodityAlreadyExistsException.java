package com.highlands.highlandscrmbackend.common.exception;

public class CommodityAlreadyExistsException extends RuntimeException {

    public CommodityAlreadyExistsException(String message) {
        super(message);
    }
}