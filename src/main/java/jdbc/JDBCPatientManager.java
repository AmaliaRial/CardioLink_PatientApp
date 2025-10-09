package jdbc;

import jdbcInterfaces.PatientManager;
import pojos.Patient;
import common.enums.Sex;

import java.sql.*;
import java.text.SimpleDateFormat;

public class JDBCPatientManager implements PatientManager {

    private final ConnectionManager conMan;

    public JDBCPatientManager(ConnectionManager conMan) {
        this.conMan = conMan;
    }

    private static String toIso(Date d) {
        if (d == null) return null;
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    @Override
    public void addPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients(" +
                "namePatient, dniPatient, dobPatient, emailPatient, passwordPatient, " +
                "sexPatient, phoneNumberPatient, healthInsuranceNumberPatient, emergencyContactPatient) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = conMan.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getNamePatient());
            ps.setString(2, p.getDniPatient());
            ps.setString(3, toIso((Date) p.getDobPatient()));
            ps.setString(4, p.getEmailPatient());
            ps.setString(5, p.getPasswordPatient()); // en real: hash (BCrypt/Argon2)
            Sex sex = p.getSexPatient();
            ps.setString(6, sex == null ? null : sex.name());
            ps.setInt(7, p.getPhoneNumberPatient());
            ps.setInt(8, p.getHealthInsuranceNumberPatient());
            ps.setInt(9, p.getEmergencyContactPatient());
            ps.executeUpdate();
        }
    }




}
