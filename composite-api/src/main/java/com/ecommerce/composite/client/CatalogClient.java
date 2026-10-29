package com.ecommerce.composite.client;

import com.ecommerce.composite.dto.CatalogProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder builder,
            @Value("${services.catalog.base-url}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public CatalogProductResponse getProduct(String id) {
        CatalogProductResponse product = restClient.get()
                .uri("/microservices/products/{id}", id)
                .retrieve()
                .body(CatalogProductResponse.class);

        if (product == null) {
            throw new IllegalStateException("Catalog service returned an empty product response");
        }

        return product;
    }
}
