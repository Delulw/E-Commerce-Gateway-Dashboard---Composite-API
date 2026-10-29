package com.ecommerce.inventory.model;

public record Inventory(
        String productId,
        int availableUnits,
        String warehouseLocation,
        int estimatedDeliveryDays
) {
}
