package org.speech4j.securityservice.exception;

public class DataOperationException extends RuntimeException {

    private String message;

    public DataOperationException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
