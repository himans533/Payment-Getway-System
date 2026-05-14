package com.Payment_Getway.Config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.Payment_Getway.Model.Product;
import com.Payment_Getway.Repository.ProductRepository;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedProducts(ProductRepository productRepository) {

        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            productRepository.saveAll(List.of(
                    product(
                            "Payment Gateway Starter Kit",
                            "Spring Boot + Thymeleaf payment integration template.",
                            999.00,
                            25,
                            "software",
                            "PG"
                    ),
                    product(
                            "Spring Boot Payment Course",
                            "Learn Razorpay, orders, payments, invoices, and security.",
                            1499.00,
                            30,
                            "course",
                            "SB"
                    ),
                    product(
                            "Invoice Automation Service",
                            "Generate professional payment receipts and invoices.",
                            799.00,
                            40,
                            "service",
                            "IN"
                    ),
                    product(
                            "Premium Merchant Plan",
                            "Advanced reports, transaction tracking, and priority support.",
                            2499.00,
                            15,
                            "subscription",
                            "PM"
                    )
            ));
        };
    }

    private Product product(
            String name,
            String description,
            Double price,
            Integer stock,
            String category,
            String imageUrl
    ) {

        Product product = new Product();

        product.setProductName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setCreatedAt(LocalDateTime.now());

        return product;
    }
}
