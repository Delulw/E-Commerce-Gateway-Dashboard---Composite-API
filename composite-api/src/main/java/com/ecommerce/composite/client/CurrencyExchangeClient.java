package com.ecommerce.composite.client;

import com.ecommerce.composite.dto.ExchangeRate;
import com.ecommerce.composite.dto.FrankfurterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CurrencyExchangeClient {

    private static final String BASE_CURRENCY = "USD";
    private static final String SOURCE = "Frankfurter External API";

    private final RestClient restClient;
    private final String targetCurrency;

    public CurrencyExchangeClient(
            RestClient.Builder builder,
            @Value("${exchange-rate.base-url}") String baseUrl,
            @Value("${exchange-rate.target-currency}") String targetCurrency
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.targetCurrency = targetCurrency;
    }

    public ExchangeRate getUsdExchangeRate() {
        FrankfurterResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/latest")
                        .queryParam("from", BASE_CURRENCY)
                        .queryParam("to", targetCurrency)
                        .build())
                .retrieve()
                .body(FrankfurterResponse.class);

        BigDecimal rate = response == null || response.rates() == null
                ? null
                : response.rates().get(targetCurrency);

        if (rate == null) {
            throw new IllegalStateException(
                    "Frankfurter response did not contain a rate for " + targetCurrency
            );
        }

        return new ExchangeRate(
                BASE_CURRENCY,
                targetCurrency,
                rate,
                response.date(),
                SOURCE
        );
    }
}
