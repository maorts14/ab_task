package com.assignment.order.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

public class OrderNormalizer {
    private final ObjectMapper objectMapper;
    private final CountryCatalog countryCatalog;

    public OrderNormalizer(ObjectMapper objectMapper, CountryCatalog countryCatalog) {
        this.objectMapper = objectMapper;
        this.countryCatalog = countryCatalog;
    }

    public String validate(SourceSystem sourceSystem, String rawOrder) {
        return switch (sourceSystem) {
            case SOURCE_A -> readSourceA(rawOrder).orderId();
            case SOURCE_B -> readSourceB(rawOrder).orderNumber();
        };
    }

    public ProcessedOrder normalize(SourceSystem sourceSystem, String rawOrder) {
        return switch (sourceSystem) {
            case SOURCE_A -> normalize(readSourceA(rawOrder));
            case SOURCE_B -> normalize(readSourceB(rawOrder));
        };
    }

    private SourceAOrder readSourceA(String rawOrder) {
        SourceAOrder order = read(rawOrder, SourceAOrder.class);
        requireText(order.orderId(), "orderId");
        requireText(order.customerId(), "customerId");
        requireText(order.customerName(), "customerName");
        requireText(order.country(), "country");
        requireText(order.orderDate(), "orderDate");
        requireText(order.productCode(), "productCode");
        requireQuantity(order.quantity());
        requirePrice(order.unitPrice());
        countryCatalog.get(order.country());
        return order;
    }

    private SourceBOrder readSourceB(String rawOrder) {
        SourceBOrder order = read(rawOrder, SourceBOrder.class);
        requireText(order.orderNumber(), "order_number");
        if (order.customer() == null) {
            throw new OrderValidationException("customer is required");
        }
        if (order.item() == null) {
            throw new OrderValidationException("item is required");
        }
        requireText(order.customer().id(), "customer.id");
        requireText(order.customer().firstName(), "customer.first_name");
        requireText(order.customer().lastName(), "customer.last_name");
        requireText(order.customer().countryCode(), "customer.country_code");
        requireText(order.createdAt(), "created_at");
        requireText(order.item().sku(), "item.sku");
        requireQuantity(order.item().units());
        requirePrice(order.item().price());
        countryCatalog.get(order.customer().countryCode());
        return order;
    }

    private ProcessedOrder normalize(SourceAOrder order) {
        CountryInfo country = countryCatalog.get(order.country());
        return new ProcessedOrder(
                order.orderId(),
                new ProcessedOrder.Customer(order.customerId(), order.customerName(), country.name()),
                order.orderDate(),
                new ProcessedOrder.Product(order.productCode(), order.quantity(), order.unitPrice()),
                order.unitPrice().multiply(BigDecimal.valueOf(order.quantity())),
                country.currency()
        );
    }

    private ProcessedOrder normalize(SourceBOrder order) {
        CountryInfo country = countryCatalog.get(order.customer().countryCode());
        return new ProcessedOrder(
                order.orderNumber(),
                new ProcessedOrder.Customer(
                        order.customer().id(),
                        order.customer().firstName() + " " + order.customer().lastName(),
                        country.name()
                ),
                order.createdAt(),
                new ProcessedOrder.Product(order.item().sku(), order.item().units(), order.item().price()),
                order.item().price().multiply(BigDecimal.valueOf(order.item().units())),
                country.currency()
        );
    }

    private <T> T read(String rawOrder, Class<T> type) {
        try {
            return objectMapper.readValue(rawOrder, type);
        } catch (JsonProcessingException exception) {
            throw new OrderValidationException("Invalid JSON payload");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new OrderValidationException(field + " is required");
        }
    }

    private void requireQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new OrderValidationException("quantity must be greater than zero");
        }
    }

    private void requirePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new OrderValidationException("unit price must be greater than zero");
        }
    }
}
