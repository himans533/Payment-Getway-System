package com.Payment_Getway.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Payment_Getway.Model.Payment;
import com.Payment_Getway.Model.User;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(
            String razorpayOrderId
    );

    Optional<Payment> findByRazorpayPaymentId(
            String razorpayPaymentId
    );

    List<Payment> findByPaymentStatus(
            String paymentStatus
    );

    List<Payment> findByOrder_UserOrderByTransactionDateDesc(
            User user
    );
}
