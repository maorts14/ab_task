package com.assignment.order.worker;

import com.assignment.order.core.CountryCatalog;
import com.assignment.order.core.InMemoryCountryCatalog;
import com.assignment.order.core.OrderNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CoreConfiguration {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    CountryCatalog countryCatalog() {
        return new InMemoryCountryCatalog();
    }

    @Bean
    OrderNormalizer orderNormalizer(ObjectMapper objectMapper, CountryCatalog countryCatalog) {
        return new OrderNormalizer(objectMapper, countryCatalog);
    }
}
