package jdbc;

import jdbcInterfaces.PatientManager;
import pojos.Patient;
import common.enums.Sex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class JDBCPatientManager implements PatientManager {

    private final ConnectionManager conMan;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    public JDBCPatientManager(ConnectionManager conMan) {
        this.conMan = conMan;
    }

    @Override
    public void addPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients(" +
                "namePatient, surnamePatient, dniPatient, dobPatient, emailPatient, " +
                "sexPatient, phoneNumberPatient, healthInsuranceNumberPatient, emergencyContactPatient) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = conMan.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getNamePatient());
            ps.setString(2, p.getSurnamePatient());
            ps.setString(3, p.getDniPatient());
            // Guardar la fecha como String en formato dd/MM/yyyy
            String dobString = p.getDobPatient() != null ? SDF.format(p.getDobPatient()) : null;
            ps.setString(4, dobString);
            ps.setString(5, p.getEmailPatient());

            Sex sex = p.getSexPatient();
            ps.setString(6, sex == null ? null : sex.name());
            ps.setInt(7, p.getPhoneNumberPatient());
            ps.setInt(8, p.getHealthInsuranceNumberPatient());
            ps.setInt(9, p.getEmergencyContactPatient());

            ps.executeUpdate();
        }
    }

    public Patient getPatientByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE userId = ?";
        try (Connection c = conMan.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String sexStr = rs.getString("sexPatient");
                Sex sexEnum = null;

                if (sexStr != null) {
                    try {
                        // Normalize possible single-letter or full names
                        if (sexStr.equalsIgnoreCase("M")) sexEnum = Sex.MALE;
                        else if (sexStr.equalsIgnoreCase("F")) sexEnum = Sex.FEMALE;
                        else sexEnum = Sex.valueOf(sexStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("⚠️ Invalid sex value in DB: " + sexStr);
                    }
                }

                Patient p = new Patient(rs.getInt("idPatient"),
                        rs.getString("namePatient"),
                        rs.getString("surnamePatient"),
                        rs.getString("dniPatient"),
                        rs.getDate("dobPatient"),
                        rs.getString("emailPatient"),
                        sexEnum,
                        rs.getInt("phoneNumberPatient"),
                        rs.getInt("healthInsuranceNumberPatient"),
                        rs.getInt("emergencyContactPatient"),
                        rs.getInt("userId"))
;
                return p;
            }
            return null;
        }
    }


    /*
    USERNAME AND PASSWORDS ARE NO LONGER STORED IN PATIENTS TABLE

    public Patient getPatientByUsernameAndPassword(String username, String password) throws Exception {
        String sql = "SELECT * FROM patients WHERE usernamePatient = ? AND passwordPatient = ?";
        try (PreparedStatement ps = conMan.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Patient p = new Patient();
                p.setUsernamePatient(rs.getString("usernamePatient"));
                p.setNamePatient(rs.getString("namePatient"));
                p.setSurnamePatient(rs.getString("surnamePatient"));
                p.setDniPatient(rs.getString("dniPatient"));

                String dobStr = rs.getString("dobPatient");
                try {
                    p.setDobPatient(dobStr != null ? SDF.parse(dobStr) : null);
                } catch (ParseException e) {
                    p.setDobPatient(null);
                }
                p.setEmailPatient(rs.getString("emailPatient"));
                p.setPasswordPatient(rs.getString("passwordPatient"));
                String sexStr = rs.getString("sexPatient");
                p.setSexPatient(sexStr == null ? null : Sex.valueOf(sexStr));
                p.setPhoneNumberPatient(rs.getInt("phoneNumberPatient"));
                p.setHealthInsuranceNumberPatient(rs.getInt("healthInsuranceNumberPatient"));
                p.setEmergencyContactPatient(rs.getInt("emergencyContactPatient"));
                return p;
            }
            return null;
        }
    }*/
}
