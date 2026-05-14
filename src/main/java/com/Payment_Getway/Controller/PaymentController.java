package com.Payment_Getway.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.Payment_Getway.Model.Payment;
import com.Payment_Getway.Service.PaymentService;
import com.Payment_Getway.Service.RazorpayService;
import com.Payment_Getway.Service.UserService;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private RazorpayService razorpayService;

    @GetMapping("/checkout")
    public String checkoutPage() {

        return "redirect:/dashboard/checkout";
    }

    @PostMapping("/create-order")
    @ResponseBody
    public Map<String, Object> createRazorpayOrder(
            Principal principal,
            @RequestParam(required = false) String shippingAddress
    ) throws Exception {

        Payment payment = paymentService.createCheckoutPayment(
                userService.getRequiredUserByEmail(principal.getName()),
                shippingAddress
        );

        Map<String, Object> response = new HashMap<>();

        response.put("key", razorpayService.getRazorpayKey());
        response.put("orderId", payment.getRazorpayOrderId());
        response.put("localOrderId", payment.getOrder().getOrderId());
        response.put("amount", Math.round(payment.getAmount() * 100));
        response.put("currency", payment.getCurrency());
        response.put("status", payment.getPaymentStatus());

        return response;
    }

    @PostMapping("/verify")
    @ResponseBody
    public Map<String, Object> verifyPayment(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature
    ) {

        Payment payment = paymentService.verifyPayment(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature
        );

        boolean success = "SUCCESS".equalsIgnoreCase(payment.getPaymentStatus());

        Map<String, Object> response = new HashMap<>();

        response.put("success", success);
        response.put("status", payment.getPaymentStatus());
        response.put("orderId", payment.getOrder().getOrderId());
        response.put(
                "redirectUrl",
                "/dashboard/payment-result?status="
                        + (success ? "success" : "failed")
                        + "&orderId="
                        + payment.getOrder().getOrderId()
        );

        return response;
    }

    @PostMapping("/failed")
    @ResponseBody
    public Map<String, Object> markFailed(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam(defaultValue = "Payment cancelled or failed.") String reason
    ) {

        Payment payment = paymentService.markFailedByRazorpayOrderId(
                razorpayOrderId,
                reason
        );

        Map<String, Object> response = new HashMap<>();

        response.put("success", false);
        response.put("status", payment.getPaymentStatus());
        response.put(
                "redirectUrl",
                "/dashboard/payment-result?status=failed&orderId="
                        + payment.getOrder().getOrderId()
        );

        return response;
    }
}
