package ru.myproject.itq.exeption;

public class RegistryWriteFailedRuntimeException extends RuntimeException {
    public RegistryWriteFailedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
