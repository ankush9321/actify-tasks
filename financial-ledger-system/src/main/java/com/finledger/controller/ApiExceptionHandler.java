package com.finledger.controller;

import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String,String>> forbidden(AccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException ex) {
        return response(HttpStatus.BAD_REQUEST, "Request validation failed");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> general(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Request failed");
    }
    private ResponseEntity<Map<String,String>> response(HttpStatus status, String message) {
        Map<String,String> body = new LinkedHashMap<String,String>();
        body.put("error", message == null ? status.getReasonPhrase() : message);
        return new ResponseEntity<Map<String,String>>(body, status);
    }
}
