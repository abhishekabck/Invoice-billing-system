package src.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import src.enums.PaymentMethod;

public class Payment {
    private int id;
    private Invoice invoice;
    private BigDecimal amount;
    private PaymentMethod method;
    private LocalDateTime paymentDate;

    public Payment() {
    }

    public Payment(Invoice invoice,
                   BigDecimal amount,
                   PaymentMethod method) {
        this.invoice = invoice;
        this.amount = amount;
        this.method = method;
        this.paymentDate = LocalDateTime.now();
    }

    public Payment(int id, Invoice invoice,
                   BigDecimal amount,
                   PaymentMethod method,
                   LocalDateTime paymentDate) {
        this.id = id;
        this.invoice = invoice;
        this.amount = amount;
        this.method = method;
        this.paymentDate = paymentDate;
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}