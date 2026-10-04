package src.services;

import src.models.Service;
import src.repositories.ServiceRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ServiceService {

    private final ServiceRepository serviceRepo;

    public ServiceService(ServiceRepository serviceRepo) {
        this.serviceRepo = serviceRepo;
    }

    public Service createService(String name, String description, BigDecimal price, int durationHours)
            throws SQLException {
        validateServiceData(name, price);
        Service service = new Service(name, description, price, true, durationHours);
        return serviceRepo.save(service);
    }

    public Service getService(int id) throws SQLException {
        return serviceRepo.findById(id);
    }

    public List<Service> getAllServices() throws SQLException {
        return serviceRepo.findAll();
    }

    public boolean updateService(int id, String name, String description, BigDecimal price, int durationHours)
            throws SQLException {
        Service service = serviceRepo.findById(id);
        if (service == null) return false;
        validateServiceData(name, price);
        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        service.setDurationInHours(durationHours);
        return serviceRepo.update(service);
    }

    public boolean deleteService(int id) throws SQLException {
        return serviceRepo.delete(id);
    }

    private void validateServiceData(String name, BigDecimal price) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Service name cannot be empty");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price cannot be negative");
    }
}
