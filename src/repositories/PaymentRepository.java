package src.repositories;

import src.database.DatabaseConnection;
import src.enums.PaymentMethod;
import src.models.Invoice;
import src.models.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepository {

    public Payment save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (invoice_id, amount, method, payment_date) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, payment.getInvoice().getId());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setString(3, payment.getMethod().name());
            stmt.setTimestamp(4, Timestamp.valueOf(payment.getPaymentDate()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) payment.setId(rs.getInt(1));
            }
        }
        return payment;
    }

    public Payment findById(int id) throws SQLException {
        String sql = "SELECT id, invoice_id, amount, method, payment_date FROM payments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Payment> findByInvoiceId(int invoiceId) throws SQLException {
        String sql = "SELECT id, invoice_id, amount, method, payment_date FROM payments WHERE invoice_id = ? ORDER BY id";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT id, invoice_id, amount, method, payment_date FROM payments ORDER BY id";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        // Lightweight Invoice reference — only id needed for association
        Invoice invoice = new Invoice();
        invoice.setId(rs.getInt("invoice_id"));

        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setInvoice(invoice);
        p.setAmount(rs.getBigDecimal("amount"));
        p.setMethod(PaymentMethod.valueOf(rs.getString("method")));
        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) p.setPaymentDate(ts.toLocalDateTime());
        return p;
    }
}
