package src.models;

import java.time.LocalDateTime;

public class Inventory {
    private int id;
    private Product product;
    private int quantity;
    private LocalDateTime updatedAt;

    public Inventory() {
    }

    public Inventory(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public Inventory(int id, Product product, int quantity,
        LocalDateTime updatedAt) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (this.quantity < quantity) throw new IllegalStateException("Insufficient stock");
        this.quantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasStock(int quantity) {
        return this.quantity >= quantity;
    }
}
