package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.InventoryServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = InventoryServiceApplication.class)
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsInventoryWhenProductIdExists() throws Exception {
        mockMvc.perform(get("/microservices/inventory/P-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("P-101"))
                .andExpect(jsonPath("$.availableUnits").value(15))
                .andExpect(jsonPath("$.warehouseLocation").value("Almacén central - A-03"))
                .andExpect(jsonPath("$.estimatedDeliveryDays").value(2));
    }

    @Test
    void returnsNotFoundWhenProductIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/microservices/inventory/UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}
