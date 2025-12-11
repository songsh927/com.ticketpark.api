package com.ticketpark.ticketpark.common.exception;

import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class ApiException extends RuntimeException {

    private ExceptionEnum error;

    public ApiException(ExceptionEnum e) {
        super(e.getMessage());
        this.error = e;
    }

}
