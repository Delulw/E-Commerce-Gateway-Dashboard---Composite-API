package com.ecommerce.composite.controller;

import com.ecommerce.composite.dto.ProductSummary;
import com.ecommerce.composite.service.ProductSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductSummaryController {

    private final ProductSummaryService productSummaryService;

    public ProductSummaryController(ProductSummaryService productSummaryService) {
        this.productSummaryService = productSummaryService;
    }

    @GetMapping("/{id}/summary")
    public ProductSummary getProductSummary(@PathVariable String id) {
        return productSummaryService.getProductSummary(id);
    }
}
