package com.shopifake.microservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for cart item response.
 * Contains only product identifiers and quantity.
 * Frontend should fetch product details (name, price, etc.) from catalog and pricing services.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private UUID id;
    private UUID productId;
    private Integer quantity;
}

