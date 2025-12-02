package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for CartItem entity operations.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find a cart item by cart ID and product ID.
     *
     * @param cartId the cart ID
     * @param productId the product ID
     * @return Optional containing the cart item if found
     */
    Optional<CartItem> findByCart_IdAndProductId(UUID cartId, UUID productId);

    /**
     * Find all cart items by cart ID.
     *
     * @param cartId the cart ID
     * @return list of cart items
     */
    List<CartItem> findByCart_Id(UUID cartId);
}

