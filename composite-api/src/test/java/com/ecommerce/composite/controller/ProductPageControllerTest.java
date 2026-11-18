package com.ecommerce.composite.controller;

import com.ecommerce.composite.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProductPageController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {
        "auth.username=test-user",
        "auth.password=test-password",
        "auth.jwt-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "auth.access-token-ttl-seconds=900",
        "auth.refresh-token-ttl-seconds=3600",
        "auth.cookie-secure=false"
})
class ProductPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesProductSummaryPageAtRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Encuentra lo que buscas"
                )))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "product-search-form"
                )));
    }
}
