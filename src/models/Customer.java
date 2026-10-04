package src.models;

import java.sql.Date;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String gst;
    private String status;

    public Customer() {
    }

    // Used when populating the values from Database.
    public Customer(int id, String name, String phone, String email, String address, String gst, Date createdAt, Date updatedAt, String status) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.gst = gst;
        this.status = status;
    }

    // Used when creating a new Customer.
    public Customer(String name, String phone, String email, String address, String gst, Date createdAt, Date updatedAt, String status) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.gst = gst;
        this.status = status;
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getGst() {
        return gst;
    }

    public String getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
