package com.assignment.order.core;

import java.util.Map;

public class InMemoryCountryCatalog implements CountryCatalog {
    private static final Map<String, CountryInfo> COUNTRIES = Map.of(
            "US", new CountryInfo("United States", "USD"),
            "GB", new CountryInfo("United Kingdom", "GBP"),
            "DE", new CountryInfo("Germany", "EUR")
    );

    @Override
    public CountryInfo get(String countryCode) {
        CountryInfo country = COUNTRIES.get(countryCode);
        if (country == null) {
            throw new OrderValidationException("Unsupported country code: " + countryCode);
        }
        return country;
    }
}
