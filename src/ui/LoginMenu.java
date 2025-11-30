package ui;

import model.User;
import service.AuthService;
import service.HospitalException;
import util.InputUtil;
import util.ValidationUtil;

public class LoginMenu {
    private final AuthService authService;
    private final InputUtil inputUtil;

    public LoginMenu() {
        this.authService = new AuthService();
        this.inputUtil = new InputUtil();
    }

    // Main login/registration menu
    public User showLoginMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   HOSPITAL MANAGEMENT SYSTEM - LOGIN   ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Login");
            System.out.println("2. Register as Patient");
            System.out.println("3. Register as Doctor");
            System.out.println("4. Exit");
            System.out.println("─────────────────────────────────────────");

            int choice = inputUtil.readInt("Enter your choice: ", 1, 4);

            try {
                switch (choice) {
                    case 1:
                        return handleLogin();
                    case 2:
                        return handlePatientRegistration();
                    case 3:
                        return handleDoctorRegistration();
                    case 4:
                        System.out.println("\nThank you for using Hospital Management System!");
                        System.exit(0);
                }
            } catch (HospitalException e) {
                System.out.println("\n❌ Error: " + e.getMessage());
                System.out.println("Please try again.\n");
            }
        }
    }

    // Handle user login
    private User handleLogin() throws HospitalException {
        System.out.println("\n═══════════════ LOGIN ═══════════════");

        String username = inputUtil.readNonEmpty("Username: ");
        String password = inputUtil.readNonEmpty("Password: ");

        User user = authService.login(username, password);

        System.out.println("\n✓ Login successful!");
        System.out.println("Welcome, " + username + " (" + user.getRole() + ")");

        return user;
    }

    // Handle patient registration
    private User handlePatientRegistration() throws HospitalException {
        System.out.println("\n═══════════ PATIENT REGISTRATION ═══════════");

        // Username validation
        String username = inputUtil.readNonEmpty("Username: ");
        if (!ValidationUtil.isValidUsername(username)) {
            throw new HospitalException("Username must be 3-20 characters (letters, numbers, underscore only)");
        }

        // Password validation
        String password = inputUtil.readNonEmpty("Password: ");
        if (!ValidationUtil.isValidPassword(password)) {
            throw new HospitalException("Password must be at least 6 characters");
        }

        System.out.println("\n--- Patient Information ---");

        // Name validation
        String name = inputUtil.readNonEmpty("Full Name: ");
        if (!ValidationUtil.isValidName(name)) {
            throw new HospitalException("Name must be 2-50 characters (letters and spaces only)");
        }

        // Age validation
        int age = inputUtil.readInt("Age: ", 1, 120);

        // Gender selection
        System.out.println("\nSelect Gender:");
        System.out.println("1. Male");
        System.out.println("2. Female");
        System.out.println("3. Other");
        int genderChoice = inputUtil.readInt("Choice: ", 1, 3);

        String gender;
        switch (genderChoice) {
            case 1:
                gender = "Male";
                break;
            case 2:
                gender = "Female";
                break;
            case 3:
                gender = "Other";
                break;
            default:
                gender = "Other";
        }

        User user = authService.registerPatient(username, password, name, age, gender);

        System.out.println("\n✓ Patient registration successful!");
        System.out.println("You can now login with your credentials.");

        return user;
    }

    // Handle doctor registration
    private User handleDoctorRegistration() throws HospitalException {
        System.out.println("\n═══════════ DOCTOR REGISTRATION ═══════════");

        // Username validation
        String username = inputUtil.readNonEmpty("Username: ");
        if (!ValidationUtil.isValidUsername(username)) {
            throw new HospitalException("Username must be 3-20 characters (letters, numbers, underscore only)");
        }

        // Password validation
        String password = inputUtil.readNonEmpty("Password: ");
        if (!ValidationUtil.isValidPassword(password)) {
            throw new HospitalException("Password must be at least 6 characters");
        }

        System.out.println("\n--- Doctor Information ---");

        // Name validation
        String name = inputUtil.readNonEmpty("Full Name: ");
        if (!ValidationUtil.isValidName(name)) {
            throw new HospitalException("Name must be 2-50 characters (letters and spaces only)");
        }

        // Specialization validation
        String specialization = inputUtil.readNonEmpty("Specialization: ");
        if (!ValidationUtil.isValidSpecialization(specialization)) {
            throw new HospitalException("Specialization must be 2-50 characters");
        }

        User user = authService.registerDoctor(username, password, name, specialization);

        System.out.println("\n✓ Doctor registration successful!");
        System.out.println("You can now login with your credentials.");

        return user;
    }
}
