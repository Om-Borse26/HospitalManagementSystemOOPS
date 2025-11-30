package service;

import dao.UserDAO;
import dao.PatientDAO;
import dao.DoctorDAO;
import model.User;
import model.Patient;
import model.Doctor;
import util.ValidationUtil; // Added import for ValidationUtil

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final UserDAO userDAO;
    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;

    // Collection Framework: ConcurrentHashMap for thread-safe session management
    private static final ConcurrentHashMap<String, User> activeUsers = new ConcurrentHashMap<>();

    public AuthService() {
        this.userDAO = new UserDAO();
        this.patientDAO = new PatientDAO();
        this.doctorDAO = new DoctorDAO();
    }

    // Change password
    public synchronized boolean changePassword(int userId, String oldPassword, String newPassword)
            throws HospitalException {
        try {
            User user = userDAO.findUserById(userId);

            if (user == null) {
                throw new HospitalException("User not found!");
            }

            if (!user.getPassword().equals(oldPassword)) {
                throw new HospitalException("Current password is incorrect!");
            }

            if (!ValidationUtil.isValidPassword(newPassword)) {
                throw new HospitalException("New password must be at least 6 characters!");
            }

            boolean changed = userDAO.updatePassword(userId, newPassword);

            if (changed) {
                // Update cached user
                user.setPassword(newPassword);
                activeUsers.put(user.getUsername(), user);
            }

            return changed;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Register new patient
    public synchronized User registerPatient(String username, String password, String name, int age, String gender)
            throws HospitalException {
        try {
            // Validate username doesn't exist
            if (userDAO.usernameExists(username)) {
                throw new HospitalException("Username already exists!");
            }

            // Create user account
            User user = new User(username, password, "PATIENT");
            if (!userDAO.registerUser(user)) {
                throw new HospitalException("Failed to create user account");
            }

            // Create patient profile
            Patient patient = new Patient(name, age, gender, user.getId());
            if (!patientDAO.addPatient(patient)) {
                throw new HospitalException("Failed to create patient profile");
            }

            // Add to active users cache
            activeUsers.put(username, user);

            return user;
        } catch (SQLException e) {
            throw new HospitalException("Database error during registration: " + e.getMessage(), e);
        }
    }

    // Register new doctor
    public synchronized User registerDoctor(String username, String password, String name, String specialization)
            throws HospitalException {
        try {
            if (userDAO.usernameExists(username)) {
                throw new HospitalException("Username already exists!");
            }

            User user = new User(username, password, "DOCTOR");
            if (!userDAO.registerUser(user)) {
                throw new HospitalException("Failed to create user account");
            }

            Doctor doctor = new Doctor(name, specialization, user.getId());
            if (!doctorDAO.addDoctor(doctor)) {
                throw new HospitalException("Failed to create doctor profile");
            }

            activeUsers.put(username, user);

            return user;
        } catch (SQLException e) {
            throw new HospitalException("Database error during registration: " + e.getMessage(), e);
        }
    }

    // Login user
    public User login(String username, String password) throws HospitalException {
        try {
            // Check cache first
            User cachedUser = activeUsers.get(username);
            if (cachedUser != null && cachedUser.getPassword().equals(password)) {
                return cachedUser;
            }

            // Query database
            User user = userDAO.findUserByCredentials(username, password);
            if (user == null) {
                throw new HospitalException("Invalid username or password!");
            }

            // Add to cache
            activeUsers.put(username, user);

            return user;
        } catch (SQLException e) {
            throw new HospitalException("Database error during login: " + e.getMessage(), e);
        }
    }

    // Logout user
    public void logout(String username) {
        activeUsers.remove(username);
    }

    // Get patient by user ID
    public Patient getPatientByUserId(int userId) throws HospitalException {
        try {
            Patient patient = patientDAO.findPatientByUserId(userId);
            if (patient == null) {
                throw new HospitalException("Patient profile not found");
            }
            return patient;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Get doctor by user ID
    public Doctor getDoctorByUserId(int userId) throws HospitalException {
        try {
            Doctor doctor = doctorDAO.findDoctorByUserId(userId);
            if (doctor == null) {
                throw new HospitalException("Doctor profile not found");
            }
            return doctor;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Get active users count (for monitoring)
    public static int getActiveUsersCount() {
        return activeUsers.size();
    }

    // Clear all cached users (for maintenance)
    public static void clearCache() {
        activeUsers.clear();
    }
}
