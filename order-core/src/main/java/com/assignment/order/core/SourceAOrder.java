package com.assignment.order.core;

import java.math.BigDecimal;

public record SourceAOrder(
        String orderId,
        String customerId,
        String customerName,
        String country,
        String orderDate,
        String productCode,
        Integer quantity,
        BigDecimal unitPrice
) {
}
