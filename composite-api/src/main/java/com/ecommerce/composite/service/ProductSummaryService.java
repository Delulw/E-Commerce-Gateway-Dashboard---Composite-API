package com.ecommerce.composite.service;

import com.ecommerce.composite.client.CatalogClient;
import com.ecommerce.composite.client.CurrencyExchangeClient;
import com.ecommerce.composite.client.InventoryClient;
import com.ecommerce.composite.dto.CatalogProductResponse;
import com.ecommerce.composite.dto.ExchangeRate;
import com.ecommerce.composite.dto.InventoryResponse;
import com.ecommerce.composite.dto.ProductSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
public class ProductSummaryService {

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final CurrencyExchangeClient currencyExchangeClient;
    private final Executor requestExecutor;

    public ProductSummaryService(
            CatalogClient catalogClient,
            InventoryClient inventoryClient,
            CurrencyExchangeClient currencyExchangeClient,
            @Qualifier("compositeRequestExecutor") Executor requestExecutor
    ) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.currencyExchangeClient = currencyExchangeClient;
        this.requestExecutor = requestExecutor;
    }

    public ProductSummary getProductSummary(String id) {
        CompletableFuture<CatalogProductResponse> product = CompletableFuture.supplyAsync(
                () -> catalogClient.getProduct(id),
                requestExecutor
        );
        CompletableFuture<InventoryResponse> inventory = CompletableFuture.supplyAsync(
                () -> inventoryClient.getInventory(id),
                requestExecutor
        );
        CompletableFuture<ExchangeRate> exchangeRate = CompletableFuture.supplyAsync(
                currencyExchangeClient::getUsdExchangeRate,
                requestExecutor
        );

        try {
            CompletableFuture.allOf(product, inventory, exchangeRate).join();
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw exception;
        }

        return buildSummary(product.join(), inventory.join(), exchangeRate.join());
    }

    private ProductSummary buildSummary(
            CatalogProductResponse product,
            InventoryResponse inventory,
            ExchangeRate exchangeRate
    ) {
        return new ProductSummary(
                product.id(),
                product.name(),
                product.description(),
                product.category(),
                product.priceUsd(),
                product.priceUsd()
                        .multiply(exchangeRate.rate())
                        .setScale(2, RoundingMode.HALF_UP),
                inventory.availableUnits() > 0 ? "IN_STOCK" : "OUT_OF_STOCK",
                inventory.availableUnits(),
                inventory.warehouseLocation(),
                inventory.estimatedDeliveryDays(),
                exchangeRate.source(),
                "SUCCESS"
        );
    }
}
