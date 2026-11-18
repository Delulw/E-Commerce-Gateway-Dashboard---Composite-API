package com.ecommerce.composite.controller;

import com.ecommerce.composite.dto.ProductSummary;
import com.ecommerce.composite.service.ProductSummaryService;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "auth.username=test-user",
        "auth.password=test-password",
        "auth.jwt-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "auth.access-token-ttl-seconds=900",
        "auth.refresh-token-ttl-seconds=3600",
        "auth.cookie-secure=false"
})
@AutoConfigureMockMvc
class AuthenticationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductSummaryService productSummaryService;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void deniesRequestsWithoutOrWithInvalidAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/products/P-101/summary"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/products/P-101/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        Instant now = Instant.now();
        String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer("ecommerce-composite-api")
                        .subject("test-user")
                        .issuedAt(now.minusSeconds(120))
                        .expiresAt(now.minusSeconds(60))
                        .build()
        )).getTokenValue();
        mockMvc.perform(get("/api/v1/products/P-101/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginIssuesAccessTokenAndRefreshRotatesItsCookie() throws Exception {
        when(productSummaryService.getProductSummary("P-101")).thenReturn(sampleSummary());
        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"username":"test-user","password":"test-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String accessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        String oldRefreshCookie = cookieValue(login.getResponse().getHeader(HttpHeaders.SET_COOKIE));
        mockMvc.perform(get("/api/v1/products/P-101/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product_id").value("P-101"));

        MvcResult refresh = mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", oldRefreshCookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        String newRefreshCookie = cookieValue(refresh.getResponse().getHeader(HttpHeaders.SET_COOKIE));
        assertNotEquals(oldRefreshCookie, newRefreshCookie);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", oldRefreshCookie)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsIncorrectCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"username":"test-user","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String cookieValue(String setCookieHeader) {
        return setCookieHeader.substring("refresh_token=".length(), setCookieHeader.indexOf(';'));
    }

    private ProductSummary sampleSummary() {
        return new ProductSummary(
                "P-101", "Laptop", "Laptop de prueba", "Electrónica",
                new BigDecimal("1000.00"), new BigDecimal("18000.00"),
                "IN_STOCK", 10, "Almacén central", 2, "Frankfurter", "SUCCESS"
        );
    }
}
