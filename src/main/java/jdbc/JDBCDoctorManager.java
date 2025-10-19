package jdbc;

import jdbcInterfaces.DoctorManager;
import pojos.DiagnosisFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JDBCDoctorManager implements DoctorManager {

    private Connection c;
    private ConnectionManager conMan;

    public JDBCDoctorManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }

    @Override
    public void modifyDiagnosisFile(DiagnosisFile diagnosisFile) {
        try {
            String template = "UPDATE diagnosisFile SET symptoms = ?, diagnosis = ?, medication = ?, date = ?, patientId = ?, WHERE id = ?;";
            PreparedStatement ps;
            ps = c.prepareStatement(template);
            ps.setString(1, diagnosisFile.getSymptoms());
            ps.setString(2, diagnosisFile.getDiagnosis());
            ps.setString(3, diagnosisFile.getMedication());
            ps.setDate(4, java.sql.Date.valueOf(diagnosisFile.getDate()));
            ps.setInt(5, diagnosisFile.getPatientId());
            ps.setInt(6, diagnosisFile.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }

    @Override
    public void deleteDiagnosisFile(int  id) {
        try {
            String template = "DELETE FROM diagnosisFile WHERE id = ?";
            PreparedStatement ps;
            ps = c.prepareStatement(template);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();


        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }
}