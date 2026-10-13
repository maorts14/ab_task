package com.assignment.order.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderNormalizerTest {
    private final OrderNormalizer normalizer = new OrderNormalizer(new ObjectMapper(), new InMemoryCountryCatalog());

    @Test
    void normalizesSourceA() {
        ProcessedOrder order = normalizer.normalize(SourceSystem.SOURCE_A, """
                {"orderId":"ORD-10001","customerId":"CUST-501","customerName":"John Smith","country":"US","orderDate":"2026-09-01T10:30:00","productCode":"P100","quantity":2,"unitPrice":125.50}
                """);

        assertEquals("ORD-10001", order.orderReference());
        assertEquals("United States", order.customer().country());
        assertEquals(new BigDecimal("251.00"), order.totalOrderValue());
        assertEquals("USD", order.currency());
    }

    @Test
    void normalizesSourceB() {
        ProcessedOrder order = normalizer.normalize(SourceSystem.SOURCE_B, """
                {"order_number":"ORD-20001","customer":{"id":"CUST-842","first_name":"Jane","last_name":"Miller","country_code":"DE"},"created_at":"2026-09-01T11:15:00","item":{"sku":"P200","units":3,"price":80.00}}
                """);

        assertEquals("Jane Miller", order.customer().fullName());
        assertEquals("Germany", order.customer().country());
        assertEquals(new BigDecimal("240.00"), order.totalOrderValue());
        assertEquals("EUR", order.currency());
    }

    @Test
    void rejectsUnknownCountry() {
        assertThrows(OrderValidationException.class, () -> normalizer.validate(SourceSystem.SOURCE_A, """
                {"orderId":"ORD-1","customerId":"CUST-1","customerName":"Name","country":"FR","orderDate":"2026-09-01T10:30:00","productCode":"P1","quantity":1,"unitPrice":1.00}
                """));
    }
}
