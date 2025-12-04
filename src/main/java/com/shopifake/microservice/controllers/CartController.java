package com.shopifake.microservice.controllers;

import com.shopifake.microservice.dtos.AddToCartRequest;
import com.shopifake.microservice.dtos.CartResponse;
import com.shopifake.microservice.dtos.UpdateCartItemRequest;
import com.shopifake.microservice.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for cart management operations.
 */
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Carts", description = "API for managing shopping carts")
public class CartController {

    private static final String SITE_ID_REQUIRED_MESSAGE = "Site ID is required";

    /** The cart service for business logic operations. */
    private final CartService cartService;

    /**
     * Get the cart.
     * For logged-in users: provide X-User-Id header.
     * For guest users: optionally provide X-Session-Id header. If not provided, a sessionId will be auto-generated and returned in the response.
     *
     * @param userId the user ID from header (optional - required for logged-in users)
     * @param sessionId the session ID from header (optional - will be auto-generated if not provided for guest users)
     * @param siteId the site ID (required)
     * @return the cart response (includes sessionId if it was auto-generated)
     */
    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieves the cart for a user or session. For guest users without sessionId, one will be auto-generated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - siteId required")
    })
    public ResponseEntity<CartResponse> getCart(
            @Parameter(description = "User ID (required for logged-in users)") @RequestHeader(value = "X-User-Id", required = false) final UUID userId,
            @Parameter(description = "Session ID (optional for guest users - will be auto-generated if not provided)") @RequestHeader(value = "X-Session-Id", required = false) final String sessionId,
            @Parameter(description = "Site ID") @RequestParam(required = true) final UUID siteId) {

        log.debug("Fetching cart for user: {}, session: {}, site: {}", userId, sessionId, siteId);

        if (siteId == null) {
            log.warn(SITE_ID_REQUIRED_MESSAGE);
            return ResponseEntity.badRequest().build();
        }

        CartResponse response = cartService.getCart(userId, sessionId, siteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Add an item to the cart.
     * For logged-in users: provide X-User-Id header.
     * For guest users: optionally provide X-Session-Id header. If not provided, a sessionId will be auto-generated and returned in the response.
     *
     * @param userId the user ID from header (optional - required for logged-in users)
     * @param sessionId the session ID from header (optional - will be auto-generated if not provided for guest users)
     * @param siteId the site ID (required)
     * @param request the add to cart request
     * @return the updated cart response (includes sessionId if it was auto-generated)
     */
    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product to the cart with specified quantity. For guest users without sessionId, one will be auto-generated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CartResponse> addToCart(
            @Parameter(description = "User ID") @RequestHeader(value = "X-User-Id", required = false) final UUID userId,
            @Parameter(description = "Session ID") @RequestHeader(value = "X-Session-Id", required = false) final String sessionId,
            @Parameter(description = "Site ID") @RequestParam(required = true) final UUID siteId,
            @Valid @RequestBody final AddToCartRequest request) {

        log.info("Received request to add item to cart: productId={}, quantity={}",
                request.getProductId(), request.getQuantity());

        if (siteId == null) {
            log.warn(SITE_ID_REQUIRED_MESSAGE);
            return ResponseEntity.badRequest().build();
        }

        try {
            CartResponse response = cartService.addToCart(userId, sessionId, siteId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add item to cart: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Update a cart item quantity.
     * For logged-in users: provide X-User-Id header.
     * For guest users: provide X-Session-Id header (use the sessionId from previous cart responses).
     *
     * @param userId the user ID from header (optional - required for logged-in users)
     * @param sessionId the session ID from header (optional - required for guest users)
     * @param siteId the site ID (required)
     * @param itemId the cart item ID
     * @param request the update request
     * @return the updated cart response
     */
    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Updates the quantity of a cart item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<CartResponse> updateCartItem(
            @Parameter(description = "User ID") @RequestHeader(value = "X-User-Id", required = false) final UUID userId,
            @Parameter(description = "Session ID") @RequestHeader(value = "X-Session-Id", required = false) final String sessionId,
            @Parameter(description = "Site ID") @RequestParam(required = true) final UUID siteId,
            @Parameter(description = "Cart item ID") @PathVariable final UUID itemId,
            @Valid @RequestBody final UpdateCartItemRequest request) {

        log.info("Updating cart item {} with quantity {}", itemId, request.getQuantity());

        if (siteId == null) {
            log.warn(SITE_ID_REQUIRED_MESSAGE);
            return ResponseEntity.badRequest().build();
        }

        try {
            CartResponse response = cartService.updateCartItem(userId, sessionId, siteId, itemId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update cart item: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Remove an item from the cart.
     * For logged-in users: provide X-User-Id header.
     * For guest users: provide X-Session-Id header (use the sessionId from previous cart responses).
     *
     * @param userId the user ID from header (optional - required for logged-in users)
     * @param sessionId the session ID from header (optional - required for guest users)
     * @param siteId the site ID (required)
     * @param itemId the cart item ID
     * @return the updated cart response
     */
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove cart item", description = "Removes an item from the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<CartResponse> removeCartItem(
            @Parameter(description = "User ID") @RequestHeader(value = "X-User-Id", required = false) final UUID userId,
            @Parameter(description = "Session ID") @RequestHeader(value = "X-Session-Id", required = false) final String sessionId,
            @Parameter(description = "Site ID") @RequestParam(required = true) final UUID siteId,
            @Parameter(description = "Cart item ID") @PathVariable final UUID itemId) {

        log.info("Removing cart item {}", itemId);

        if (siteId == null) {
            log.warn(SITE_ID_REQUIRED_MESSAGE);
            return ResponseEntity.badRequest().build();
        }

        try {
            CartResponse response = cartService.removeCartItem(userId, sessionId, siteId, itemId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to remove cart item: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Clear all items from the cart.
     * For logged-in users: provide X-User-Id header.
     * For guest users: provide X-Session-Id header (use the sessionId from previous cart responses).
     *
     * @param userId the user ID from header (optional - required for logged-in users)
     * @param sessionId the session ID from header (optional - required for guest users)
     * @param siteId the site ID (required)
     * @return the updated cart response
     */
    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    })
    public ResponseEntity<CartResponse> clearCart(
            @Parameter(description = "User ID") @RequestHeader(value = "X-User-Id", required = false) final UUID userId,
            @Parameter(description = "Session ID") @RequestHeader(value = "X-Session-Id", required = false) final String sessionId,
            @Parameter(description = "Site ID") @RequestParam(required = true) final UUID siteId) {

        log.info("Clearing cart for user: {}, session: {}, site: {}", userId, sessionId, siteId);

        if (siteId == null) {
            log.warn(SITE_ID_REQUIRED_MESSAGE);
            return ResponseEntity.badRequest().build();
        }

        CartResponse response = cartService.clearCart(userId, sessionId, siteId);
        return ResponseEntity.ok(response);
    }
}

