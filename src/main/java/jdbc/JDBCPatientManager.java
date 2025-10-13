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
            java.util.Date utilDate = p.getDobPatient();
            java.sql.Date sqlDate = (utilDate == null) ? null : new java.sql.Date(utilDate.getTime());
            ps.setDate(3, sqlDate);

            ps.setString(4, p.getEmailPatient());
            ps.setString(5, p.getPasswordPatient());
            Sex sex = p.getSexPatient();
            ps.setString(6, sex == null ? null : sex.name());
            ps.setInt(7, p.getPhoneNumberPatient());
            ps.setInt(8, p.getHealthInsuranceNumberPatient());
            ps.setInt(9, p.getEmergencyContactPatient());
            ps.executeUpdate();
        }
    }

    public Patient getPatientByDniAndPassword(String dni, String password) throws Exception {
        String sql = "SELECT * FROM patients WHERE dniPatient = ? AND passwordPatient = ?";
        try (PreparedStatement ps = conMan.getConnection().prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Patient p = new Patient();
                p.setNamePatient(rs.getString("namePatient"));
                p.setDniPatient(rs.getString("dniPatient"));
                p.setDobPatient(rs.getDate("dobPatient"));
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
