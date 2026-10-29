package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.model.Product;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final Map<String, Product> products = Map.of(
            "P-101", new Product(
                    "P-101",
                    "Laptop Gamer XYZ",
                    "Laptop para videojuegos y tareas de alto rendimiento.",
                    "Electrónica",
                    new BigDecimal("1200.00")
            )
    );

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }
}
