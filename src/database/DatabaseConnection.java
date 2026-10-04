package src.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
        System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/invoice_db");
    private static final String USER =
        System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String PASSWORD =
        System.getenv().getOrDefault("DB_PASSWORD", "");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC driver not found. Add postgresql-*.jar to classpath.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
