package src.services;

import src.models.Product;
import src.repositories.InventoryRepository;
import src.repositories.ProductRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    public ProductService(ProductRepository productRepo, InventoryRepository inventoryRepo) {
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
    }

    public Product createProduct(String sku, String name, String description, BigDecimal price, int initialStock)
            throws SQLException {
        validateProductData(sku, name, price);
        if (productRepo.findBySku(sku) != null) {
            throw new IllegalArgumentException("Product with SKU already exists: " + sku);
        }
        Product product = new Product(sku, name, description, price, 0);
        productRepo.save(product);

        // Create inventory record for the product
        src.models.Inventory inventory = new src.models.Inventory(product, Math.max(0, initialStock));
        inventoryRepo.save(inventory);

        return product;
    }

    public Product getProduct(int id) throws SQLException {
        return productRepo.findById(id);
    }

    public Product findBySku(String sku) throws SQLException {
        return productRepo.findBySku(sku);
    }

    public List<Product> getAllProducts() throws SQLException {
        return productRepo.findAll();
    }

    public boolean updateProduct(int id, String sku, String name, String description, BigDecimal price)
            throws SQLException {
        Product product = productRepo.findById(id);
        if (product == null) return false;
        validateProductData(sku, name, price);
        Product existing = productRepo.findBySku(sku);
        if (existing != null && existing.getId() != id) {
            throw new IllegalArgumentException("Product with SKU already exists: " + sku);
        }
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        return productRepo.update(product);
    }

    public boolean deleteProduct(int id) throws SQLException {
        return productRepo.delete(id);
    }

    private void validateProductData(String sku, String name, BigDecimal price) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU cannot be empty");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name cannot be empty");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price cannot be negative");
    }
}
