package ru.klokov.exception;

public class WrongParametersException extends RuntimeException {
    public WrongParametersException(String message) {
        super(message);
    }
}
