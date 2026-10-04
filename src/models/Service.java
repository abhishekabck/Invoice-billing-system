package src.models;

import src.interfaces.Billable;
import java.math.BigDecimal;

public class Service implements Billable {
    
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    // private BigDecimal taxRate; // Tax rate as a percentage (e.g., 10 for 10%)
    private boolean isActive;
    private int durationInHours; // Duration of the service in hours

    public Service() {
    }

    // Used when creating a new Service.
    public Service(String name, String description, BigDecimal price, boolean isActive, int durationInHours) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
        this.durationInHours = durationInHours;
    }

    // Used when populating the values from Database.
    public Service(int id, String name, String description, BigDecimal price, boolean isActive, int durationInHours) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
        this.durationInHours = durationInHours;
    }

    // getters and setters
    public int getId() {
        return id;
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

    public boolean getIsActive() {
        return isActive;
    }

    public int getDurationInHours() {
        return durationInHours;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setDurationInHours(int durationInHours) {
        this.durationInHours = durationInHours;
    }
}
