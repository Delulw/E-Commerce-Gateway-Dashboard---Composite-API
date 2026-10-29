package com.ecommerce.composite.client;

import com.ecommerce.composite.dto.InventoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(
            RestClient.Builder builder,
            @Value("${services.inventory.base-url}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public InventoryResponse getInventory(String productId) {
        InventoryResponse inventory = restClient.get()
                .uri("/microservices/inventory/{productId}", productId)
                .retrieve()
                .body(InventoryResponse.class);

        if (inventory == null) {
            throw new IllegalStateException("Inventory service returned an empty response");
        }

        return inventory;
    }
}
