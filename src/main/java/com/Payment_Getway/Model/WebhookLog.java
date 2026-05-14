package com.Payment_Getway.Model;


import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "webhook_logs")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Boolean processed;

    private LocalDateTime receivedAt;
}