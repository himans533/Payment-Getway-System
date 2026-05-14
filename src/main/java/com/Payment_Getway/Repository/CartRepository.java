package com.Payment_Getway.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Payment_Getway.Model.Cart;
import com.Payment_Getway.Model.Product;
import com.Payment_Getway.Model.User;

@Repository
public interface CartRepository
        extends JpaRepository<Cart, Long> {

    List<Cart> findByUser(User user);

    Optional<Cart> findByUserAndProduct(
            User user,
            Product product
    );
}
