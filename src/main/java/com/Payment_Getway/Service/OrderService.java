package com.Payment_Getway.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Payment_Getway.Model.Order;
import com.Payment_Getway.Model.User;
import com.Payment_Getway.Repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Order order) {

        order.setOrderId(
                UUID.randomUUID().toString()
        );

        order.setCreatedAt(LocalDateTime.now());

        order.setOrderStatus("CREATED");

        order.setPaymentStatus("PENDING");

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderByOrderId(
            String orderId
    ) {

        return orderRepository.findByOrderId(orderId);
    }

    public List<Order> getOrdersByUser(User user) {

        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long countByUserAndPaymentStatus(
            User user,
            String paymentStatus
    ) {

        return orderRepository.findByUser(user)
                .stream()
                .filter(order -> paymentStatus.equalsIgnoreCase(order.getPaymentStatus()))
                .count();
    }
}
