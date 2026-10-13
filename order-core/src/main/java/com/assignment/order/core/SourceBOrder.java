package com.assignment.order.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record SourceBOrder(
        @JsonProperty("order_number") String orderNumber,
        Customer customer,
        @JsonProperty("created_at") String createdAt,
        Item item
) {
    public record Customer(
            String id,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            @JsonProperty("country_code") String countryCode
    ) {
    }

    public record Item(String sku, Integer units, BigDecimal price) {
    }
}
