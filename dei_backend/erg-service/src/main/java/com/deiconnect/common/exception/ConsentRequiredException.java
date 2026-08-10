package com.deiconnect.common.exception;

public class ConsentRequiredException extends RuntimeException {

    public ConsentRequiredException(String message) {
        super(message);
    }
}
