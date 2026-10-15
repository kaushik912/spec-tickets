package com.example.ecommercecheckout.cart;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/carts")
    @ResponseStatus(HttpStatus.CREATED)
    public Cart createCart() {
        return cartService.createCart();
    }

    @PostMapping("/carts/{cartId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Cart addItem(@PathVariable Long cartId, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(cartId, request);
    }

    @GetMapping("/carts/{cartId}")
    public Cart getCart(@PathVariable Long cartId) {
        return cartService.getCart(cartId);
    }
}
