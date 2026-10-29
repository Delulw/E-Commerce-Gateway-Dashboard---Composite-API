package com.ecommerce.catalog.model;

import java.math.BigDecimal;

public record Product(
        String id,
        String name,
        String description,
        String category,
        BigDecimal priceUsd
) {
}
