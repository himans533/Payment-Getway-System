package com.Payment_Getway.Model;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderId;

    private Double totalAmount;

    private String orderStatus;

    private String paymentStatus;

    private String shippingAddress;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "order",
            cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "order",
            cascade = CascadeType.ALL)
    private Invoice invoice;
}