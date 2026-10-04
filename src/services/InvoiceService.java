package src.services;

import src.database.DatabaseConnection;
import src.enums.InvoiceStatus;
import src.enums.PaymentStatus;
import src.interfaces.Discount;
import src.models.*;
import src.repositories.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final InvoiceItemRepository itemRepo;
    private final InventoryRepository inventoryRepo;

    public InvoiceService(InvoiceRepository invoiceRepo,
                          InvoiceItemRepository itemRepo,
                          InventoryRepository inventoryRepo) {
        this.invoiceRepo = invoiceRepo;
        this.itemRepo = itemRepo;
        this.inventoryRepo = inventoryRepo;
    }

    public Invoice createInvoice(Customer customer) throws SQLException {
        return createInvoice(customer, null);
    }

    public Invoice createInvoice(Customer customer, LocalDate dueDate) throws SQLException {
        if (customer == null) throw new IllegalArgumentException("Customer cannot be null");
        Invoice invoice = new Invoice("PENDING", customer, dueDate);
        return invoiceRepo.save(invoice);
    }

    public Invoice getInvoice(int id) throws SQLException {
        return invoiceRepo.findById(id);
    }

    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceRepo.findAll();
    }

    /**
     * Add a product item to a DRAFT invoice.
     * Stock is NOT deducted here — only on finalization.
     */
    public void addProductItem(Invoice invoice, Product product, int quantity) throws SQLException {
        validateInvoiceEditable(invoice);
        if (product == null) throw new IllegalArgumentException("Product cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");

        // Check stock availability (but do not deduct yet)
        int currentStock = inventoryRepo.findByProductId(product.getId()) != null
                ? inventoryRepo.findByProductId(product.getId()).getQuantity() : 0;
        if (currentStock < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName()
                    + " (available: " + currentStock + ")");
        }

        InvoiceItem item = new InvoiceItem(product, quantity, product.getPrice(), "PRODUCT", product.getId());
        itemRepo.save(item, invoice.getId());
        invoice.addItem(item);
        invoiceRepo.updateTotals(invoice);
    }

    /**
     * Add a service item to a DRAFT invoice.
     */
    public void addServiceItem(Invoice invoice, Service service, int quantity) throws SQLException {
        validateInvoiceEditable(invoice);
        if (service == null) throw new IllegalArgumentException("Service cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");

        InvoiceItem item = new InvoiceItem(service, quantity, service.getPrice(), "SERVICE", service.getId());
        itemRepo.save(item, invoice.getId());
        invoice.addItem(item);
        invoiceRepo.updateTotals(invoice);
    }

    public void removeItem(Invoice invoice, int itemId) throws SQLException {
        validateInvoiceEditable(invoice);
        itemRepo.delete(itemId);
        invoice.removeItem(itemId);
        invoiceRepo.updateTotals(invoice);
    }

    public void applyDiscount(Invoice invoice, Discount discount) throws SQLException {
        validateInvoiceEditable(invoice);
        invoice.setDiscount(discount);
        invoiceRepo.updateTotals(invoice);
    }

    public BigDecimal calculateSubtotal(Invoice invoice) {
        return invoice.getItems().stream()
                .map(InvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateDiscount(Invoice invoice) {
        if (invoice.getDiscount() == null) return BigDecimal.ZERO;
        return invoice.getDiscount().calculateDiscount(calculateSubtotal(invoice));
    }

    public BigDecimal calculateTotal(Invoice invoice) {
        BigDecimal subtotal = calculateSubtotal(invoice);
        BigDecimal discount = calculateDiscount(invoice);
        BigDecimal total = subtotal.subtract(discount);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    /**
     * Finalize invoice: check stock, deduct stock, update status — all in one JDBC transaction.
     */
    public void finalizeInvoice(Invoice invoice) throws SQLException {
        if (invoice == null) throw new IllegalArgumentException("Invoice cannot be null");
        if (invoice.getItems().isEmpty()) throw new IllegalStateException("Cannot finalize an empty invoice");
        if (invoice.getStatus() == InvoiceStatus.CANCELLED)
            throw new IllegalStateException("Cannot finalize a cancelled invoice");
        if (invoice.getStatus() == InvoiceStatus.ISSUED || invoice.getStatus() == InvoiceStatus.PAID)
            throw new IllegalStateException("Invoice is already finalized");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check and deduct stock for all product items
                for (InvoiceItem item : invoice.getItems()) {
                    if ("PRODUCT".equals(item.getItemType())) {
                        int available = inventoryRepo.getQuantityForUpdate(conn, item.getItemRefId());
                        if (available < item.getQuantity()) {
                            throw new IllegalStateException(
                                "Insufficient stock for: " + item.getProduct().getName()
                                + " (needed: " + item.getQuantity() + ", available: " + available + ")");
                        }
                        inventoryRepo.updateQuantity(conn, item.getItemRefId(), available - item.getQuantity());
                    }
                }
                // Update invoice status
                invoiceRepo.updateStatus(conn, invoice.getId(), InvoiceStatus.ISSUED, PaymentStatus.UNPAID);
                conn.commit();
                invoice.setStatus(InvoiceStatus.ISSUED);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void cancelInvoice(Invoice invoice) throws SQLException {
        if (invoice == null) throw new IllegalArgumentException("Invoice cannot be null");
        if (invoice.getStatus() == InvoiceStatus.PAID)
            throw new IllegalStateException("Paid invoice cannot be cancelled");
        invoiceRepo.updateStatus(invoice.getId(), InvoiceStatus.CANCELLED, PaymentStatus.CANCELLED);
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setPaymentStatus(PaymentStatus.CANCELLED);
    }

    private void validateInvoiceEditable(Invoice invoice) {
        if (invoice == null) throw new IllegalArgumentException("Invoice cannot be null");
        if (invoice.getStatus() != InvoiceStatus.DRAFT)
            throw new IllegalStateException("Invoice can only be edited in DRAFT status");
    }
}
