package model;

import java.io.Serializable;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private int age;
    private String gender;
    private int userId; // Foreign key to users table

    // Constructor for new patient (without id)
    public Patient(String name, int age, String gender, int userId) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.userId = userId;
    }

    // Constructor for existing patient (with id)
    public Patient(int id, String name, int age, String gender, int userId) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", userId=" + userId +
                '}';
    }
}
