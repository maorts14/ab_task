package com.assignment.order.core;

import java.math.BigDecimal;

public record ProcessedOrder(
        String orderReference,
        Customer customer,
        String orderTimestamp,
        Product product,
        BigDecimal totalOrderValue,
        String currency
) {
    public record Customer(String customerReference, String fullName, String country) {
    }

    public record Product(String code, Integer quantity, BigDecimal unitPrice) {
    }
}
