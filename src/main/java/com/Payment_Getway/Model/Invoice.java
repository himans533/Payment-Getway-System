package com.Payment_Getway.Model;


import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "invoices")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;

    private LocalDateTime invoiceDate;

    private Double totalAmount;

    private Double taxAmount;

    private String pdfPath;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}