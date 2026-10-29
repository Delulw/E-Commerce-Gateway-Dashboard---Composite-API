package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.model.Inventory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class InventoryRepository {

    private final Map<String, Inventory> inventoryByProductId = Map.of(
            "P-101", new Inventory(
                    "P-101",
                    15,
                    "Almacén central - A-03",
                    2
            )
    );

    public Optional<Inventory> findByProductId(String productId) {
        return Optional.ofNullable(inventoryByProductId.get(productId));
    }
}
