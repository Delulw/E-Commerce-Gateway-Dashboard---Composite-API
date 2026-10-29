package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.CatalogServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CatalogServiceApplication.class)
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsProductWhenIdExists() throws Exception {
        mockMvc.perform(get("/microservices/products/P-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("P-101"))
                .andExpect(jsonPath("$.name").value("Laptop Gamer XYZ"))
                .andExpect(jsonPath("$.description")
                        .value("Laptop para videojuegos y tareas de alto rendimiento."))
                .andExpect(jsonPath("$.category").value("Electrónica"))
                .andExpect(jsonPath("$.priceUsd").value(1200.00));
    }

    @Test
    void returnsNotFoundWhenIdDoesNotExist() throws Exception {
        mockMvc.perform(get("/microservices/products/UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}
