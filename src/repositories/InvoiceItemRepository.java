package src.repositories;

import src.database.DatabaseConnection;
import src.interfaces.Billable;
import src.models.InvoiceItem;
import src.models.Product;
import src.models.Service;
import java.math.BigDecimal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceItemRepository {

    public InvoiceItemRepository() {}

    public InvoiceItem save(InvoiceItem item, int invoiceId) throws SQLException {
        String sql = "INSERT INTO invoice_items " +
                     "(invoice_id, item_type, item_ref_id, item_name, item_description, unit_price, quantity, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            stmt.setString(2, item.getItemType());
            stmt.setInt(3, item.getItemRefId());
            stmt.setString(4, item.getProduct().getName());
            stmt.setString(5, item.getProduct().getDescription());
            stmt.setBigDecimal(6, item.getUnitPrice());
            stmt.setInt(7, item.getQuantity());
            stmt.setBigDecimal(8, item.getTotalPrice());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) item.setId(rs.getInt(1));
            }
        }
        return item;
    }

    /** Transaction-aware version: uses a shared connection. */
    public InvoiceItem save(Connection conn, InvoiceItem item, int invoiceId) throws SQLException {
        String sql = "INSERT INTO invoice_items " +
                     "(invoice_id, item_type, item_ref_id, item_name, item_description, unit_price, quantity, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            stmt.setString(2, item.getItemType());
            stmt.setInt(3, item.getItemRefId());
            stmt.setString(4, item.getProduct().getName());
            stmt.setString(5, item.getProduct().getDescription());
            stmt.setBigDecimal(6, item.getUnitPrice());
            stmt.setInt(7, item.getQuantity());
            stmt.setBigDecimal(8, item.getTotalPrice());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) item.setId(rs.getInt(1));
            }
        }
        return item;
    }

    public List<InvoiceItem> findByInvoiceId(int invoiceId) throws SQLException {
        String sql = "SELECT id, item_type, item_ref_id, item_name, item_description, " +
                     "unit_price, quantity, total_price FROM invoice_items WHERE invoice_id = ? ORDER BY id";
        List<InvoiceItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean delete(int itemId) throws SQLException {
        String sql = "DELETE FROM invoice_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        }
    }

    private InvoiceItem mapRow(ResultSet rs) throws SQLException {
        String itemType = rs.getString("item_type");
        int itemRefId = rs.getInt("item_ref_id");
        String itemName = rs.getString("item_name");
        String itemDesc = rs.getString("item_description");
        BigDecimal unitPrice = rs.getBigDecimal("unit_price");
        int quantity = rs.getInt("quantity");

        // Reconstruct Billable from stored historical data (name/price preserved)
        Billable billable;
        if ("PRODUCT".equals(itemType)) {
            billable = new Product(itemRefId, "", itemName, itemDesc, unitPrice, 0);
        } else {
            billable = new Service(itemRefId, itemName, itemDesc, unitPrice, true, 0);
        }

        InvoiceItem item = new InvoiceItem(billable, quantity, unitPrice, itemType, itemRefId);
        item.setId(rs.getInt("id"));
        return item;
    }
}
