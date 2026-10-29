package com.ecommerce.composite.dto;

public record InventoryResponse(
        String productId,
        int availableUnits,
        String warehouseLocation,
        int estimatedDeliveryDays
) {
}
