package edu.numerical.controller;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> invalid(IllegalArgumentException ex){return ResponseEntity.badRequest().body(Map.of("code","INVALID_INPUT","message",ex.getMessage()));}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex){return ResponseEntity.badRequest().body(Map.of("code","VALIDATION_ERROR","message","Check the input fields.","fields",ex.getBindingResult().getFieldErrors().stream().map(e->Map.of("field",e.getField(),"message",String.valueOf(e.getDefaultMessage()))).toList()));}
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> json(){return ResponseEntity.badRequest().body(Map.of("code","INVALID_JSON","message","Provide valid JSON with finite numeric values."));}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception ex){return ResponseEntity.status(500).body(Map.of("code","INTERNAL_ERROR","message","Unable to complete the calculation."));}
}
