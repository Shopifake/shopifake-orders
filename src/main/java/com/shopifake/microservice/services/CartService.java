package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.AddToCartRequest;
import com.shopifake.microservice.dtos.CartItemResponse;
import com.shopifake.microservice.dtos.CartResponse;
import com.shopifake.microservice.dtos.UpdateCartItemRequest;
import com.shopifake.microservice.entities.Cart;
import com.shopifake.microservice.entities.CartItem;
import com.shopifake.microservice.repositories.CartItemRepository;
import com.shopifake.microservice.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing shopping carts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * Get or create a cart for a user or session.
     * If neither userId nor sessionId is provided, a sessionId will be auto-generated.
     *
     * @param userId the user ID (nullable for guest carts)
     * @param sessionId the session ID (nullable - will be auto-generated if not provided and userId is null)
     * @param siteId the site ID
     * @return the cart
     */
    @Transactional
    public Cart getOrCreateCart(final UUID userId, final String sessionId, final UUID siteId) {
        if (userId != null) {
            // Logged-in user cart - sessionId not needed
            Optional<Cart> existingCart = cartRepository.findByUserIdAndSiteId(userId, siteId);
            if (existingCart.isPresent()) {
                log.debug("Found existing cart for user: {} and site: {}", userId, siteId);
                return existingCart.get();
            }
            log.info("Creating new cart for user: {} and site: {}", userId, siteId);
            return createCart(userId, null, siteId);
        } else {
            // Guest cart - use provided sessionId or generate one
            String effectiveSessionId = (sessionId != null && !sessionId.isBlank()) 
                    ? sessionId 
                    : UUID.randomUUID().toString();
            
            Optional<Cart> existingCart = cartRepository.findBySessionIdAndSiteId(effectiveSessionId, siteId);
            if (existingCart.isPresent()) {
                log.debug("Found existing cart for session: {} and site: {}", effectiveSessionId, siteId);
                return existingCart.get();
            }
            log.info("Creating new cart for session: {} and site: {}", effectiveSessionId, siteId);
            return createCart(null, effectiveSessionId, siteId);
        }
    }

    /**
     * Create a new cart.
     *
     * @param userId the user ID (nullable)
     * @param sessionId the session ID (nullable)
     * @param siteId the site ID
     * @return the created cart
     */
    private Cart createCart(final UUID userId, final String sessionId, final UUID siteId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .sessionId(sessionId)
                .siteId(siteId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartRepository.save(cart);
    }

    /**
     * Add an item to the cart.
     *
     * @param userId the user ID (nullable)
     * @param sessionId the session ID (nullable)
     * @param siteId the site ID
     * @param request the add to cart request
     * @return the updated cart response
     */
    @Transactional
    public CartResponse addToCart(
            final UUID userId,
            final String sessionId,
            final UUID siteId,
            final AddToCartRequest request) {

        log.info("Adding product {} with quantity {} to cart",
                request.getProductId(), request.getQuantity());

        Cart cart = getOrCreateCart(userId, sessionId, siteId);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCart_IdAndProductId(
                cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            // Update quantity of existing item
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
            log.debug("Updated existing cart item quantity to: {}", newQuantity);
        } else {
            // Create new cart item
            // Frontend should fetch current prices from pricing service
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
            log.debug("Created new cart item");
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    /**
     * Get the cart.
     *
     * @param userId the user ID (nullable)
     * @param sessionId the session ID (nullable)
     * @param siteId the site ID
     * @return the cart response
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(final UUID userId, final String sessionId, final UUID siteId) {
        Cart cart = getOrCreateCart(userId, sessionId, siteId);
        // Explicitly fetch items to avoid LazyInitializationException
        cart.getItems().size(); // Force lazy loading within transaction
        return mapToResponse(cart);
    }

    /**
     * Update a cart item quantity.
     *
     * @param userId the user ID (nullable)
     * @param sessionId the session ID (nullable)
     * @param siteId the site ID
     * @param itemId the cart item ID
     * @param request the update request
     * @return the updated cart response
     */
    @Transactional
    public CartResponse updateCartItem(
            final UUID userId,
            final String sessionId,
            final UUID siteId,
            final UUID itemId,
            final UpdateCartItemRequest request) {

        log.info("Updating cart item {} quantity to {}", itemId, request.getQuantity());

        Cart cart = getOrCreateCart(userId, sessionId, siteId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to this cart");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    /**
     * Remove an item from the cart.
     *
     * @param userId the user ID (nullable)
     * @param sessionId the session ID (nullable)
     * @param siteId the site ID
     * @param itemId the cart item ID
     * @return the updated cart response
     */
    @Transactional
    public CartResponse removeCartItem(
            final UUID userId,
            final String sessionId,
            final UUID siteId,
            final UUID itemId) {

        log.info("Removing cart item {}", itemId);

        Cart cart = getOrCreateCart(userId, sessionId, siteId);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to this cart");
        }

        cartItemRepository.delete(item);
        cart.getItems().remove(item);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    /**
     * Clear all items from the cart.
     *
     * @param userId the user ID (nullable)
     * @param sessionId the session ID (nullable)
     * @param siteId the site ID
     * @return the updated cart response
     */
    @Transactional
    public CartResponse clearCart(final UUID userId, final String sessionId, final UUID siteId) {
        log.info("Clearing cart for user: {}, session: {}, site: {}", userId, sessionId, siteId);

        Cart cart = getOrCreateCart(userId, sessionId, siteId);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    /**
     * Map Cart entity to CartResponse DTO.
     *
     * @param cart the cart entity
     * @return the cart response DTO
     */
    private CartResponse mapToResponse(final Cart cart) {
        // Explicitly fetch items to avoid LazyInitializationException
        // This ensures items are loaded even if the cart was fetched without items
        List<CartItem> items = cartItemRepository.findByCart_Id(cart.getId());

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .siteId(cart.getSiteId())
                .items(itemResponses)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * Map CartItem entity to CartItemResponse DTO.
     * Returns only product identifiers and quantity.
     * Frontend should fetch product details (name, price, etc.) from catalog and pricing services.
     *
     * @param item the cart item entity
     * @return the cart item response DTO
     */
    private CartItemResponse mapItemToResponse(final CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .build();
    }
}

