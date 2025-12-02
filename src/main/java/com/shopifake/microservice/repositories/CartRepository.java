package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Cart entity operations.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Find a cart by user ID and site ID.
     *
     * @param userId the user ID
     * @param siteId the site ID
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByUserIdAndSiteId(UUID userId, UUID siteId);

    /**
     * Find a cart by session ID and site ID.
     *
     * @param sessionId the session ID
     * @param siteId the site ID
     * @return Optional containing the cart if found
     */
    Optional<Cart> findBySessionIdAndSiteId(String sessionId, UUID siteId);

    /**
     * Check if a cart exists for a user and site.
     *
     * @param userId the user ID
     * @param siteId the site ID
     * @return true if a cart exists
     */
    boolean existsByUserIdAndSiteId(UUID userId, UUID siteId);

    /**
     * Check if a cart exists for a session and site.
     *
     * @param sessionId the session ID
     * @param siteId the site ID
     * @return true if a cart exists
     */
    boolean existsBySessionIdAndSiteId(String sessionId, UUID siteId);
}

