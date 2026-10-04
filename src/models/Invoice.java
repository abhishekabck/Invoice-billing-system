package src.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import src.enums.InvoiceStatus;
import src.enums.PaymentStatus;
import src.interfaces.Discount;

public class Invoice {

    private int id;
    private String invoiceNumber;
    private Customer customer;
    private LocalDateTime invoiceDate;
    private LocalDate dueDate;
    private List<InvoiceItem> items;
    private Discount discount;
    private BigDecimal tax;
    private BigDecimal subtotal;
    private BigDecimal grandTotal;
    private InvoiceStatus status;
    private PaymentStatus paymentStatus;

    public Invoice() {
        this.items = new ArrayList<>();
        this.tax = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.grandTotal = BigDecimal.ZERO;
    }

    public Invoice(String invoiceNumber, Customer customer, LocalDate dueDate) {
        this.invoiceNumber = invoiceNumber;
        this.customer = customer;
        this.dueDate = dueDate;
        this.items = new ArrayList<>();
        this.invoiceDate = LocalDateTime.now();
        this.status = InvoiceStatus.DRAFT;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.tax = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.grandTotal = BigDecimal.ZERO;
    }

    public void recalculate() {
        this.subtotal = items.stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = (discount != null)
                ? discount.calculateDiscount(subtotal)
                : BigDecimal.ZERO;
        this.grandTotal = subtotal.subtract(discountAmount).add(tax);
        if (this.grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            this.grandTotal = BigDecimal.ZERO;
        }
    }

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDateTime getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDateTime invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public List<InvoiceItem> getItems() { return items; }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
        recalculate();
    }

    public void addItem(InvoiceItem item) {
        this.items.add(item);
        recalculate();
    }

    public void removeItem(int itemId) {
        items.removeIf(i -> i.getId() == itemId);
        recalculate();
    }

    public Discount getDiscount() { return discount; }

    public void setDiscount(Discount discount) {
        this.discount = discount;
        recalculate();
    }

    public BigDecimal getDiscountAmount() {
        if (discount == null || subtotal == null) return BigDecimal.ZERO;
        return discount.calculateDiscount(subtotal);
    }

    public BigDecimal getTax() { return tax; }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
        recalculate();
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getGrandTotal() { return grandTotal; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}
