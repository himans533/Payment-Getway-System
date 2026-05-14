package com.Payment_Getway.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Payment_Getway.Model.Cart;
import com.Payment_Getway.Model.Order;
import com.Payment_Getway.Model.Payment;
import com.Payment_Getway.Model.User;
import com.Payment_Getway.Repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private InvoiceService invoiceService;

    public Payment savePayment(Payment payment) {

        payment.setTransactionDate(
                LocalDateTime.now()
        );

        payment.setPaymentStatus("SUCCESS");

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment createCheckoutPayment(
            User user,
            String shippingAddress
    ) throws Exception {

        List<Cart> cartItems = cartService.getUserCart(user);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }

        Double totalAmount = cartItems.stream()
                .mapToDouble(Cart::getTotalPrice)
                .sum();

        Order localOrder = new Order();

        localOrder.setUser(user);
        localOrder.setTotalAmount(round(totalAmount));
        localOrder.setShippingAddress(
                shippingAddress == null || shippingAddress.isBlank()
                        ? "Address not provided"
                        : shippingAddress
        );

        localOrder = orderService.createOrder(localOrder);

        com.razorpay.Order razorpayOrder =
                razorpayService.createRazorpayOrder(
                        localOrder.getTotalAmount(),
                        localOrder.getOrderId()
                );

        Payment payment = new Payment();

        payment.setOrder(localOrder);
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setPaymentMethod("RAZORPAY");
        payment.setPaymentStatus("PENDING");
        payment.setAmount(localOrder.getTotalAmount());
        payment.setCurrency(razorpayOrder.get("currency"));
        payment.setWebhookReceived(false);
        payment.setTransactionDate(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    ) {

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment order not found."));

        try {
            boolean validSignature = razorpayService.verifyPaymentSignature(
                    razorpayOrderId,
                    razorpayPaymentId,
                    razorpaySignature
            );

            if (!validSignature) {
                return markFailed(payment, "Invalid Razorpay signature.");
            }

            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setRazorpaySignature(razorpaySignature);
            payment.setPaymentStatus("SUCCESS");
            payment.setFailureReason(null);
            payment.setTransactionDate(LocalDateTime.now());

            Order order = payment.getOrder();
            order.setPaymentStatus("PAID");
            order.setOrderStatus("CONFIRMED");

            invoiceService.createInvoiceForOrder(order);
            cartService.clearCart(order.getUser());

            return paymentRepository.save(payment);
        } catch (Exception exception) {
            return markFailed(payment, "Payment verification failed.");
        }
    }

    @Transactional
    public Payment markFailedByRazorpayOrderId(
            String razorpayOrderId,
            String failureReason
    ) {

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment order not found."));

        return markFailed(payment, failureReason);
    }

    public Optional<Payment> getPaymentByRazorpayPaymentId(
            String paymentId
    ) {

        return paymentRepository
                .findByRazorpayPaymentId(paymentId);
    }

    public List<Payment> getPaymentsByUser(User user) {

        return paymentRepository.findByOrder_UserOrderByTransactionDateDesc(user);
    }

    public long countByUserAndStatus(
            User user,
            String paymentStatus
    ) {

        return getPaymentsByUser(user)
                .stream()
                .filter(payment -> paymentStatus.equalsIgnoreCase(payment.getPaymentStatus()))
                .count();
    }

    private Payment markFailed(
            Payment payment,
            String failureReason
    ) {

        payment.setPaymentStatus("FAILED");
        payment.setFailureReason(failureReason);
        payment.setTransactionDate(LocalDateTime.now());

        if (payment.getOrder() != null) {
            payment.getOrder().setPaymentStatus("FAILED");
            payment.getOrder().setOrderStatus("PAYMENT_FAILED");
        }

        return paymentRepository.save(payment);
    }

    private Double round(Double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}
