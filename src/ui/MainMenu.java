package ui;

import model.Appointment;
import model.Doctor;
import model.Patient;
import model.User;
import service.AppointmentService;
import service.AuthService;
import service.HospitalException;
import util.InputUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainMenu {
    private final User currentUser;
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final InputUtil inputUtil;

    public MainMenu(User user) {
        this.currentUser = user;
        this.authService = new AuthService();
        this.appointmentService = new AppointmentService();
        this.inputUtil = new InputUtil();
    }

    // Show appropriate menu based on user role
    public void show() {
        if ("PATIENT".equals(currentUser.getRole())) {
            showPatientMenu();
        } else if ("DOCTOR".equals(currentUser.getRole())) {
            showDoctorMenu();
        }
    }

    // Patient menu
    private void showPatientMenu() {
        try {
            Patient patient = authService.getPatientByUserId(currentUser.getId());

            while (true) {
                System.out.println("\n╔════════════════════════════════════════╗");
                System.out.println("║         PATIENT DASHBOARD              ║");
                System.out.println("╚════════════════════════════════════════╝");
                System.out.println("Welcome, " + patient.getName());
                System.out.println("─────────────────────────────────────────");
                System.out.println("1. View Available Doctors");
                System.out.println("2. Book Appointment");
                System.out.println("3. View My Appointments");
                System.out.println("4. Cancel Appointments");
                System.out.println("5. Search Doctors by Specialization");
                System.out.println("6. Change Password");
                System.out.println("7. Logout");
                System.out.println("─────────────────────────────────────────");

                int choice = inputUtil.readInt("Enter your choice: ", 1, 7);

                try {
                    switch (choice) {
                        case 1:
                            viewAllDoctors();
                            break;
                        case 2:
                            bookAppointment(patient.getId());
                            break;
                        case 3:
                            viewMyAppointments(patient.getId());
                            break;
                        case 4:
                            cancelAppointment(patient.getId());
                            break;
                        case 5:
                            searchDoctorsBySpecialization();
                            break;
                        case 6:                                    // ← ADD THIS CASE
                            changePassword();
                            break;
                        case 7:
                            authService.logout(currentUser.getUsername());
                            System.out.println("\n✓ Logged out successfully!");
                            return;
                    }
                } catch (HospitalException e) {
                    System.out.println("\n❌ Error: " + e.getMessage());
                }

                inputUtil.pressEnterToContinue();
            }
        } catch (HospitalException e) {
            System.out.println("❌ Error loading patient profile: " + e.getMessage());
        }
    }

    // Doctor menu
    private void showDoctorMenu() {
        try {
            Doctor doctor = authService.getDoctorByUserId(currentUser.getId());

            while (true) {
                System.out.println("\n╔════════════════════════════════════════╗");
                System.out.println("║          DOCTOR DASHBOARD              ║");
                System.out.println("╚════════════════════════════════════════╝");
                System.out.println("Dr. " + doctor.getName() + " - " + doctor.getSpecialization());
                System.out.println("─────────────────────────────────────────");
                System.out.println("1. View My Appointments");
                System.out.println("2. View All Doctors");
                System.out.println("3. Check My Availability");
                System.out.println("4. Change Password");
                System.out.println("5. Logout");
                System.out.println("─────────────────────────────────────────");

                int choice = inputUtil.readInt("Enter your choice: ", 1, 4);

                try {
                    switch (choice) {
                        case 1:
                            viewDoctorAppointments(doctor.getId());
                            break;
                        case 2:
                            viewAllDoctors();
                            break;
                        case 3:
                            checkDoctorAvailability(doctor.getId());
                            break;
                        case 4:                                    // ← ADD THIS CASE
                            changePassword();
                            break;
                        case 5:
                            authService.logout(currentUser.getUsername());
                            System.out.println("\n✓ Logged out successfully!");
                            return;
                    }
                } catch (HospitalException e) {
                    System.out.println("\n❌ Error: " + e.getMessage());
                }

                inputUtil.pressEnterToContinue();
            }
        } catch (HospitalException e) {
            System.out.println("❌ Error loading doctor profile: " + e.getMessage());
        }
    }

    private void cancelAppointment(int patientId) throws HospitalException {
        System.out.println("\n═══════════ CANCEL APPOINTMENT ═══════════");

        List<Appointment> appointments = appointmentService.getPatientAppointments(patientId);

        if (appointments.isEmpty()) {
            System.out.println("You have no appointments to cancel.");
            return;
        }

        // Show appointments
        System.out.println("\nYour upcoming appointments:");
        for (Appointment apt : appointments) {
            if (!apt.getAppointmentDate().isBefore(LocalDate.now())) {
                System.out.printf("ID: %d | Doctor: %s | Date: %s%n",
                        apt.getId(),
                        apt.getDoctorName(),
                        apt.getAppointmentDate());
            }
        }

        int appointmentId = inputUtil.readInt("\nEnter Appointment ID to cancel (0 to go back): ", 0, Integer.MAX_VALUE);

        if (appointmentId == 0) return;

        if (inputUtil.readConfirmation("\nAre you sure you want to cancel this appointment?")) {
            appointmentService.cancelAppointment(appointmentId, patientId);
            System.out.println("\n✓ Appointment cancelled successfully!");
        } else {
            System.out.println("\nCancellation aborted.");
        }
    }

    private void changePassword() throws HospitalException {
        System.out.println("\n═══════════ CHANGE PASSWORD ═══════════");

        String oldPassword = inputUtil.readNonEmpty("Current Password: ");
        String newPassword = inputUtil.readNonEmpty("New Password: ");
        String confirmPassword = inputUtil.readNonEmpty("Confirm New Password: ");

        if (!newPassword.equals(confirmPassword)) {
            throw new HospitalException("Passwords do not match!");
        }

        authService.changePassword(currentUser.getId(), oldPassword, newPassword);

        System.out.println("\n✓ Password changed successfully!");
    }

    // View all doctors
    private void viewAllDoctors() throws HospitalException {
        System.out.println("\n════════════ AVAILABLE DOCTORS ════════════");

        List<Doctor> doctors = appointmentService.getAllDoctors();

        if (doctors.isEmpty()) {
            System.out.println("No doctors available.");
            return;
        }

        System.out.printf("%-5s %-25s %-30s%n", "ID", "Name", "Specialization");
        System.out.println("─".repeat(60));

        for (Doctor doctor : doctors) {
            System.out.printf("%-5d %-25s %-30s%n",
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getSpecialization());
        }
    }

    // Book appointment
    private void bookAppointment(int patientId) throws HospitalException {
        System.out.println("\n═══════════ BOOK APPOINTMENT ═══════════");

        viewAllDoctors();

        int doctorId = inputUtil.readInt("\nEnter Doctor ID: ", 1, Integer.MAX_VALUE);
        LocalDate appointmentDate = inputUtil.readDate("Enter Appointment Date (YYYY-MM-DD): ");

        Appointment appointment = appointmentService.bookAppointment(patientId, doctorId, appointmentDate);

        System.out.println("\n✓ Appointment booked successfully!");
        System.out.println("Appointment ID: " + appointment.getId());
        System.out.println("Date: " + appointment.getAppointmentDate());
    }

    // View patient's appointments
    private void viewMyAppointments(int patientId) throws HospitalException {
        System.out.println("\n════════════ MY APPOINTMENTS ════════════");

        List<Appointment> appointments = appointmentService.getPatientAppointments(patientId);

        if (appointments.isEmpty()) {
            System.out.println("You have no appointments.");
            return;
        }

        for (Appointment apt : appointments) {
            System.out.println("\n┌─────────────────────────────────────");
            System.out.println("│ Appointment ID: " + apt.getId());
            System.out.println("│ Doctor: " + apt.getDoctorName());
            System.out.println("│ Specialization: " + apt.getDoctorSpecialization());
            System.out.println("│ Date: " + apt.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            System.out.println("└─────────────────────────────────────");
        }
    }

    // View doctor's appointments
    private void viewDoctorAppointments(int doctorId) throws HospitalException {
        System.out.println("\n════════════ MY APPOINTMENTS ════════════");

        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId);

        if (appointments.isEmpty()) {
            System.out.println("You have no appointments scheduled.");
            return;
        }

        for (Appointment apt : appointments) {
            System.out.println("\n┌─────────────────────────────────────");
            System.out.println("│ Appointment ID: " + apt.getId());
            System.out.println("│ Patient: " + apt.getPatientName());
            System.out.println("│ Date: " + apt.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            System.out.println("└─────────────────────────────────────");
        }
    }

    // Search doctors by specialization
    private void searchDoctorsBySpecialization() throws HospitalException {
        System.out.println("\n═══════ SEARCH DOCTORS BY SPECIALIZATION ═══════");

        String specialization = inputUtil.readNonEmpty("Enter Specialization: ");

        List<Doctor> doctors = appointmentService.getAllDoctors(specialization);

        if (doctors.isEmpty()) {
            System.out.println("No doctors found with specialization: " + specialization);
            return;
        }

        System.out.println("\nFound " + doctors.size() + " doctor(s):");
        System.out.printf("%-5s %-25s %-30s%n", "ID", "Name", "Specialization");
        System.out.println("─".repeat(60));

        for (Doctor doctor : doctors) {
            System.out.printf("%-5d %-25s %-30s%n",
                    doctor.getId(),
                    doctor.getName(),
                    doctor.getSpecialization());
        }
    }

    // Check doctor availability
    private void checkDoctorAvailability(int doctorId) throws HospitalException {
        System.out.println("\n═══════ CHECK AVAILABILITY ═══════");

        LocalDate date = inputUtil.readDate("Enter Date (YYYY-MM-DD): ");

        boolean available = appointmentService.isDoctorAvailable(doctorId, date);

        if (available) {
            System.out.println("\n✓ You are AVAILABLE on " + date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        } else {
            System.out.println("\n✗ You are NOT AVAILABLE on " + date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
            System.out.println("  (Appointment already booked for this date)");
        }
    }
}
