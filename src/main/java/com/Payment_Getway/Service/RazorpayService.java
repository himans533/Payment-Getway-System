package com.Payment_Getway.Service;

import java.util.UUID;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@Service
public class RazorpayService {

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    public Order createRazorpayOrder(
            Double amount,
            String receipt
    ) throws Exception {

        RazorpayClient client =
                new RazorpayClient(razorpayKey, razorpaySecret);

        JSONObject options = new JSONObject();

        options.put("amount", Math.round(amount * 100));

        options.put("currency", "INR");

        options.put("receipt",
                receipt == null || receipt.isBlank()
                        ? UUID.randomUUID().toString()
                        : receipt);

        return client.orders.create(options);
    }

    public boolean verifyPaymentSignature(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    ) throws Exception {

        JSONObject attributes = new JSONObject();

        attributes.put("razorpay_order_id", razorpayOrderId);
        attributes.put("razorpay_payment_id", razorpayPaymentId);
        attributes.put("razorpay_signature", razorpaySignature);

        return Utils.verifyPaymentSignature(attributes, razorpaySecret);
    }

    public boolean verifyWebhookSignature(
            String payload,
            String signature,
            String webhookSecret
    ) throws Exception {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            return true;
        }

        return Utils.verifyWebhookSignature(payload, signature, webhookSecret);
    }

    public String getRazorpayKey() {

        return razorpayKey;
    }
}
