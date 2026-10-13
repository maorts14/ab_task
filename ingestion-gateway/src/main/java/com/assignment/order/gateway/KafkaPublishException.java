package com.assignment.order.gateway;

class KafkaPublishException extends RuntimeException {
    KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
