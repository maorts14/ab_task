package com.assignment.order.gateway;

import com.assignment.order.core.OrderValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(OrderValidationException.class)
    ResponseEntity<ErrorResponse> invalidOrder(OrderValidationException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid order", exception.getMessage()));
    }

    @ExceptionHandler(KafkaPublishException.class)
    ResponseEntity<ErrorResponse> unavailable(KafkaPublishException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Order was not accepted", exception.getMessage()));
    }
}
