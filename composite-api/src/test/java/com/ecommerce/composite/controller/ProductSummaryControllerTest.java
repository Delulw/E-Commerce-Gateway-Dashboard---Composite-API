package com.ecommerce.composite.controller;

import com.ecommerce.composite.dto.ProductSummary;
import com.ecommerce.composite.service.ProductSummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductSummaryController.class)
class ProductSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductSummaryService productSummaryService;

    @Test
    void returnsConsolidatedSummaryAsSnakeCaseJson() throws Exception {
        when(productSummaryService.getProductSummary("P-101")).thenReturn(new ProductSummary(
                "P-101",
                "Laptop Gamer XYZ",
                "Laptop para videojuegos.",
                "Electrónica",
                new BigDecimal("1200.00"),
                new BigDecimal("21660.00"),
                "IN_STOCK",
                15,
                "Almacén central - A-03",
                2,
                "Frankfurter External API",
                "SUCCESS"
        ));

        mockMvc.perform(get("/api/v1/products/P-101/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product_id").value("P-101"))
                .andExpect(jsonPath("$.title").value("Laptop Gamer XYZ"))
                .andExpect(jsonPath("$.description").value("Laptop para videojuegos."))
                .andExpect(jsonPath("$.category").value("Electrónica"))
                .andExpect(jsonPath("$.price_usd").value(1200.00))
                .andExpect(jsonPath("$.price_mxn").value(21660.00))
                .andExpect(jsonPath("$.stock_status").value("IN_STOCK"))
                .andExpect(jsonPath("$.available_units").value(15))
                .andExpect(jsonPath("$.warehouse_location").value("Almacén central - A-03"))
                .andExpect(jsonPath("$.estimated_delivery_days").value(2))
                .andExpect(jsonPath("$.currency_conversion_source")
                        .value("Frankfurter External API"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void reportsNotFoundWhenDependencyDoesNotFindProduct() throws Exception {
        when(productSummaryService.getProductSummary("UNKNOWN"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        null
                ));

        mockMvc.perform(get("/api/v1/products/UNKNOWN/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void reportsBadGatewayWhenDependencyCannotBeReached() throws Exception {
        when(productSummaryService.getProductSummary("P-101"))
                .thenThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(get("/api/v1/products/P-101/summary"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.detail")
                        .value("A dependent service could not be reached."));
    }
}
