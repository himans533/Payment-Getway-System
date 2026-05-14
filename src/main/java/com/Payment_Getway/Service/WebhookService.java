package com.Payment_Getway.Service;

import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Payment_Getway.Model.Payment;
import com.Payment_Getway.Model.WebhookLog;
import com.Payment_Getway.Repository.PaymentRepository;
import com.Payment_Getway.Repository.WebhookLogRepository;

@Service
public class WebhookService {

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Value("${razorpay.webhook-secret:}")
    private String webhookSecret;

    public WebhookLog saveWebhookLog(
            WebhookLog webhookLog
    ) {

        webhookLog.setReceivedAt(
                LocalDateTime.now()
        );

        return webhookLogRepository.save(webhookLog);
    }

    @Transactional
    public WebhookLog handleWebhook(
            String payload,
            String signature
    ) {

        WebhookLog log = new WebhookLog();

        log.setPayload(payload);
        log.setProcessed(false);

        try {
            JSONObject json = new JSONObject(payload);
            String event = json.optString("event", "unknown");

            log.setEventType(event);

            if (webhookSecret == null || webhookSecret.isBlank()) {
                return saveWebhookLog(log);
            }

            boolean verified = razorpayService.verifyWebhookSignature(
                    payload,
                    signature,
                    webhookSecret
            );

            if (!verified) {
                return saveWebhookLog(log);
            }

            markWebhookReceived(json);
            log.setProcessed(true);

            return saveWebhookLog(log);
        } catch (Exception exception) {
            log.setEventType("invalid");

            return saveWebhookLog(log);
        }
    }

    private void markWebhookReceived(JSONObject json) {

        JSONObject payload = json.optJSONObject("payload");

        if (payload == null) {
            return;
        }

        JSONObject payment = payload.optJSONObject("payment");

        if (payment == null) {
            return;
        }

        JSONObject paymentEntity = payment.optJSONObject("entity");

        if (paymentEntity == null) {
            return;
        }

        String razorpayOrderId = paymentEntity.optString("order_id", "");

        if (razorpayOrderId.isBlank()) {
            return;
        }

        paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .ifPresent(this::markPaymentWebhookReceived);
    }

    private void markPaymentWebhookReceived(Payment payment) {

        payment.setWebhookReceived(true);
        paymentRepository.save(payment);
    }
}
