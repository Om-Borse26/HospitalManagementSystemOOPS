package util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    // Private constructor to prevent instantiation (utility class)
    private ValidationUtil() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    // Regex patterns (compiled once for performance)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern SPECIALIZATION_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");

    // Constants
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 50;
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 120;

    /**
     * Validate username
     * Rules: 3-20 characters, alphanumeric and underscore only
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password
     * Rules: 6-50 characters
     */
    public static boolean isValidPassword(String password) {
        return password != null
                && password.length() >= MIN_PASSWORD_LENGTH
                && password.length() <= MAX_PASSWORD_LENGTH;
    }

    /**
     * Validate name (patient/doctor name)
     * Rules: 2-50 characters, letters and spaces only
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Validate email
     * Rules: Standard email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number
     * Rules: Exactly 10 digits
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate age
     * Rules: Between 1 and 120
     */
    public static boolean isValidAge(int age) {
        return age >= MIN_AGE && age <= MAX_AGE;
    }

    /**
     * Validate gender
     * Rules: Must be "Male", "Female", or "Other"
     */
    public static boolean isValidGender(String gender) {
        return gender != null
                && (gender.equalsIgnoreCase("Male")
                || gender.equalsIgnoreCase("Female")
                || gender.equalsIgnoreCase("Other"));
    }

    /**
     * Validate specialization
     * Rules: 2-50 characters, letters and spaces only
     */
    public static boolean isValidSpecialization(String specialization) {
        return specialization != null && SPECIALIZATION_PATTERN.matcher(specialization).matches();
    }

    /**
     * Validate role
     * Rules: Must be "PATIENT" or "DOCTOR"
     */
    public static boolean isValidRole(String role) {
        return role != null
                && (role.equalsIgnoreCase("PATIENT") || role.equalsIgnoreCase("DOCTOR"));
    }

    /**
     * Check if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validate ID (must be positive)
     */
    public static boolean isValidId(int id) {
        return id > 0;
    }

    /**
     * Sanitize string input (remove extra spaces, trim)
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Check if password is strong
     * Strong password: At least 8 chars, contains uppercase, lowercase, digit, and special char
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * Get password strength message
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return "Too short (minimum " + MIN_PASSWORD_LENGTH + " characters)";
        }

        if (password.length() >= 8 && isStrongPassword(password)) {
            return "Strong";
        } else if (password.length() >= 6) {
            return "Moderate";
        } else {
            return "Weak";
        }
    }

    /**
     * Validate appointment date is not in the past
     */
    public static boolean isValidFutureDate(java.time.LocalDate date) {
        return date != null && !date.isBefore(java.time.LocalDate.now());
    }

    /**
     * Validate appointment date is within reasonable range (e.g., next 6 months)
     */
    public static boolean isValidAppointmentDate(java.time.LocalDate date) {
        if (date == null) return false;

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate maxDate = today.plusMonths(6);

        return !date.isBefore(today) && !date.isAfter(maxDate);
    }
}
