package com.example.ecommercecheckout.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional
    public Cart createCart() {
        return cartRepository.save(new Cart());
    }

    @Transactional
    public Cart addItem(Long cartId, AddCartItemRequest request) {
        Cart cart = getCart(cartId);
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductName(request.getProductName());
        item.setUnitPrice(request.getUnitPrice());
        item.setQuantity(request.getQuantity());
        cart.getItems().add(item);
        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));
    }
}
