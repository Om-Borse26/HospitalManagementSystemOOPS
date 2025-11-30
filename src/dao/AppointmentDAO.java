package dao;

import model.Appointment;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    // Book appointment with prepared statement
    public synchronized boolean bookAppointment(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, appointment.getPatientId());
            ps.setInt(2, appointment.getDoctorId());
            ps.setDate(3, Date.valueOf(appointment.getAppointmentDate()));

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        appointment.setId(rs.getLong(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Check doctor availability (synchronized for thread safety)
    public synchronized boolean isDoctorAvailable(int doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return false;
    }

    // Get appointments for a specific patient
    public List<Appointment> getAppointmentsByPatientId(int patientId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.id, a.patient_id, a.doctor_id, a.appointment_date, " +
                "p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE a.patient_id = ? " +
                "ORDER BY a.appointment_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                            rs.getLong("id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getString("patient_name"),
                            rs.getString("doctor_name"),
                            rs.getString("specialization")
                    ));
                }
            }
        }
        return appointments;
    }

    // Get appointments for a specific doctor
    public List<Appointment> getAppointmentsByDoctorId(int doctorId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.id, a.patient_id, a.doctor_id, a.appointment_date, " +
                "p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE a.doctor_id = ? " +
                "ORDER BY a.appointment_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                            rs.getLong("id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getString("patient_name"),
                            rs.getString("doctor_name"),
                            rs.getString("specialization")
                    ));
                }
            }
        }
        return appointments;
    }

    // Get appointment by ID
    public Appointment getAppointmentById(int appointmentId) throws SQLException {
        String sql = "SELECT a.*, p.name AS patient_name, d.name AS doctor_name, " +
                "d.specialization AS doctor_specialization " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE a.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("appointment_date").toLocalDate()
                );
                appointment.setPatientName(rs.getString("patient_name"));
                appointment.setDoctorName(rs.getString("doctor_name"));
                appointment.setDoctorSpecialization(rs.getString("doctor_specialization"));
                return appointment;
            }
        }

        return null;
    }

    // Cancel appointment
    public boolean cancelAppointment(int appointmentId) throws SQLException {
        String sql = "DELETE FROM appointments WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Get past appointments for patient
    public List<Appointment> getPastAppointmentsByPatientId(int patientId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.id, a.patient_id, a.doctor_id, a.appointment_date, " +
                "p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "WHERE a.patient_id = ? AND a.appointment_date < CURDATE() " +
                "ORDER BY a.appointment_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                            rs.getLong("id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getString("patient_name"),
                            rs.getString("doctor_name"),
                            rs.getString("specialization")
                    ));
                }
            }
        }

        return appointments;
    }

    // Get all appointments
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.id, a.patient_id, a.doctor_id, a.appointment_date, " +
                "p.name AS patient_name, d.name AS doctor_name, d.specialization " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN doctors d ON a.doctor_id = d.id " +
                "ORDER BY a.appointment_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                appointments.add(new Appointment(
                        rs.getLong("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("appointment_date").toLocalDate(),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("specialization")
                ));
            }
        }
        return appointments;
    }
}
