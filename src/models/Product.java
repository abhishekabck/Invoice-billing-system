package src.models;

import java.math.BigDecimal;
import src.interfaces.Billable;

public class Product implements Billable {

    private int id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    // private BigDecimal taxRate; // Tax rate as a percentage (e.g., 10 for 10%)
    private int stockQuantity;

    public Product() {
    }

    // Used when creating a new Product.
    public Product(String sku, String name, String description,
                   BigDecimal price, int stockQuantity) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Used when populating the values from Database.
    public Product(int id, String sku, String name, String description,
                   BigDecimal price, int stockQuantity) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    @Override 
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override 
    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}