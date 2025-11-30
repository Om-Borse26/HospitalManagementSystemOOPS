# Hospital Management System

A comprehensive healthcare management application built with Java, demonstrating core Object-Oriented Programming principles and JDBC database integration.

## 📋 Project Overview

This Hospital Management System provides a complete solution for managing healthcare operations including patient registration, doctor management, appointment scheduling, and medical records. The system features role-based access control with separate interfaces for patients, doctors, and administrators.

## ✨ Features

- **User Authentication**: Secure login with password masking
- **Role-Based Access Control**: Different interfaces for Patients, Doctors, and Admins
- **Appointment Management**: Schedule, view, and manage appointments
- **Medical Records**: Track patient history and diagnoses
- **Report Generation**: Export appointments to files
- **Password Management**: Change password functionality
- **Graceful Shutdown**: Proper resource cleanup

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- MySQL Server 5.7 or higher
- MySQL JDBC Driver (included in dependencies)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd hospital-management-system
   ```

2. **Set up MySQL Database**
   ```sql
   CREATE DATABASE hospital_management;
   USE hospital_management;
   
   -- Run the SQL schema 
   -- MySQL
   CREATE DATABASE IF NOT EXISTS hospital_management
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_0900_ai_ci;
   
   USE hospital_management;
   
   -- Patients: id, name, age, gender
   CREATE TABLE IF NOT EXISTS patients (
     id INT UNSIGNED NOT NULL AUTO_INCREMENT,
     name VARCHAR(100) NOT NULL,
     age TINYINT UNSIGNED NOT NULL,
     gender ENUM('Male','Female','Other') NOT NULL,
     PRIMARY KEY (id)
   ) ENGINE=InnoDB;
   
   -- Doctors: id, name, specialization
   CREATE TABLE IF NOT EXISTS doctors (
     id INT UNSIGNED NOT NULL AUTO_INCREMENT,
     name VARCHAR(100) NOT NULL,
     specialization VARCHAR(100) NOT NULL,
     PRIMARY KEY (id)
   ) ENGINE=InnoDB;
   
   -- Appointments: id, patient_id, doctor_id, appointment_date
   CREATE TABLE IF NOT EXISTS appointments (
     id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
     patient_id INT UNSIGNED NOT NULL,
     doctor_id INT UNSIGNED NOT NULL,
     appointment_date DATE NOT NULL,
     PRIMARY KEY (id),
     -- optional but useful to prevent double booking per doctor per date
     UNIQUE KEY uq_doctor_date (doctor_id, appointment_date),
     CONSTRAINT fk_appointments_patient
       FOREIGN KEY (patient_id) REFERENCES patients (id)
       ON DELETE CASCADE ON UPDATE CASCADE,
     CONSTRAINT fk_appointments_doctor
       FOREIGN KEY (doctor_id) REFERENCES doctors (id)
       ON DELETE CASCADE ON UPDATE CASCADE
   ) ENGINE=InnoDB;
   
   USE hospital_management;
   
   -- 1. Create users table (new)
   CREATE TABLE IF NOT EXISTS users (
     id INT UNSIGNED NOT NULL AUTO_INCREMENT,
     username VARCHAR(50) NOT NULL UNIQUE,
     password VARCHAR(255) NOT NULL,
     role ENUM('PATIENT', 'DOCTOR') NOT NULL,
     PRIMARY KEY (id),
     INDEX idx_username (username)
   ) ENGINE=InnoDB;
   
   -- 2. Add user_id column to patients (allow NULL initially for existing records)
   ALTER TABLE patients 
   ADD COLUMN user_id INT UNSIGNED NULL AFTER gender;
   
   -- 3. Add user_id column to doctors (allow NULL initially for existing records)
   ALTER TABLE doctors 
   ADD COLUMN user_id INT UNSIGNED NULL AFTER specialization;
   
   -- 4. Add foreign key constraints
   ALTER TABLE patients 
   ADD CONSTRAINT fk_patients_user
     FOREIGN KEY (user_id) REFERENCES users (id)
     ON DELETE CASCADE ON UPDATE CASCADE;
   
   ALTER TABLE doctors 
   ADD CONSTRAINT fk_doctors_user
     FOREIGN KEY (user_id) REFERENCES users (id)
     ON DELETE CASCADE ON UPDATE CASCADE;
   
   -- 5. Optional: Create some test users and link them
   -- Example: Create a test patient user
   INSERT INTO users (username, password, role) 
   VALUES ('patient1', 'password123', 'PATIENT');
   
   -- Get the last inserted user_id and update an existing patient
   -- UPDATE patients SET user_id = LAST_INSERT_ID() WHERE id = 1;
   
   -- Example: Create a test doctor user
   INSERT INTO users (username, password, role) 
   VALUES ('doctor1', 'password123', 'DOCTOR');
   
   -- UPDATE doctors SET user_id = LAST_INSERT_ID() WHERE id = 1;
   
   select * from users;
   ```

3. **Configure Database Connection**
   
   Update credentials in `src/dao/DBConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/hospital_management";
   private static final String USER = "your_username";
   private static final String PASSWORD = "your_password";
   ```

4. **Compile and Run**
   ```bash
   # Using IDE (IntelliJ IDEA)
   - Open project in IntelliJ IDEA
   - Run App.java
   
   # Using command line
   javac -d bin src/**/*.java
   java -cp bin App
   ```

### Debug Mode

Enable debug mode for detailed error traces:
```bash
java -Ddebug=true -cp bin App
```

## 🎯 OOP Concepts Implementation

### 1. **Encapsulation**

**Purpose**: Data hiding and controlled access to class members

**Implementation**:
- All entity classes (`User`, `Patient`, `Doctor`, `Appointment`) use private fields
- Public getter/setter methods provide controlled access
- Database credentials encapsulated in `DBConnection` class

**Example**:
```java
public class User {
    private int id;
    private String username;
    private String password;
    
