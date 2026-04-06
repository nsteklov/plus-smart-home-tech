package ru.yandex.practicum.exception;

import org.springframework.http.HttpStatus;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String userMessage;

    public NoSpecifiedProductInWarehouseException(String message, HttpStatus httpStatus, String userMessage) {
        super(message);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }
}
