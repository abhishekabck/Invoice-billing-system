package src.repositories;

import src.database.DatabaseConnection;
import src.models.Inventory;
import src.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryRepository {

    private final ProductRepository productRepo;

    public InventoryRepository(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public Inventory save(Inventory inventory) throws SQLException {
        String sql = "INSERT INTO inventory (product_id, quantity) VALUES (?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventory.getProduct().getId());
            stmt.setInt(2, inventory.getQuantity());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) inventory.setId(rs.getInt(1));
            }
        }
        return inventory;
    }

    public Inventory findByProductId(int productId) throws SQLException {
        String sql = "SELECT id, product_id, quantity, updated_at FROM inventory WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs, conn);
            }
        }
        return null;
    }

    public List<Inventory> findAll() throws SQLException {
        String sql = "SELECT id, product_id, quantity, updated_at FROM inventory ORDER BY id";
        List<Inventory> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs, conn));
        }
        return list;
    }

    public void updateQuantity(int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE inventory SET quantity = ?, updated_at = NOW() WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    /** Transaction-aware: uses a shared connection (caller manages commit/rollback). */
    public void updateQuantity(Connection conn, int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE inventory SET quantity = ?, updated_at = NOW() WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    /** Transaction-aware: lock the inventory row for update. */
    public int getQuantityForUpdate(Connection conn, int productId) throws SQLException {
        String sql = "SELECT quantity FROM inventory WHERE product_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No inventory record for product id=" + productId);
            }
        }
    }

    private Inventory mapRow(ResultSet rs, Connection conn) throws SQLException {
        int productId = rs.getInt("product_id");
        Product product = productRepo.findById(conn, productId);
        if (product == null) product = new Product();
        Inventory inv = new Inventory();
        inv.setId(rs.getInt("id"));
        inv.setProduct(product);
        inv.setQuantity(rs.getInt("quantity"));
        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) inv.setUpdatedAt(ts.toLocalDateTime());
        return inv;
    }
}
