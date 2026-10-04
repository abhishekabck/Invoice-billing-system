package src.services;

import src.models.Inventory;
import src.models.Product;
import src.repositories.InventoryRepository;

import java.sql.SQLException;
import java.util.List;

public class InventoryService {

    private final InventoryRepository inventoryRepo;

    public InventoryService(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    public Inventory getInventory(Product product) throws SQLException {
        return inventoryRepo.findByProductId(product.getId());
    }

    public List<Inventory> getAllInventories() throws SQLException {
        return inventoryRepo.findAll();
    }

    public void addStock(Product product, int quantity) throws SQLException {
        validateQuantity(quantity);
        Inventory inventory = inventoryRepo.findByProductId(product.getId());
        if (inventory == null) throw new IllegalArgumentException("No inventory record for product: " + product.getName());
        inventory.addStock(quantity);
        inventoryRepo.updateQuantity(product.getId(), inventory.getQuantity());
    }

    public void removeStock(Product product, int quantity) throws SQLException {
        validateQuantity(quantity);
        Inventory inventory = inventoryRepo.findByProductId(product.getId());
        if (inventory == null) throw new IllegalArgumentException("No inventory record for product: " + product.getName());
        inventory.removeStock(quantity); // throws if insufficient
        inventoryRepo.updateQuantity(product.getId(), inventory.getQuantity());
    }

    public boolean hasStock(Product product, int quantity) throws SQLException {
        validateQuantity(quantity);
        Inventory inventory = inventoryRepo.findByProductId(product.getId());
        return inventory != null && inventory.hasStock(quantity);
    }

    public int getStock(Product product) throws SQLException {
        Inventory inventory = inventoryRepo.findByProductId(product.getId());
        if (inventory == null) throw new IllegalArgumentException("No inventory record for product: " + product.getName());
        return inventory.getQuantity();
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");
    }
}
