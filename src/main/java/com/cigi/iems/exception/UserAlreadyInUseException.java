package com.cigi.iems.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyInUseException extends RuntimeException {
    public UserAlreadyInUseException(String message) {
        super(message);
    }
}
