package com.ecommerce.composite.client;

import com.ecommerce.composite.dto.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CurrencyExchangeClientTest {

    private MockRestServiceServer mockServer;
    private CurrencyExchangeClient currencyExchangeClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        currencyExchangeClient = new CurrencyExchangeClient(
                builder,
                "https://api.frankfurter.app",
                "MXN"
        );
    }

    @Test
    void returnsExchangeRateWhenProviderIncludesTargetCurrency() {
        mockServer.expect(requestTo(
                        "https://api.frankfurter.app/latest?from=USD&to=MXN"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "amount": 1.0,
                          "base": "USD",
                          "date": "2026-10-29",
                          "rates": {
                            "MXN": 18.05
                          }
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        ExchangeRate result = currencyExchangeClient.getUsdExchangeRate();

        assertEquals("USD", result.baseCurrency());
        assertEquals("MXN", result.targetCurrency());
        assertEquals(0, result.rate().compareTo(new BigDecimal("18.05")));
        assertEquals("2026-10-29", result.rateDate());
        assertEquals("Frankfurter External API", result.source());
        mockServer.verify();
    }

    @Test
    void failsClearlyWhenProviderDoesNotReturnTargetCurrency() {
        mockServer.expect(requestTo(
                        "https://api.frankfurter.app/latest?from=USD&to=MXN"
                ))
                .andRespond(withSuccess(
                        """
                        {
                          "base": "USD",
                          "date": "2026-10-29",
                          "rates": {}
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> currencyExchangeClient.getUsdExchangeRate()
        );

        assertTrue(exception.getMessage().contains("MXN"));
        mockServer.verify();
    }

    @Test
    void propagatesProviderServerErrors() {
        mockServer.expect(requestTo(
                        "https://api.frankfurter.app/latest?from=USD&to=MXN"
                ))
                .andRespond(withServerError());

        assertThrows(
                HttpServerErrorException.class,
                () -> currencyExchangeClient.getUsdExchangeRate()
        );
        mockServer.verify();
    }
}
