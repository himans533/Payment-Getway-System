package com.Payment_Getway.Model;


import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "payments")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;

    private String paymentMethod;

    private String paymentStatus;

    private Double amount;

    private String currency;

    private String failureReason;

    private Boolean webhookReceived;

    private LocalDateTime transactionDate;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}