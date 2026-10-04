package src.repositories;

import src.database.DatabaseConnection;
import src.models.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepository {

    public Service save(Service service) throws SQLException {
        String sql = "INSERT INTO services (name, description, price, is_active, duration_in_hours) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getPrice());
            stmt.setBoolean(4, service.getIsActive());
            stmt.setInt(5, service.getDurationInHours());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) service.setId(rs.getInt(1));
            }
        }
        return service;
    }

    public Service findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, price, is_active, duration_in_hours FROM services WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Service> findAll() throws SQLException {
        String sql = "SELECT id, name, description, price, is_active, duration_in_hours FROM services ORDER BY id";
        List<Service> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean update(Service service) throws SQLException {
        String sql = "UPDATE services SET name=?, description=?, price=?, is_active=?, duration_in_hours=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setBigDecimal(3, service.getPrice());
            stmt.setBoolean(4, service.getIsActive());
            stmt.setInt(5, service.getDurationInHours());
            stmt.setInt(6, service.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM services WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    // Used by InvoiceItemRepository with shared connection
    Service findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, name, description, price, is_active, duration_in_hours FROM services WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private Service mapRow(ResultSet rs) throws SQLException {
        return new Service(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getBoolean("is_active"),
            rs.getInt("duration_in_hours")
        );
    }
}
