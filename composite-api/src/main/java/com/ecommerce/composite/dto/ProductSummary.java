package com.ecommerce.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProductSummary(
        @JsonProperty("product_id") String productId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("category") String category,
        @JsonProperty("price_usd") BigDecimal priceUsd,
        @JsonProperty("price_mxn") BigDecimal priceMxn,
        @JsonProperty("stock_status") String stockStatus,
        @JsonProperty("available_units") int availableUnits,
        @JsonProperty("warehouse_location") String warehouseLocation,
        @JsonProperty("estimated_delivery_days") int estimatedDeliveryDays,
        @JsonProperty("currency_conversion_source") String currencyConversionSource,
        @JsonProperty("status") String status
) {
}
