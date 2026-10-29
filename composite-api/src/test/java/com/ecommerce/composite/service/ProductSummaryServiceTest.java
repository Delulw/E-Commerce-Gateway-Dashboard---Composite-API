package com.ecommerce.composite.service;

import com.ecommerce.composite.client.CatalogClient;
import com.ecommerce.composite.client.CurrencyExchangeClient;
import com.ecommerce.composite.client.InventoryClient;
import com.ecommerce.composite.dto.CatalogProductResponse;
import com.ecommerce.composite.dto.ExchangeRate;
import com.ecommerce.composite.dto.InventoryResponse;
import com.ecommerce.composite.dto.ProductSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductSummaryServiceTest {

    @Test
    void aggregatesAllSourcesAndCalculatesLocalPriceAndStockStatus() {
        CatalogClient catalogClient = mock(CatalogClient.class);
        InventoryClient inventoryClient = mock(InventoryClient.class);
        CurrencyExchangeClient currencyExchangeClient = mock(CurrencyExchangeClient.class);

        when(catalogClient.getProduct("P-101")).thenReturn(new CatalogProductResponse(
                "P-101",
                "Laptop Gamer XYZ",
                "Laptop para videojuegos.",
                "Electrónica",
                new BigDecimal("1200.00")
        ));
        when(inventoryClient.getInventory("P-101")).thenReturn(new InventoryResponse(
                "P-101",
                15,
                "Almacén central - A-03",
                2
        ));
        when(currencyExchangeClient.getUsdExchangeRate()).thenReturn(new ExchangeRate(
                "USD",
                "MXN",
                new BigDecimal("18.05"),
                "2026-10-29",
                "Frankfurter External API"
        ));

        ProductSummaryService service = new ProductSummaryService(
                catalogClient,
                inventoryClient,
                currencyExchangeClient,
                Runnable::run
        );

        ProductSummary result = service.getProductSummary("P-101");

        assertEquals("P-101", result.productId());
        assertEquals("Laptop Gamer XYZ", result.title());
        assertEquals("Laptop para videojuegos.", result.description());
        assertEquals("Electrónica", result.category());
        assertEquals(0, new BigDecimal("1200.00").compareTo(result.priceUsd()));
        assertEquals(0, new BigDecimal("21660.00").compareTo(result.priceMxn()));
        assertEquals("IN_STOCK", result.stockStatus());
        assertEquals(15, result.availableUnits());
        assertEquals("Almacén central - A-03", result.warehouseLocation());
        assertEquals(2, result.estimatedDeliveryDays());
        assertEquals("Frankfurter External API", result.currencyConversionSource());
        assertEquals("SUCCESS", result.status());
    }

    @Test
    void startsAllThreeDependencyCallsConcurrently() throws Exception {
        CatalogClient catalogClient = mock(CatalogClient.class);
        InventoryClient inventoryClient = mock(InventoryClient.class);
        CurrencyExchangeClient currencyExchangeClient = mock(CurrencyExchangeClient.class);
        CountDownLatch requestsStarted = new CountDownLatch(3);
        CountDownLatch releaseRequests = new CountDownLatch(1);

        CatalogProductResponse product = new CatalogProductResponse(
                "P-101", "Laptop Gamer XYZ", "Laptop.", "Electrónica", new BigDecimal("1200.00")
        );
        InventoryResponse inventory = new InventoryResponse("P-101", 15, "A-03", 2);
        ExchangeRate exchangeRate = new ExchangeRate(
                "USD", "MXN", new BigDecimal("18.05"), "2026-10-29", "Frankfurter External API"
        );

        when(catalogClient.getProduct("P-101"))
                .thenAnswer(invocation -> waitForRelease(requestsStarted, releaseRequests, product));
        when(inventoryClient.getInventory("P-101"))
                .thenAnswer(invocation -> waitForRelease(requestsStarted, releaseRequests, inventory));
        when(currencyExchangeClient.getUsdExchangeRate())
                .thenAnswer(invocation -> waitForRelease(requestsStarted, releaseRequests, exchangeRate));

        ExecutorService requestExecutor = Executors.newFixedThreadPool(3);
        ExecutorService callerExecutor = Executors.newSingleThreadExecutor();
        try {
            ProductSummaryService service = new ProductSummaryService(
                    catalogClient,
                    inventoryClient,
                    currencyExchangeClient,
                    requestExecutor
            );
            CompletableFuture<ProductSummary> result = CompletableFuture.supplyAsync(
                    () -> service.getProductSummary("P-101"),
                    callerExecutor
            );

            boolean allRequestsStarted = requestsStarted.await(2, TimeUnit.SECONDS);
            releaseRequests.countDown();

            assertTrue(allRequestsStarted, "All dependency calls should overlap");
            assertEquals("SUCCESS", result.get(2, TimeUnit.SECONDS).status());
        } finally {
            releaseRequests.countDown();
            requestExecutor.shutdownNow();
            callerExecutor.shutdownNow();
        }
    }

    private <T> T waitForRelease(
            CountDownLatch requestsStarted,
            CountDownLatch releaseRequests,
            T response
    ) throws InterruptedException {
        requestsStarted.countDown();
        if (!releaseRequests.await(3, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Timed out waiting for concurrent dependency calls");
        }
        return response;
    }
}
