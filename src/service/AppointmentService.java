package service;

import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import model.Appointment;
import model.Doctor;
import model.Patient;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.*;
import java.util.concurrent.*;

public class AppointmentService {
    private final AppointmentDAO appointmentDAO;
    private final DoctorDAO doctorDAO;
    private final PatientDAO patientDAO;

    // Collection Framework: ConcurrentHashMap for thread-safe caching
    private static final ConcurrentHashMap<Integer, List<Appointment>> doctorAppointmentsCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, List<Appointment>> patientAppointmentsCache = new ConcurrentHashMap<>();

    // volatile for thread visibility
    private static volatile long cacheTimestamp = System.currentTimeMillis();
    private static final long CACHE_EXPIRY_MS = 60000; // 1 minute cache

    // ExecutorService for multithreading demo
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.doctorDAO = new DoctorDAO();
        this.patientDAO = new PatientDAO();
    }

    // Book appointment with validation
    public synchronized Appointment bookAppointment(int patientId, int doctorId, LocalDate appointmentDate)
            throws HospitalException {
        try {
            // Validate patient exists
            Patient patient = patientDAO.findPatientById(patientId);
            if (patient == null) {
                throw new HospitalException("Patient not found!");
            }

            // Validate doctor exists
            Doctor doctor = doctorDAO.findDoctorById(doctorId);
            if (doctor == null) {
                throw new HospitalException("Doctor not found!");
            }

            // Check date is not in the past
            if (appointmentDate.isBefore(LocalDate.now())) {
                throw new HospitalException("Cannot book appointment in the past!");
            }

            // Check doctor availability
            if (!appointmentDAO.isDoctorAvailable(doctorId, appointmentDate)) {
                throw new HospitalException("Doctor is not available on this date!");
            }

            // Create appointment
            Appointment appointment = new Appointment(patientId, doctorId, appointmentDate);
            if (!appointmentDAO.bookAppointment(appointment)) {
                throw new HospitalException("Failed to book appointment!");
            }

            // Clear cache after booking
            clearCache();

            return appointment;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Get appointments for patient with caching
    public List<Appointment> getPatientAppointments(int patientId) throws HospitalException {
        try {
            // Check cache first
            if (isCacheValid()) {
                List<Appointment> cached = patientAppointmentsCache.get(patientId);
                if (cached != null) {
                    return new ArrayList<>(cached); // Return copy
                }
            }

            // Fetch from database
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);

            // Update cache
            patientAppointmentsCache.put(patientId, appointments);

            return appointments;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Get appointments for doctor with caching
    public List<Appointment> getDoctorAppointments(int doctorId) throws HospitalException {
        try {
            if (isCacheValid()) {
                List<Appointment> cached = doctorAppointmentsCache.get(doctorId);
                if (cached != null) {
                    return new ArrayList<>(cached);
                }
            }

            List<Appointment> appointments = appointmentDAO.getAppointmentsByDoctorId(doctorId);
            doctorAppointmentsCache.put(doctorId, appointments);

            return appointments;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Cancel appointment
    public synchronized boolean cancelAppointment(int appointmentId, int patientId) throws HospitalException {
        try {
            Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);

            if (appointment == null) {
                throw new HospitalException("Appointment not found!");
            }

            // Verify the appointment belongs to this patient
            if (appointment.getPatientId() != patientId) {
                throw new HospitalException("You can only cancel your own appointments!");
            }

            // Check if appointment is in the future
            if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
                throw new HospitalException("Cannot cancel past appointments!");
            }

            boolean cancelled = appointmentDAO.cancelAppointment(appointmentId);

            if (cancelled) {
                clearCache();
            }

            return cancelled;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Get all doctors (with filter by specialization)
    public List<Doctor> getAllDoctors(String specializationFilter) throws HospitalException {
        try {
            List<Doctor> doctors = doctorDAO.getAllDoctors();

            if (specializationFilter != null && !specializationFilter.isEmpty()) {
                // Collection Framework: Stream API for filtering
                return doctors.stream()
                        .filter(d -> d.getSpecialization().toLowerCase()
                                .contains(specializationFilter.toLowerCase()))
                        .collect(Collectors.toList());
            }

            return doctors;
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Get all doctors (no filter)
    public List<Doctor> getAllDoctors() throws HospitalException {
        return getAllDoctors(null);
    }

    public void exportAppointmentsToFile(List<Appointment> appointments, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Appointment Report - Generated on " + LocalDate.now());
            writer.println("=".repeat(60));

            for (Appointment apt : appointments) {
                writer.printf("ID: %d | Doctor: %s | Date: %s%n",
                        apt.getId(), apt.getDoctorName(), apt.getAppointmentDate());
            }

            System.out.println("✓ Appointments exported to " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting file: " + e.getMessage());
        }
    }


    // Check doctor availability
    public boolean isDoctorAvailable(int doctorId, LocalDate date) throws HospitalException {
        try {
            return appointmentDAO.isDoctorAvailable(doctorId, date);
        } catch (SQLException e) {
            throw new HospitalException("Database error: " + e.getMessage(), e);
        }
    }

    // Multithreading: Async method to prefetch appointments for multiple patients
    public Future<Map<Integer, List<Appointment>>> prefetchPatientAppointments(List<Integer> patientIds) {
        return executorService.submit(() -> {
            Map<Integer, List<Appointment>> results = new ConcurrentHashMap<>();

            for (Integer patientId : patientIds) {
                try {
                    List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);
                    results.put(patientId, appointments);
                } catch (SQLException e) {
                    System.err.println("Error fetching appointments for patient " + patientId + ": " + e.getMessage());
                }
            }

            return results;
        });
    }

    // Multithreading: Async method to check multiple doctors' availability
    public Future<Map<Integer, Boolean>> checkMultipleDoctorsAvailability(List<Integer> doctorIds, LocalDate date) {
        return executorService.submit(() -> {
            Map<Integer, Boolean> results = new ConcurrentHashMap<>();

            for (Integer doctorId : doctorIds) {
                try {
                    boolean available = appointmentDAO.isDoctorAvailable(doctorId, date);
                    results.put(doctorId, available);
                } catch (SQLException e) {
                    System.err.println("Error checking availability for doctor " + doctorId + ": " + e.getMessage());
                    results.put(doctorId, false);
                }
            }

            return results;
        });
    }

    // Cache management
    private boolean isCacheValid() {
        return (System.currentTimeMillis() - cacheTimestamp) < CACHE_EXPIRY_MS;
    }

    private static synchronized void clearCache() {
        doctorAppointmentsCache.clear();
        patientAppointmentsCache.clear();
        cacheTimestamp = System.currentTimeMillis();
    }

    // Shutdown executor service
    public static void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

    }
}
