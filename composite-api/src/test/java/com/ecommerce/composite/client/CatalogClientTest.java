package com.ecommerce.composite.client;

import com.ecommerce.composite.dto.CatalogProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class CatalogClientTest {

    private MockRestServiceServer mockServer;
    private CatalogClient catalogClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        catalogClient = new CatalogClient(builder, "http://catalog.test");
    }

    @Test
    void returnsProductFromCatalogService() {
        mockServer.expect(requestTo("http://catalog.test/microservices/products/P-101"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "P-101",
                          "name": "Laptop Gamer XYZ",
                          "description": "Laptop para videojuegos.",
                          "category": "Electrónica",
                          "priceUsd": 1200.00
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        CatalogProductResponse result = catalogClient.getProduct("P-101");

        assertEquals("P-101", result.id());
        assertEquals("Laptop Gamer XYZ", result.name());
        assertEquals(0, new BigDecimal("1200.00").compareTo(result.priceUsd()));
        mockServer.verify();
    }

    @Test
    void propagatesNotFoundFromCatalogService() {
        mockServer.expect(requestTo("http://catalog.test/microservices/products/UNKNOWN"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(
                HttpClientErrorException.class,
                () -> catalogClient.getProduct("UNKNOWN")
        );
        mockServer.verify();
    }
}
