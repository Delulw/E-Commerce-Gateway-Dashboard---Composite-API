package com.ecommerce.composite.client;

import com.ecommerce.composite.dto.InventoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class InventoryClientTest {

    private MockRestServiceServer mockServer;
    private InventoryClient inventoryClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        inventoryClient = new InventoryClient(builder, "http://inventory.test");
    }

    @Test
    void returnsInventoryFromInventoryService() {
        mockServer.expect(requestTo("http://inventory.test/microservices/inventory/P-101"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "productId": "P-101",
                          "availableUnits": 15,
                          "warehouseLocation": "Almacén central - A-03",
                          "estimatedDeliveryDays": 2
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        InventoryResponse result = inventoryClient.getInventory("P-101");

        assertEquals("P-101", result.productId());
        assertEquals(15, result.availableUnits());
        assertEquals("Almacén central - A-03", result.warehouseLocation());
        assertEquals(2, result.estimatedDeliveryDays());
        mockServer.verify();
    }

    @Test
    void propagatesNotFoundFromInventoryService() {
        mockServer.expect(requestTo("http://inventory.test/microservices/inventory/UNKNOWN"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(
                HttpClientErrorException.class,
                () -> inventoryClient.getInventory("UNKNOWN")
        );
        mockServer.verify();
    }
}
