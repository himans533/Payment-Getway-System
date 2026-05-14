package com.Payment_Getway.Controller;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Payment_Getway.Model.Invoice;
import com.Payment_Getway.Model.Order;
import com.Payment_Getway.Model.User;
import com.Payment_Getway.Service.CartService;
import com.Payment_Getway.Service.InvoiceService;
import com.Payment_Getway.Service.OrderService;
import com.Payment_Getway.Service.PaymentService;
import com.Payment_Getway.Service.ProductService;
import com.Payment_Getway.Service.RazorpayService;
import com.Payment_Getway.Service.UserService;

@Controller
public class DashboardController {

    private final UserService userService;

    private final ProductService productService;

    private final CartService cartService;

    private final OrderService orderService;

    private final PaymentService paymentService;

    private final InvoiceService invoiceService;

    private final RazorpayService razorpayService;

    public DashboardController(
            UserService userService,
            ProductService productService,
            CartService cartService,
            OrderService orderService,
            PaymentService paymentService,
            InvoiceService invoiceService,
            RazorpayService razorpayService
    ) {

        this.userService = userService;
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
        this.razorpayService = razorpayService;
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        model.addAttribute("paymentCount", paymentService.getPaymentsByUser(user).size());
        model.addAttribute("orderCount", orderService.getOrdersByUser(user).size());
        model.addAttribute("invoiceCount", invoiceService.getInvoicesByUser(user).size());
        model.addAttribute("cartItemCount", cartService.countCartItems(user));

        return "dashboard/dashboard";
    }

    @GetMapping("/dashboard/payments")
    public String paymentsPage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        model.addAttribute("payments", paymentService.getPaymentsByUser(user));
        model.addAttribute("capturedCount", paymentService.countByUserAndStatus(user, "SUCCESS"));
        model.addAttribute("pendingCount", paymentService.countByUserAndStatus(user, "PENDING"));
        model.addAttribute("failedCount", paymentService.countByUserAndStatus(user, "FAILED"));

        return "dashboard/payments";
    }

    @GetMapping("/dashboard/orders")
    public String ordersPage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        List<Order> orders = orderService.getOrdersByUser(user);

        model.addAttribute("orders", orders);
        model.addAttribute("createdCount", countOrderStatus(orders, "CREATED"));
        model.addAttribute("confirmedCount", countOrderStatus(orders, "CONFIRMED"));
        model.addAttribute("failedCount", countOrderStatus(orders, "PAYMENT_FAILED"));

        return "dashboard/orders";
    }

    @GetMapping("/dashboard/invoice")
    public String invoicePage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        List<Invoice> invoices = invoiceService.getInvoicesByUser(user);

        model.addAttribute("invoices", invoices);
        model.addAttribute("invoiceCount", invoices.size());
        model.addAttribute(
                "taxTotal",
                invoices.stream().mapToDouble(Invoice::getTaxAmount).sum()
        );
        model.addAttribute(
                "billingTotal",
                invoices.stream().mapToDouble(Invoice::getTotalAmount).sum()
        );

        return "dashboard/invoice";
    }

    @GetMapping("/dashboard/settings")
    public String settingsPage(Principal principal, Model model) {

        addUserContext(principal, model);

        return "dashboard/settings";
    }

    @GetMapping("/dashboard/products")
    public String productsPage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("cartItemCount", cartService.countCartItems(user));

        return "dashboard/products";
    }

    @GetMapping("/dashboard/cart")
    public String cartPage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        addCartContext(user, model);

        return "dashboard/cart";
    }

    @GetMapping("/dashboard/checkout")
    public String checkoutPage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        addCartContext(user, model);
        model.addAttribute("razorpayKey", razorpayService.getRazorpayKey());

        return "dashboard/checkout";
    }

    @GetMapping("/dashboard/payment-result")
    public String paymentResultPage(
            Principal principal,
            @RequestParam(defaultValue = "failed") String status,
            @RequestParam(required = false) String orderId,
            Model model
    ) {

        addUserContext(principal, model);
        model.addAttribute("status", status);
        model.addAttribute("orderId", orderId);

        return "dashboard/payment-result";
    }

    @GetMapping("/dashboard/history")
    public String historyPage(Principal principal, Model model) {

        User user = addUserContext(principal, model);

        model.addAttribute("events", buildHistoryEvents(user));

        return "dashboard/history";
    }
    

    private User addUserContext(Principal principal, Model model) {

        User user = userService.getRequiredUserByEmail(principal.getName());

        model.addAttribute("email", principal.getName());

        return user;
    }

    private void addCartContext(User user, Model model) {

        model.addAttribute("cartItems", cartService.getUserCart(user));
        model.addAttribute("cartTotal", cartService.calculateCartTotal(user));
        model.addAttribute("cartItemCount", cartService.countCartItems(user));
    }

    private long countOrderStatus(
            List<Order> orders,
            String status
    ) {

        return orders.stream()
                .filter(order -> status.equalsIgnoreCase(order.getOrderStatus()))
                .count();
    }

    private List<HistoryEvent> buildHistoryEvents(User user) {

        Stream<HistoryEvent> orderEvents = orderService.getOrdersByUser(user)
                .stream()
                .map(order -> new HistoryEvent(
                        order.getCreatedAt(),
                        "Order " + order.getOrderId(),
                        "Status " + order.getOrderStatus()
                                + " with payment " + order.getPaymentStatus()
                ));

        Stream<HistoryEvent> paymentEvents = paymentService.getPaymentsByUser(user)
                .stream()
                .map(payment -> new HistoryEvent(
                        payment.getTransactionDate(),
                        "Payment " + payment.getPaymentStatus(),
                        "Razorpay order " + payment.getRazorpayOrderId()
                ));

        Stream<HistoryEvent> invoiceEvents = invoiceService.getInvoicesByUser(user)
                .stream()
                .map(invoice -> new HistoryEvent(
                        invoice.getInvoiceDate(),
                        "Invoice " + invoice.getInvoiceNumber(),
                        "Billing total INR " + invoice.getTotalAmount()
                ));

        return Stream.concat(Stream.concat(orderEvents, paymentEvents), invoiceEvents)
                .sorted(Comparator.comparing(HistoryEvent::date).reversed())
                .toList();
    }

    public record HistoryEvent(
            java.time.LocalDateTime date,
            String title,
            String description
    ) {
    }
}
