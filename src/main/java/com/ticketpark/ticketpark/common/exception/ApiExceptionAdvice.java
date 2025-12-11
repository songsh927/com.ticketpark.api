package com.ticketpark.ticketpark.common.exception;

import com.ticketpark.ticketpark.common.dto.DefaultRes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionAdvice {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity exceptionHandler(HttpServletRequest http, ApiException e){
        return new ResponseEntity(DefaultRes.res(false, e.getError().getMessage()), e.getError().getStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity exceptionHandler(HttpServletRequest http,final RuntimeException e){
        System.out.println("[ERROR]::::"+ e.getMessage());
        return new ResponseEntity(DefaultRes.res(false, "error" ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity exceptionHandler(HttpServletRequest http,final Exception e){
        System.out.println("[ERROR]::::"+ e.getMessage());
        return new ResponseEntity(DefaultRes.res(false, "error"), HttpStatus.UNAUTHORIZED);
    }


}
