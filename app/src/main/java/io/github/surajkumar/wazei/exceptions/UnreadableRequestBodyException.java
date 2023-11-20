package io.github.surajkumar.wazei.exceptions;

public class UnreadableRequestBodyException extends Exception {
    public UnreadableRequestBodyException() {
        super();
    }

    public UnreadableRequestBodyException(String message) {
        super(message);
    }
}
