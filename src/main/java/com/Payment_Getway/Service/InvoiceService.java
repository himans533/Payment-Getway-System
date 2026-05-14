package com.Payment_Getway.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Payment_Getway.Model.Invoice;
import com.Payment_Getway.Model.Order;
import com.Payment_Getway.Model.User;
import com.Payment_Getway.Repository.InvoiceRepository;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    public Invoice saveInvoice(Invoice invoice) {

        invoice.setInvoiceDate(
                LocalDateTime.now()
        );

        return invoiceRepository.save(invoice);
    }

    public Invoice createInvoiceForOrder(Order order) {

        Optional<Invoice> existingInvoice =
                invoiceRepository.findByOrder(order);

        if (existingInvoice.isPresent()) {
            return existingInvoice.get();
        }

        Invoice invoice = new Invoice();

        invoice.setOrder(order);
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setTaxAmount(round(order.getTotalAmount() * 0.18));
        invoice.setPdfPath(null);

        return saveInvoice(invoice);
    }

    public List<Invoice> getInvoicesByUser(User user) {

        return invoiceRepository.findByOrder_UserOrderByInvoiceDateDesc(user);
    }

    private Double round(Double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}
