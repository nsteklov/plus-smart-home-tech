package ru.yandex.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NoDeliveryFoundException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String userMessage;

    public NoDeliveryFoundException(String message, HttpStatus httpStatus, String userMessage) {
        super(message);
        this.httpStatus = httpStatus;
        this.userMessage = userMessage;
    }
}
