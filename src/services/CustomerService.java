package src.services;

import src.models.Customer;
import src.repositories.CustomerRepository;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {

    private final CustomerRepository customerRepo;

    public CustomerService(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    public Customer createCustomer(String name, String phone, String email, String address) throws SQLException {
        return createCustomer(name, phone, email, address, "");
    }

    public Customer createCustomer(String name, String phone, String email, String address, String gst)
            throws SQLException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Customer name cannot be empty");
        Customer customer = new Customer();
        customer.setName(name.trim());
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setGst(gst != null ? gst : "");
        customer.setStatus("ACTIVE");
        return customerRepo.save(customer);
    }

    public Customer getCustomer(int id) throws SQLException {
        return customerRepo.findById(id);
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerRepo.findAll();
    }

    public boolean updateCustomer(int id, String name, String phone, String email, String address)
            throws SQLException {
        Customer customer = customerRepo.findById(id);
        if (customer == null) return false;
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Customer name cannot be empty");
        customer.setName(name.trim());
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        return customerRepo.update(customer);
    }

    public boolean deleteCustomer(int id) throws SQLException {
        return customerRepo.delete(id);
    }
}
