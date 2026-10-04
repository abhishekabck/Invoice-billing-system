package src.services;

import src.enums.InvoiceStatus;
import src.enums.PaymentMethod;
import src.enums.PaymentStatus;
import src.models.Invoice;
import src.models.Payment;
import src.repositories.InvoiceRepository;
import src.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final InvoiceRepository invoiceRepo;
    private final InvoiceService invoiceService;

    public PaymentService(PaymentRepository paymentRepo, InvoiceRepository invoiceRepo,
                          InvoiceService invoiceService) {
        this.paymentRepo = paymentRepo;
        this.invoiceRepo = invoiceRepo;
        this.invoiceService = invoiceService;
    }

    public Payment makePayment(Invoice invoice, BigDecimal amount, PaymentMethod method) throws SQLException {
        if (invoice == null) throw new IllegalArgumentException("Invoice cannot be null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        if (method == null) throw new IllegalArgumentException("Payment method cannot be null");

        if (invoice.getStatus() == InvoiceStatus.CANCELLED)
            throw new IllegalStateException("Cannot pay a cancelled invoice");
        if (invoice.getStatus() != InvoiceStatus.ISSUED && invoice.getStatus() != InvoiceStatus.PARTIALLY_PAID)
            throw new IllegalStateException("Invoice must be ISSUED or PARTIALLY_PAID to accept payment");

        BigDecimal remaining = getRemainingAmount(invoice);
        if (amount.compareTo(remaining) > 0)
            throw new IllegalArgumentException(
                "Payment amount (" + amount + ") exceeds remaining balance (" + remaining + ")");

        Payment payment = new Payment(invoice, amount, method);
        paymentRepo.save(payment);

        // Update invoice payment status
        updateInvoicePaymentStatus(invoice);
        return payment;
    }

    public List<Payment> getPaymentsForInvoice(Invoice invoice) throws SQLException {
        return paymentRepo.findByInvoiceId(invoice.getId());
    }

    public BigDecimal getPaidAmount(Invoice invoice) throws SQLException {
        BigDecimal paid = BigDecimal.ZERO;
        for (Payment p : paymentRepo.findByInvoiceId(invoice.getId())) {
            paid = paid.add(p.getAmount());
        }
        return paid;
    }

    public BigDecimal getRemainingAmount(Invoice invoice) throws SQLException {
        BigDecimal total = invoiceService.calculateTotal(invoice);
        BigDecimal paid = getPaidAmount(invoice);
        BigDecimal remaining = total.subtract(paid);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    public List<Payment> getAllPayments() throws SQLException {
        return paymentRepo.findAll();
    }

    private void updateInvoicePaymentStatus(Invoice invoice) throws SQLException {
        BigDecimal total = invoiceService.calculateTotal(invoice);
        BigDecimal paid = getPaidAmount(invoice);

        InvoiceStatus newStatus;
        PaymentStatus newPaymentStatus;
        if (paid.compareTo(total) >= 0) {
            newStatus = InvoiceStatus.PAID;
            newPaymentStatus = PaymentStatus.PAID;
        } else {
            newStatus = InvoiceStatus.PARTIALLY_PAID;
            newPaymentStatus = PaymentStatus.PARTIAL;
        }

        invoiceRepo.updateStatus(invoice.getId(), newStatus, newPaymentStatus);
        invoice.setStatus(newStatus);
        invoice.setPaymentStatus(newPaymentStatus);
    }
}
