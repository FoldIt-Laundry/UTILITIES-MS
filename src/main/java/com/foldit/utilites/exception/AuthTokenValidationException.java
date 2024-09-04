package com.foldit.utilites.exception;

public class AuthTokenValidationException extends RuntimeException{

    public AuthTokenValidationException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    public AuthTokenValidationException(String errorMessage) {
        super(errorMessage);
    }
}
