package com.ecommerce.composite.dto;

import java.math.BigDecimal;

public record ExchangeRate(
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        String rateDate,
        String source
) {
}
