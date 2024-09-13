package com.foldit.utilites.exception;

public class GoogleApiException extends RuntimeException{
    public GoogleApiException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    public GoogleApiException(String errorMessage) {
        super(errorMessage);
    }

}