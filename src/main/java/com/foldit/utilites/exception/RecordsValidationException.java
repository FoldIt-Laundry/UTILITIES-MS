package com.foldit.utilites.exception;

public class RecordsValidationException extends RuntimeException{
    public RecordsValidationException(String errorMessage, Exception ex) {
        super(errorMessage, ex);
    }

    public RecordsValidationException(String errorMessage) {
        super(errorMessage);
    }
}
