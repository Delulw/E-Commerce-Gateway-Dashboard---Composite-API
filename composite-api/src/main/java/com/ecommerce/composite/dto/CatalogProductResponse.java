package com.ecommerce.composite.dto;

import java.math.BigDecimal;

public record CatalogProductResponse(
        String id,
        String name,
        String description,
        String category,
        BigDecimal priceUsd
) {
}
