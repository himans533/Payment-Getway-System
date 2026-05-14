package com.Payment_Getway.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Payment_Getway.Model.Invoice;
import com.Payment_Getway.Model.Order;
import com.Payment_Getway.Model.User;

@Repository
public interface InvoiceRepository
        extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(
            String invoiceNumber
    );

    Optional<Invoice> findByOrder(Order order);

    java.util.List<Invoice> findByOrder_UserOrderByInvoiceDateDesc(
            User user
    );
}
