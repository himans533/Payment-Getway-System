package com.Payment_Getway.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Payment_Getway.Model.Cart;
import com.Payment_Getway.Model.Product;
import com.Payment_Getway.Model.User;
import com.Payment_Getway.Repository.CartRepository;
import com.Payment_Getway.Repository.ProductRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Cart addToCart(
            User user,
            Long productId,
            Integer quantity
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        int safeQuantity = normalizeQuantity(quantity);

        Cart cart = cartRepository.findByUserAndProduct(user, product)
                .orElseGet(Cart::new);

        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(
                cart.getId() == null
                        ? safeQuantity
                        : cart.getQuantity() + safeQuantity
        );

        recalculate(cart);

        return cartRepository.save(cart);
    }

    public List<Cart> getUserCart(User user) {

        return cartRepository.findByUser(user);
    }

    @Transactional
    public Cart updateQuantity(
            User user,
            Long cartId,
            Integer quantity
    ) {

        Cart cart = getOwnedCartItem(user, cartId);
        cart.setQuantity(normalizeQuantity(quantity));
        recalculate(cart);

        return cartRepository.save(cart);
    }

    @Transactional
    public void removeItem(
            User user,
            Long cartId
    ) {

        cartRepository.delete(getOwnedCartItem(user, cartId));
    }

    @Transactional
    public void clearCart(User user) {

        cartRepository.deleteAll(cartRepository.findByUser(user));
    }

    public Double calculateCartTotal(User user) {

        return cartRepository.findByUser(user)
                .stream()
                .mapToDouble(Cart::getTotalPrice)
                .sum();
    }

    public int countCartItems(User user) {

        return cartRepository.findByUser(user)
                .stream()
                .mapToInt(Cart::getQuantity)
                .sum();
    }

    private Cart getOwnedCartItem(
            User user,
            Long cartId
    ) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));

        if (cart.getUser() == null
                || cart.getUser().getId() == null
                || !cart.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to the current user.");
        }

        return cart;
    }

    private void recalculate(Cart cart) {

        cart.setTotalPrice(
                cart.getQuantity()
                        * cart.getProduct().getPrice()
        );
    }

    private int normalizeQuantity(Integer quantity) {

        if (quantity == null || quantity < 1) {
            return 1;
        }

        return Math.min(quantity, 99);
    }
}
