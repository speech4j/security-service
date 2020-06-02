package org.speech4j.securityservice.exception;

public class EntityExistsException extends RuntimeException {

    private String message;

    public EntityExistsException(String message) {
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
