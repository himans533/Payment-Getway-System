package com.Payment_Getway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.Payment_Getway.Model.Cart;
import com.Payment_Getway.Model.Payment;
import com.Payment_Getway.Model.Product;
import com.Payment_Getway.Model.User;
import com.Payment_Getway.Repository.InvoiceRepository;
import com.Payment_Getway.Repository.ProductRepository;
import com.Payment_Getway.Repository.UserRepository;
import com.Payment_Getway.Service.CartService;
import com.Payment_Getway.Service.PaymentService;
import com.Payment_Getway.Service.RazorpayService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentFlowServiceTests {

    @Autowired
    private CartService cartService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RazorpayService razorpayService;

    @Test
    void cartAddUpdateRemoveRecalculatesTotals() {

        User user = testUser("cart@example.com");
        Product product = testProduct("Cart Product", 200.00);

        Cart item = cartService.addToCart(user, product.getId(), 2);

        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getTotalPrice()).isEqualTo(400.00);

        item = cartService.addToCart(user, product.getId(), 1);

        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getTotalPrice()).isEqualTo(600.00);

        item = cartService.updateQuantity(user, item.getId(), 5);

        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getTotalPrice()).isEqualTo(1000.00);

        cartService.removeItem(user, item.getId());

        assertThat(cartService.getUserCart(user)).isEmpty();
    }

    @Test
    void checkoutCreatesLocalOrderAndPendingPaymentFromServerCart() throws Exception {

        User user = testUser("checkout@example.com");
        Product product = testProduct("Checkout Product", 150.00);
        cartService.addToCart(user, product.getId(), 2);

        when(razorpayService.createRazorpayOrder(any(Double.class), anyString()))
                .thenReturn(razorpayOrder("order_checkout", "INR"));

        Payment payment = paymentService.createCheckoutPayment(user, "Test Address");

        assertThat(payment.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(payment.getAmount()).isEqualTo(300.00);
        assertThat(payment.getRazorpayOrderId()).isEqualTo("order_checkout");
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo("CREATED");
        assertThat(payment.getOrder().getPaymentStatus()).isEqualTo("PENDING");
    }

    @Test
    void successfulVerificationUpdatesOrderCreatesInvoiceAndClearsCart() throws Exception {

        User user = testUser("success@example.com");
        Product product = testProduct("Success Product", 500.00);
        cartService.addToCart(user, product.getId(), 1);

        when(razorpayService.createRazorpayOrder(any(Double.class), anyString()))
                .thenReturn(razorpayOrder("order_success", "INR"));
        when(razorpayService.verifyPaymentSignature("order_success", "pay_success", "sig_success"))
                .thenReturn(true);

        paymentService.createCheckoutPayment(user, "Test Address");
        Payment verified = paymentService.verifyPayment(
                "order_success",
                "pay_success",
                "sig_success"
        );

        assertThat(verified.getPaymentStatus()).isEqualTo("SUCCESS");
        assertThat(verified.getOrder().getPaymentStatus()).isEqualTo("PAID");
        assertThat(verified.getOrder().getOrderStatus()).isEqualTo("CONFIRMED");
        assertThat(invoiceRepository.findByOrder_UserOrderByInvoiceDateDesc(user)).hasSize(1);
        assertThat(cartService.getUserCart(user)).isEmpty();
    }

    @Test
    void failedVerificationMarksPaymentAndOrderFailedWithoutInvoice() throws Exception {

        User user = testUser("failed@example.com");
        Product product = testProduct("Failed Product", 400.00);
        cartService.addToCart(user, product.getId(), 1);

        when(razorpayService.createRazorpayOrder(any(Double.class), anyString()))
                .thenReturn(razorpayOrder("order_failed", "INR"));
        when(razorpayService.verifyPaymentSignature("order_failed", "pay_failed", "bad_signature"))
                .thenReturn(false);

        paymentService.createCheckoutPayment(user, "Test Address");
        Payment verified = paymentService.verifyPayment(
                "order_failed",
                "pay_failed",
                "bad_signature"
        );

        assertThat(verified.getPaymentStatus()).isEqualTo("FAILED");
        assertThat(verified.getOrder().getPaymentStatus()).isEqualTo("FAILED");
        assertThat(verified.getOrder().getOrderStatus()).isEqualTo("PAYMENT_FAILED");
        assertThat(invoiceRepository.findByOrder_UserOrderByInvoiceDateDesc(user)).isEmpty();
    }

    @Test
    void unauthenticatedDashboardRequestsRedirectToLogin() throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    private User testUser(String email) {

        User user = new User();

        user.setFullName("Test User");
        user.setEmail(email);
        user.setPhone("9999999999");
        user.setPassword("password");
        user.setRole("CUSTOMER");
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private Product testProduct(
            String name,
            Double price
    ) {

        Product product = new Product();

        product.setProductName(name);
        product.setDescription("Test description");
        product.setPrice(price);
        product.setStockQuantity(10);
        product.setCategory("software");
        product.setImageUrl("TP");
        product.setCreatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    private com.razorpay.Order razorpayOrder(
            String id,
            String currency
    ) {

        JSONObject json = new JSONObject();

        json.put("id", id);
        json.put("currency", currency);
        json.put("status", "created");

        return new com.razorpay.Order(json);
    }
}