    // Controlled access via getters/setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
```

### 2. **Inheritance**

**Purpose**: Code reusability and establishing IS-A relationships

**Implementation**:
- `Patient` and `Doctor` classes extend `User` base class
- Shared attributes (id, username, password, role) inherited from `User`
- Specialized attributes added in child classes

**Example**:
```java
public class User {
    protected int id;
    protected String username;
    protected String role;
}

public class Patient extends User {
    private String medicalHistory;  // Patient-specific field
}

public class Doctor extends User {
    private String specialization;  // Doctor-specific field
}
```

### 3. **Polymorphism**

**Purpose**: Multiple forms of the same entity

**Implementation**:
- **Method Overriding**: Child classes override `toString()` and other methods
- **Runtime Polymorphism**: `User` reference can hold `Patient` or `Doctor` objects
- **Method Overloading**: Multiple constructors with different parameters

**Example**:
```java
User user = loginService.authenticate(username, password);
// user can be Patient or Doctor at runtime

if (user instanceof Patient) {
    Patient patient = (Patient) user;
    // Patient-specific operations
} else if (user instanceof Doctor) {
    Doctor doctor = (Doctor) user;
    // Doctor-specific operations
}
```

### 4. **Abstraction**

**Purpose**: Hiding complex implementation details

**Implementation**:
- Service layer (`UserService`, `AppointmentService`) abstracts business logic
- DAO layer (`UserDAO`, `AppointmentDAO`) abstracts database operations
- UI layer shows only relevant options based on user role

**Example**:
```java
// High-level abstraction - user doesn't see SQL queries
UserService userService = new UserService();
Patient patient = userService.getPatientById(id);

// Behind the scenes, complex DAO operations are hidden
```

### 5. **Association**

**Purpose**: Relationships between objects

**Implementation**:
- **One-to-Many**: One Patient has many Appointments
- **One-to-Many**: One Doctor has many Appointments
- **Composition**: `MainMenu` contains `UserService` and `AppointmentService`

**Example**:
```java
public class Appointment {
    private int patientId;     // Association with Patient
    private String doctorName; // Association with Doctor
}
```

### 6. **Dependency Injection**

**Purpose**: Loose coupling between classes

**Implementation**:
- Services injected into menu classes
- User object passed to `MainMenu` constructor

**Example**:
```java
public class MainMenu {
    private User user;
    private UserService userService;
    
    public MainMenu(User user) {
        this.user = user;  // Dependency injection
        this.userService = new UserService();
    }
}
```

### 7. **Singleton Pattern** (Design Pattern)

**Purpose**: Single database connection instance

**Implementation**:
- `DBConnection` ensures only one connection exists
- Prevents multiple database connections

**Example**:
```java
public class DBConnection {
    private static Connection connection = null;
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
```

## 📁 Project Structure

```
hospital-management-system/
├── src/
│   ├── App.java                    # Main entry point
│   ├── model/                      # Entity classes (Encapsulation)
│   │   ├── User.java              # Base class (Inheritance)
│   │   ├── Patient.java           # Inherits User
│   │   ├── Doctor.java            # Inherits User
│   │   └── Appointment.java
│   ├── dao/                        # Data Access Layer (Abstraction)
│   │   ├── DBConnection.java     # Singleton pattern
│   │   ├── UserDAO.java
│   │   └── AppointmentDAO.java
│   ├── service/                    # Business Logic Layer (Abstraction)
│   │   ├── UserService.java
│   │   └── AppointmentService.java
│   └── ui/                         # User Interface Layer
│       ├── LoginMenu.java
│       ├── MainMenu.java          # Role-based polymorphism
│       ├── PatientMenu.java
│       ├── DoctorMenu.java
│       └── AdminMenu.java
└── README.md
```

## 🔐 User Roles

### Patient
- View profile
- Book appointments
- View appointment history
- View medical records
- Change password

### Doctor
- View profile
- View assigned appointments
- Update patient records
- Change password

### Admin
- Manage users (create, update, delete)
- View all appointments
- Generate reports
- System management

## 🛠️ Technical Features

- **Thread Safety**: Volatile flag for graceful shutdown
- **Resource Management**: Automatic cleanup via shutdown hooks
- **Exception Handling**: Comprehensive error handling throughout
- **Password Security**: Masked password input (using `Console` API)
- **Connection Pooling**: Efficient database connection management
- **Report Generation**: Export data to text files

## 📊 Database Schema

Key tables:
- `users`: User authentication and roles
- `patients`: Patient-specific information
- `doctors`: Doctor specializations
- `appointments`: Appointment scheduling
- `medical_records`: Patient history

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is created for educational purposes to demonstrate OOP concepts in Java.

## 👨‍💻 Author

Om Borse (@Om-Borse26)

## 🙏 Acknowledgments

- Built with Java OOP principles
- MySQL for database management
- IntelliJ IDEA for development

---

**Version**: 1.0.0  
**Last Updated**: 2025
