package src;

import src.enums.PaymentMethod;
import src.models.*;
import src.repositories.*;
import src.services.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // Repositories
    private static final CustomerRepository customerRepo    = new CustomerRepository();
    private static final ProductRepository  productRepo     = new ProductRepository();
    private static final ServiceRepository  serviceRepo     = new ServiceRepository();
    private static final InventoryRepository inventoryRepo  = new InventoryRepository(productRepo);
    private static final InvoiceItemRepository itemRepo     = new InvoiceItemRepository();
    private static final InvoiceRepository  invoiceRepo     = new InvoiceRepository(customerRepo, itemRepo);
    private static final PaymentRepository  paymentRepo     = new PaymentRepository();

    // Services
    private static final CustomerService  customerService  = new CustomerService(customerRepo);
    private static final ProductService   productService   = new ProductService(productRepo, inventoryRepo);
    private static final ServiceService   serviceService   = new ServiceService(serviceRepo);
    private static final InventoryService inventoryService = new InventoryService(inventoryRepo);
    private static final InvoiceService   invoiceService   = new InvoiceService(invoiceRepo, itemRepo, inventoryRepo);
    private static final PaymentService   paymentService   = new PaymentService(paymentRepo, invoiceRepo, invoiceService);

    public static void main(String[] args) {
        System.out.println("\n========================================");
        System.out.println("      INVOICE BILLING SYSTEM");
        System.out.println("========================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Customer Management");
            System.out.println("2. Product Management");
            System.out.println("3. Service Management");
            System.out.println("4. Inventory Management");
            System.out.println("5. Invoice Management");
            System.out.println("6. Make Payment");
            System.out.println("7. View Invoice Details");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == -1) break;  // EOF — exit cleanly
            try {
                switch (choice) {
                    case 1 -> customerMenu();
                    case 2 -> productMenu();
                    case 3 -> serviceMenu();
                    case 4 -> inventoryMenu();
                    case 5 -> invoiceMenu();
                    case 6 -> makePaymentFlow();
                    case 7 -> viewInvoiceDetails();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (SQLException e) {
                System.out.println("[DB Error] " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("[Error] " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    // ===========================================================
    // CUSTOMER MENU
    // ===========================================================
    private static void customerMenu() throws SQLException {
        System.out.println("\n--- CUSTOMERS ---");
        System.out.println("1. Create customer");
        System.out.println("2. List customers");
        System.out.println("3. Update customer");
        System.out.println("4. Delete customer");
        System.out.print("Choose: ");
        int choice = readInt();
        switch (choice) {
            case 1 -> createCustomerFlow();
            case 2 -> listCustomers();
            case 3 -> updateCustomerFlow();
            case 4 -> deleteCustomerFlow();
            default -> System.out.println("Invalid option.");
        }
    }

    private static void createCustomerFlow() throws SQLException {
        System.out.println("\n-- New Customer --");
        String name    = readString("Name: ");
        String phone   = readString("Phone: ");
        String email   = readString("Email: ");
        String address = readString("Address: ");
        String gst     = readString("GST (leave blank if none): ");
        Customer c = customerService.createCustomer(name, phone, email, address, gst);
        System.out.println("Customer created. ID=" + c.getId() + ", Name=" + c.getName());
    }

    private static void listCustomers() throws SQLException {
        List<Customer> list = customerService.getAllCustomers();
        if (list.isEmpty()) { System.out.println("No customers found."); return; }
        System.out.printf("%-5s %-25s %-15s %-25s%n", "ID", "Name", "Phone", "Email");
        System.out.println("-".repeat(72));
        for (Customer c : list) {
            System.out.printf("%-5d %-25s %-15s %-25s%n", c.getId(), c.getName(),
                    nvl(c.getPhone()), nvl(c.getEmail()));
        }
    }

    private static void updateCustomerFlow() throws SQLException {
        listCustomers();
        int id = readIntPrompt("Customer ID to update: ");
        Customer existing = customerService.getCustomer(id);
        if (existing == null) { System.out.println("Customer not found."); return; }
        String name    = readStringOr("Name [" + existing.getName() + "]: ", existing.getName());
        String phone   = readStringOr("Phone [" + nvl(existing.getPhone()) + "]: ", existing.getPhone());
        String email   = readStringOr("Email [" + nvl(existing.getEmail()) + "]: ", existing.getEmail());
        String address = readStringOr("Address [" + nvl(existing.getAddress()) + "]: ", existing.getAddress());
        boolean ok = customerService.updateCustomer(id, name, phone, email, address);
        System.out.println(ok ? "Customer updated." : "Update failed.");
    }

    private static void deleteCustomerFlow() throws SQLException {
        listCustomers();
        int id = readIntPrompt("Customer ID to delete: ");
        boolean ok = customerService.deleteCustomer(id);
        System.out.println(ok ? "Customer deleted." : "Customer not found.");
    }

    // ===========================================================
    // PRODUCT MENU
    // ===========================================================
    private static void productMenu() throws SQLException {
        System.out.println("\n--- PRODUCTS ---");
        System.out.println("1. Create product");
        System.out.println("2. List products");
        System.out.println("3. Update product");
        System.out.println("4. Delete product");
        System.out.print("Choose: ");
        int choice = readInt();
        switch (choice) {
            case 1 -> createProductFlow();
            case 2 -> listProducts();
            case 3 -> updateProductFlow();
            case 4 -> deleteProductFlow();
            default -> System.out.println("Invalid option.");
        }
    }

    private static void createProductFlow() throws SQLException {
        System.out.println("\n-- New Product --");
        String sku   = readString("SKU: ");
        String name  = readString("Name: ");
        String desc  = readString("Description: ");
        BigDecimal price = readDecimal("Price: ");
        int stock = readIntPrompt("Initial stock quantity: ");
        Product p = productService.createProduct(sku, name, desc, price, stock);
        System.out.println("Product created. ID=" + p.getId() + ", SKU=" + p.getSku());
    }

    private static void listProducts() throws SQLException {
        List<Product> list = productService.getAllProducts();
        if (list.isEmpty()) { System.out.println("No products found."); return; }
        System.out.printf("%-5s %-12s %-25s %12s%n", "ID", "SKU", "Name", "Price");
        System.out.println("-".repeat(58));
        for (Product p : list) {
            System.out.printf("%-5d %-12s %-25s %12.2f%n", p.getId(), p.getSku(), p.getName(), p.getPrice());
        }
    }

    private static void updateProductFlow() throws SQLException {
        listProducts();
        int id = readIntPrompt("Product ID to update: ");
        Product existing = productService.getProduct(id);
        if (existing == null) { System.out.println("Product not found."); return; }
        String sku   = readStringOr("SKU [" + existing.getSku() + "]: ", existing.getSku());
        String name  = readStringOr("Name [" + existing.getName() + "]: ", existing.getName());
        String desc  = readStringOr("Description: ", existing.getDescription());
        System.out.print("Price [" + existing.getPrice() + "]: ");
        BigDecimal price = readDecimalOr(existing.getPrice());
        boolean ok = productService.updateProduct(id, sku, name, desc, price);
        System.out.println(ok ? "Product updated." : "Update failed.");
    }

    private static void deleteProductFlow() throws SQLException {
        listProducts();
        int id = readIntPrompt("Product ID to delete: ");
        boolean ok = productService.deleteProduct(id);
        System.out.println(ok ? "Product deleted." : "Product not found.");
    }

    // ===========================================================
    // SERVICE MENU
    // ===========================================================
    private static void serviceMenu() throws SQLException {
        System.out.println("\n--- SERVICES ---");
        System.out.println("1. Create service");
        System.out.println("2. List services");
        System.out.print("Choose: ");
        int choice = readInt();
        switch (choice) {
            case 1 -> createServiceFlow();
            case 2 -> listServices();
            default -> System.out.println("Invalid option.");
        }
    }

    private static void createServiceFlow() throws SQLException {
        System.out.println("\n-- New Service --");
        String name  = readString("Name: ");
        String desc  = readString("Description: ");
        BigDecimal price = readDecimal("Price: ");
        int hours = readIntPrompt("Duration (hours, 0 if N/A): ");
        Service s = serviceService.createService(name, desc, price, hours);
        System.out.println("Service created. ID=" + s.getId() + ", Name=" + s.getName());
    }

    private static void listServices() throws SQLException {
        List<Service> list = serviceService.getAllServices();
        if (list.isEmpty()) { System.out.println("No services found."); return; }
        System.out.printf("%-5s %-25s %12s %8s%n", "ID", "Name", "Price", "Hours");
        System.out.println("-".repeat(54));
        for (Service s : list) {
            System.out.printf("%-5d %-25s %12.2f %8d%n", s.getId(), s.getName(), s.getPrice(), s.getDurationInHours());
        }
    }

    // ===========================================================
    // INVENTORY MENU
    // ===========================================================
    private static void inventoryMenu() throws SQLException {
        System.out.println("\n--- INVENTORY ---");
        System.out.println("1. View stock levels");
        System.out.println("2. Add stock");
        System.out.print("Choose: ");
        int choice = readInt();
        switch (choice) {
            case 1 -> viewInventory();
            case 2 -> addStockFlow();
            default -> System.out.println("Invalid option.");
        }
    }

    private static void viewInventory() throws SQLException {
        List<Inventory> list = inventoryService.getAllInventories();
        if (list.isEmpty()) { System.out.println("No inventory records."); return; }
        System.out.printf("%-5s %-12s %-25s %10s%n", "ID", "SKU", "Product", "Qty");
        System.out.println("-".repeat(56));
        for (Inventory inv : list) {
            Product p = inv.getProduct();
            System.out.printf("%-5d %-12s %-25s %10d%n",
                inv.getId(), p != null ? p.getSku() : "-",
                p != null ? p.getName() : "-", inv.getQuantity());
        }
    }

    private static void addStockFlow() throws SQLException {
        listProducts();
        int id = readIntPrompt("Product ID: ");
        Product product = productService.getProduct(id);
        if (product == null) { System.out.println("Product not found."); return; }
        int qty = readIntPrompt("Quantity to add: ");
        inventoryService.addStock(product, qty);
        System.out.println("Stock updated. New qty: " + inventoryService.getStock(product));
    }

    // ===========================================================
    // INVOICE MENU
    // ===========================================================
    private static void invoiceMenu() throws SQLException {
        System.out.println("\n--- INVOICES ---");
        System.out.println("1. Create new invoice");
        System.out.println("2. Add item to invoice");
        System.out.println("3. Apply discount");
        System.out.println("4. Finalize invoice");
        System.out.println("5. Cancel invoice");
        System.out.println("6. List invoices");
        System.out.print("Choose: ");
        int choice = readInt();
        switch (choice) {
            case 1 -> createInvoiceFlow();
            case 2 -> addItemFlow();
            case 3 -> applyDiscountFlow();
            case 4 -> finalizeInvoiceFlow();
            case 5 -> cancelInvoiceFlow();
            case 6 -> listInvoices();
            default -> System.out.println("Invalid option.");
        }
    }

    private static void createInvoiceFlow() throws SQLException {
        listCustomers();
        int customerId = readIntPrompt("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) { System.out.println("Customer not found."); return; }
        Invoice invoice = invoiceService.createInvoice(customer);
        System.out.println("Invoice created: " + invoice.getInvoiceNumber() + " (ID=" + invoice.getId() + ")");
    }

    private static void addItemFlow() throws SQLException {
        listInvoices();
        int invoiceId = readIntPrompt("Invoice ID: ");
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        if (invoice == null) { System.out.println("Invoice not found."); return; }

        System.out.println("Item type: 1=Product  2=Service");
        System.out.print("Choose: ");
        int type = readInt();

        if (type == 1) {
            listProducts();
            int productId = readIntPrompt("Product ID: ");
            Product product = productService.getProduct(productId);
            if (product == null) { System.out.println("Product not found."); return; }
            int qty = readIntPrompt("Quantity: ");
            invoiceService.addProductItem(invoice, product, qty);
            System.out.printf("Added %dx %s @ %.2f%n", qty, product.getName(), product.getPrice());
        } else if (type == 2) {
            listServices();
            int serviceId = readIntPrompt("Service ID: ");
            Service service = serviceService.getService(serviceId);
            if (service == null) { System.out.println("Service not found."); return; }
            int qty = readIntPrompt("Quantity: ");
            invoiceService.addServiceItem(invoice, service, qty);
            System.out.printf("Added %dx %s @ %.2f%n", qty, service.getName(), service.getPrice());
        } else {
            System.out.println("Invalid type.");
            return;
        }
        printInvoiceSummary(invoice);
    }

    private static void applyDiscountFlow() throws SQLException {
        listInvoices();
        int invoiceId = readIntPrompt("Invoice ID: ");
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        if (invoice == null) { System.out.println("Invoice not found."); return; }

        System.out.println("Discount type: 1=Flat amount  2=Percentage");
        System.out.print("Choose: ");
        int type = readInt();

        if (type == 1) {
            BigDecimal amount = readDecimal("Flat discount amount: ");
            invoiceService.applyDiscount(invoice, new FlatDiscount(amount, "Flat discount"));
        } else if (type == 2) {
            BigDecimal pct = readDecimal("Percentage (e.g. 10 for 10%): ");
            invoiceService.applyDiscount(invoice, new PercentageDiscount(pct, "Percentage discount"));
        } else {
            System.out.println("Invalid type.");
            return;
        }
        printInvoiceSummary(invoice);
    }

    private static void finalizeInvoiceFlow() throws SQLException {
        listInvoices();
        int invoiceId = readIntPrompt("Invoice ID to finalize: ");
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        if (invoice == null) { System.out.println("Invoice not found."); return; }
        printInvoiceSummary(invoice);
        System.out.print("Confirm finalization? (y/n): ");
        if (!"y".equalsIgnoreCase(scanner.nextLine().trim())) { System.out.println("Cancelled."); return; }
        invoiceService.finalizeInvoice(invoice);
        System.out.println("Invoice " + invoice.getInvoiceNumber() + " finalized. Status: ISSUED");
    }

    private static void cancelInvoiceFlow() throws SQLException {
        listInvoices();
        int invoiceId = readIntPrompt("Invoice ID to cancel: ");
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        if (invoice == null) { System.out.println("Invoice not found."); return; }
        invoiceService.cancelInvoice(invoice);
        System.out.println("Invoice " + invoice.getInvoiceNumber() + " cancelled.");
    }

    private static void listInvoices() throws SQLException {
        List<Invoice> list = invoiceService.getAllInvoices();
        if (list.isEmpty()) { System.out.println("No invoices found."); return; }
        System.out.printf("%-5s %-12s %-22s %12s %-15s %-15s%n",
                "ID", "Number", "Customer", "Total", "Status", "Payment");
        System.out.println("-".repeat(82));
        for (Invoice inv : list) {
            String custName = inv.getCustomer() != null ? inv.getCustomer().getName() : "-";
            System.out.printf("%-5d %-12s %-22s %12.2f %-15s %-15s%n",
                inv.getId(), inv.getInvoiceNumber(), custName,
                inv.getGrandTotal(), inv.getStatus(), inv.getPaymentStatus());
        }
    }

    // ===========================================================
    // PAYMENT FLOW
    // ===========================================================
    private static void makePaymentFlow() throws SQLException {
        listInvoices();
        int invoiceId = readIntPrompt("Invoice ID: ");
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        if (invoice == null) { System.out.println("Invoice not found."); return; }

        BigDecimal total     = invoiceService.calculateTotal(invoice);
        BigDecimal paid      = paymentService.getPaidAmount(invoice);
        BigDecimal remaining = paymentService.getRemainingAmount(invoice);

        System.out.println("\nInvoice: " + invoice.getInvoiceNumber());
        System.out.printf("  Total:     %10.2f%n", total);
        System.out.printf("  Paid:      %10.2f%n", paid);
        System.out.printf("  Remaining: %10.2f%n", remaining);
        System.out.println("  Status: " + invoice.getStatus());

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Invoice is already fully paid.");
            return;
        }

        System.out.println("\nPayment methods:");
        PaymentMethod[] methods = PaymentMethod.values();
        for (int i = 0; i < methods.length; i++) {
            System.out.println((i + 1) + ". " + methods[i]);
        }
        int methodChoice = readIntPrompt("Choose method: ");
        if (methodChoice < 1 || methodChoice > methods.length) { System.out.println("Invalid method."); return; }
        PaymentMethod method = methods[methodChoice - 1];

        BigDecimal amount = readDecimal("Payment amount: ");
        Payment payment = paymentService.makePayment(invoice, amount, method);
        System.out.printf("Payment recorded. ID=%d, Amount=%.2f, Method=%s%n",
            payment.getId(), payment.getAmount(), payment.getMethod());
        System.out.println("Invoice status: " + invoice.getStatus());
    }

    // ===========================================================
    // VIEW INVOICE DETAILS
    // ===========================================================
    private static void viewInvoiceDetails() throws SQLException {
        listInvoices();
        int invoiceId = readIntPrompt("Invoice ID: ");
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        if (invoice == null) { System.out.println("Invoice not found."); return; }

        BigDecimal paid      = paymentService.getPaidAmount(invoice);
        BigDecimal remaining = paymentService.getRemainingAmount(invoice);

        System.out.println("\n" + "-".repeat(60));
        System.out.println("Invoice:  " + invoice.getInvoiceNumber());
        String custName = invoice.getCustomer() != null ? invoice.getCustomer().getName() : "-";
        System.out.println("Customer: " + custName);
        System.out.println("Date:     " + invoice.getInvoiceDate());
        System.out.println("Status:   " + invoice.getStatus() + " / " + invoice.getPaymentStatus());
        System.out.println("-".repeat(60));

        System.out.printf("%-30s %5s %12s %14s%n", "Item", "Qty", "Unit Price", "Subtotal");
        System.out.println("-".repeat(63));
        for (InvoiceItem item : invoice.getItems()) {
            String itemName = item.getProduct() != null ? item.getProduct().getName() : "-";
            System.out.printf("%-30s %5d %12.2f %14.2f%n",
                itemName, item.getQuantity(), item.getUnitPrice(), item.getTotalPrice());
        }
        System.out.println("-".repeat(63));
        System.out.printf("%-49s %14.2f%n", "Subtotal:", invoice.getSubtotal());
        System.out.printf("%-49s %14.2f%n", "Discount:", invoice.getDiscountAmount());
        System.out.printf("%-49s %14.2f%n", "Total:", invoice.getGrandTotal());
        System.out.printf("%-49s %14.2f%n", "Paid:", paid);
        System.out.printf("%-49s %14.2f%n", "Remaining:", remaining);
        System.out.println("-".repeat(60));

        List<Payment> payments = paymentService.getPaymentsForInvoice(invoice);
        if (!payments.isEmpty()) {
            System.out.println("\nPayments:");
            System.out.printf("%-5s %-20s %12s %-15s%n", "ID", "Date", "Amount", "Method");
            System.out.println("-".repeat(54));
            for (Payment p : payments) {
                System.out.printf("%-5d %-20s %12.2f %-15s%n",
                    p.getId(), p.getPaymentDate(), p.getAmount(), p.getMethod());
            }
        }
    }

    // ===========================================================
    // INPUT HELPERS
    // ===========================================================
    private static int readInt() {
        while (true) {
            try {
                if (!scanner.hasNextLine()) return -1;
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Enter a valid number: ");
            }
        }
    }

    private static int readIntPrompt(String prompt) {
        System.out.print(prompt);
        return readInt();
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    private static String readStringOr(String prompt, String defaultValue) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) return defaultValue;
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private static BigDecimal readDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) return BigDecimal.ZERO;
                return new BigDecimal(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid decimal number.");
            }
        }
    }

    private static BigDecimal readDecimalOr(BigDecimal defaultValue) {
        try {
            String line = scanner.nextLine().trim();
            return line.isEmpty() ? defaultValue : new BigDecimal(line);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void printInvoiceSummary(Invoice invoice) {
        System.out.println("\n  " + invoice.getInvoiceNumber() + " summary:");
        System.out.printf("  Subtotal: %.2f  Discount: %.2f  Total: %.2f%n",
            invoice.getSubtotal(), invoice.getDiscountAmount(), invoice.getGrandTotal());
    }

    private static String nvl(String s) {
        return s != null ? s : "";
    }
}
