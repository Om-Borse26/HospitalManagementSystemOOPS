package model;

import java.io.Serializable;

public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String specialization;
    private int userId; // Foreign key to users table

    // Constructor for new doctor (without id)
    public Doctor(String name, String specialization, int userId) {
        this.name = name;
        this.specialization = specialization;
        this.userId = userId;
    }

    // Constructor for existing doctor (with id)
    public Doctor(int id, String name, String specialization, int userId) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", specialization='" + specialization + '\'' +
                ", userId=" + userId +
                '}';
    }
}
