package com.Payment_Getway.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Payment_Getway.Model.WebhookLog;

@Repository
public interface WebhookLogRepository
        extends JpaRepository<WebhookLog, Long> {
}