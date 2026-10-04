package src.repositories;

import src.database.DatabaseConnection;
import src.enums.InvoiceStatus;
import src.enums.PaymentStatus;
import src.interfaces.Discount;
import src.models.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepository {

    private final CustomerRepository customerRepo;
    private final InvoiceItemRepository itemRepo;

    public InvoiceRepository(CustomerRepository customerRepo, InvoiceItemRepository itemRepo) {
        this.customerRepo = customerRepo;
        this.itemRepo = itemRepo;
    }

    /**
     * Save a new invoice. Invoice number is set after INSERT using the generated id.
     */
    public Invoice save(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices " +
                     "(invoice_number, customer_id, invoice_date, due_date, " +
                     " discount_type, discount_value, tax_amount, subtotal, grand_total, status, payment_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "PENDING");
            stmt.setInt(2, invoice.getCustomer().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(invoice.getInvoiceDate()));
            stmt.setObject(4, invoice.getDueDate()); // DATE, may be null
            setDiscountParams(stmt, 5, invoice.getDiscount(), invoice.getSubtotal());
            stmt.setBigDecimal(7, invoice.getTax() != null ? invoice.getTax() : BigDecimal.ZERO);
            stmt.setBigDecimal(8, invoice.getSubtotal());
            stmt.setBigDecimal(9, invoice.getGrandTotal());
            stmt.setString(10, invoice.getStatus().name());
            stmt.setString(11, invoice.getPaymentStatus().name());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    invoice.setId(rs.getInt(1));
                }
            }
        }
        // Update invoice_number to INV-<padded id>
        String invoiceNumber = String.format("INV-%04d", invoice.getId());
        invoice.setInvoiceNumber(invoiceNumber);
        String updateNum = "UPDATE invoices SET invoice_number = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateNum)) {
            stmt.setString(1, invoiceNumber);
            stmt.setInt(2, invoice.getId());
            stmt.executeUpdate();
        }
        return invoice;
    }

    public Invoice findById(int id) throws SQLException {
        String sql = "SELECT id, invoice_number, customer_id, invoice_date, due_date, " +
                     "discount_type, discount_value, tax_amount, subtotal, grand_total, status, payment_status " +
                     "FROM invoices WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = mapRow(rs);
                    List<InvoiceItem> items = itemRepo.findByInvoiceId(id);
                    invoice.setItems(items);
                    return invoice;
                }
            }
        }
        return null;
    }

    public List<Invoice> findAll() throws SQLException {
        String sql = "SELECT id, invoice_number, customer_id, invoice_date, due_date, " +
                     "discount_type, discount_value, tax_amount, subtotal, grand_total, status, payment_status " +
                     "FROM invoices ORDER BY id";
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Invoice invoice = mapRow(rs);
                invoice.setItems(itemRepo.findByInvoiceId(invoice.getId()));
                list.add(invoice);
            }
        }
        return list;
    }

    public void updateStatus(int invoiceId, InvoiceStatus status, PaymentStatus paymentStatus) throws SQLException {
        String sql = "UPDATE invoices SET status = ?, payment_status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, paymentStatus.name());
            stmt.setInt(3, invoiceId);
            stmt.executeUpdate();
        }
    }

    /** Transaction-aware status update. */
    public void updateStatus(Connection conn, int invoiceId, InvoiceStatus status, PaymentStatus paymentStatus)
            throws SQLException {
        String sql = "UPDATE invoices SET status = ?, payment_status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, paymentStatus.name());
            stmt.setInt(3, invoiceId);
            stmt.executeUpdate();
        }
    }

    public void updateTotals(Invoice invoice) throws SQLException {
        String sql = "UPDATE invoices SET discount_type=?, discount_value=?, subtotal=?, grand_total=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setDiscountParams(stmt, 1, invoice.getDiscount(), invoice.getSubtotal());
            stmt.setBigDecimal(3, invoice.getSubtotal());
            stmt.setBigDecimal(4, invoice.getGrandTotal());
            stmt.setInt(5, invoice.getId());
            stmt.executeUpdate();
        }
    }

    // ---------------------------------------------------------------

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));

        int customerId = rs.getInt("customer_id");
        Customer customer = customerRepo.findById(customerId);
        invoice.setCustomer(customer);

        Timestamp invoiceDate = rs.getTimestamp("invoice_date");
        if (invoiceDate != null) invoice.setInvoiceDate(invoiceDate.toLocalDateTime());

        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) invoice.setDueDate(dueDate.toLocalDate());

        String discountType = rs.getString("discount_type");
        BigDecimal discountValue = rs.getBigDecimal("discount_value");
        Discount discount = buildDiscount(discountType, discountValue);
        if (discount != null) invoice.setDiscount(discount);

        BigDecimal tax = rs.getBigDecimal("tax_amount");
        invoice.setTax(tax != null ? tax : BigDecimal.ZERO);

        invoice.setStatus(InvoiceStatus.valueOf(rs.getString("status")));
        invoice.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));

        // subtotal/grandTotal are recalculated when items are set via setItems()
        return invoice;
    }

    private Discount buildDiscount(String type, BigDecimal value) {
        if (type == null || value == null) return null;
        if ("FLAT".equals(type)) return new FlatDiscount(value, "Flat discount");
        if ("PERCENTAGE".equals(type)) return new PercentageDiscount(value, "Percentage discount");
        return null;
    }

    private void setDiscountParams(PreparedStatement stmt, int startIdx,
                                   Discount discount, BigDecimal subtotal) throws SQLException {
        if (discount instanceof FlatDiscount fd) {
            stmt.setString(startIdx, "FLAT");
            stmt.setBigDecimal(startIdx + 1, fd.getDiscountAmount());
        } else if (discount instanceof PercentageDiscount pd) {
            stmt.setString(startIdx, "PERCENTAGE");
            stmt.setBigDecimal(startIdx + 1, pd.getPercentage());
        } else {
            stmt.setNull(startIdx, Types.VARCHAR);
            stmt.setNull(startIdx + 1, Types.NUMERIC);
        }
    }
}
