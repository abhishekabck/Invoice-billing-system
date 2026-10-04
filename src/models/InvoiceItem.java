package src.models;

import java.math.BigDecimal;
import src.interfaces.Billable;

public class InvoiceItem {
    private int id;
    private Billable product;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String itemType;   // "PRODUCT" or "SERVICE"
    private int itemRefId;     // id of the product or service

    public InvoiceItem() {}

    public InvoiceItem(Billable product, int quantity, BigDecimal unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public InvoiceItem(Billable product, int quantity, BigDecimal unitPrice, String itemType, int itemRefId) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.itemType = itemType;
        this.itemRefId = itemRefId;
    }

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Billable getProduct() { return product; }
    public void setProduct(Billable product) { this.product = product; }

    /** Alias for getProduct() — used by InvoiceService */
    public Billable getItem() { return product; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculate();
    }

    public BigDecimal getUnitPrice() { return unitPrice; }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculate();
    }

    public BigDecimal getTotalPrice() { return totalPrice; }

    /** Alias for getTotalPrice() — used by InvoiceService */
    public BigDecimal getSubtotal() { return totalPrice; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public int getItemRefId() { return itemRefId; }
    public void setItemRefId(int itemRefId) { this.itemRefId = itemRefId; }

    private void recalculate() {
        if (unitPrice != null && quantity > 0) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
