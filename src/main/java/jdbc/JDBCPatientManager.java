package jdbc;

import jdbcInterfaces.PatientManager;
import pojos.Patient;
import common.enums.Sex;

import java.sql.*;
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
                "usernamePatient, namePatient, surnamePatient, dniPatient, dobPatient, emailPatient, passwordPatient, " +
                "sexPatient, phoneNumberPatient, healthInsuranceNumberPatient, emergencyContactPatient) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = conMan.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getUsernamePatient());
            ps.setString(2, p.getNamePatient());
            ps.setString(3, p.getSurnamePatient());
            ps.setString(4, p.getDniPatient());
            // Guardar la fecha como String en formato dd/MM/yyyy
            String dobString = p.getDobPatient() != null ? SDF.format(p.getDobPatient()) : null;
            ps.setString(5, dobString);
            ps.setString(6, p.getEmailPatient());
            ps.setString(7, p.getPasswordPatient());
            Sex sex = p.getSexPatient();
            ps.setString(8, sex == null ? null : sex.name());
            ps.setInt(9, p.getPhoneNumberPatient());
            ps.setInt(10, p.getHealthInsuranceNumberPatient());
            ps.setInt(11, p.getEmergencyContactPatient());
            ps.executeUpdate();
        }
    }

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
    }
}
