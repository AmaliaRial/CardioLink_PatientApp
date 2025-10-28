package jdbc;

import jdbcInterfaces.DoctorManager;
import pojos.DiagnosisFile;
import pojos.Patient;
import pojos.Symptoms;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JDBCDoctorManager implements DoctorManager {

    private Connection c;
    private ConnectionManager conMan;

    public JDBCDoctorManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }

    @Override
    public List <DiagnosisFile> listRecentlyFinishedFiles(){
        List<DiagnosisFile> recentFiles = new ArrayList<>();
        try {
            String sql = "SELECT * FROM diagnosisFile WHERE status = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, "false");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DiagnosisFile file = new DiagnosisFile();
                file.setId(rs.getInt("id"));
                file.setDiagnosis(rs.getString("diagnosis"));
                file.setMedication(rs.getString("medication"));
                file.setDate(rs.getDate("date").toLocalDate());
                file.setPatientId(rs.getInt("patientId"));
                file.setSensorDataECG(rs.getString("sensorDataECG"));
                file.setSensorDataEDA(rs.getString("sensorDataEDA"));
                file.setStatus(rs.getBoolean("status"));
                String symptomsStr = rs.getString("symptoms");
                List<String> symptoms = Arrays.asList(symptomsStr.split(","));
                recentFiles.add(file);
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return recentFiles;
    }

    @Override
    public List <DiagnosisFile> listAllFinishedFiles(){
        List<DiagnosisFile> recentFiles = new ArrayList<>();
        try {
            String sql = "SELECT * FROM diagnosisFile WHERE status = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, "true");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DiagnosisFile file = new DiagnosisFile();
                file.setId(rs.getInt("id"));
                file.setDiagnosis(rs.getString("diagnosis"));
                file.setMedication(rs.getString("medication"));
                file.setDate(rs.getDate("date").toLocalDate());
                file.setPatientId(rs.getInt("patientId"));
                file.setSensorDataECG(rs.getString("sensorDataECG"));
                file.setSensorDataEDA(rs.getString("sensorDataEDA"));
                file.setStatus(rs.getBoolean("status"));
                String symptomsStr = rs.getString("symptoms");
                List<String> symptoms = Arrays.asList(symptomsStr.split(","));
                recentFiles.add(file);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return recentFiles;
    }

    @Override
    public void downloadFileInComputer(DiagnosisFile diagnosisFile) throws IOException {
        String fileName = diagnosisFile.getDiagnosis() + ".csv";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("ID,PatientID,Diagnosis,Medication,Date,Status\n");
            writer.write(diagnosisFile.getId() + "," +
                    diagnosisFile.getPatientId() + "," +
                    diagnosisFile.getDiagnosis() + "," +
                    diagnosisFile.getMedication() + "," +
                    diagnosisFile.getDate() + "," +
                    diagnosisFile.getStatus() + "\n\n");

            writer.write("Symptoms:\n");
            for (Symptoms symptom : diagnosisFile.getSymptoms()) {
                writer.write("- " + symptom + "\n");
            }
            writer.write("\nSensor Data:\n");
            writer.write("ECG,EDA\n");

            String[] ecgValues = diagnosisFile.getSensorDataECG().split(",");
            String[] edaValues = diagnosisFile.getSensorDataEDA().split(",");

            int len = Math.min(ecgValues.length, edaValues.length);
            for (int i = 0; i < len; i++) {
                writer.write(ecgValues[i] + "," + edaValues[i] + "\n");
            }

            System.out.println("Archivo descargado correctamente: " + fileName);
        }
    }


    public List<DiagnosisFile> getDiagnosisFilesByPatientId(int patientId) {
        List<DiagnosisFile> diagnosisFiles = new ArrayList<>();
        try {
            String template = "SELECT * FROM diagnosisFile WHERE patientId = ?";
            PreparedStatement ps = c.prepareStatement(template);
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DiagnosisFile file = new DiagnosisFile();
                file.setId(rs.getInt("id"));
                file.setDiagnosis(rs.getString("diagnosis"));
                file.setMedication(rs.getString("medication"));
                file.setDate(rs.getDate("date").toLocalDate());
                file.setPatientId(rs.getInt("patientId"));
                file.setSensorDataECG(rs.getString("sensorDataECG"));
                file.setSensorDataEDA(rs.getString("sensorDataEDA"));
                file.setStatus(rs.getBoolean("status"));
                String symptomsStr = rs.getString("symptoms");
                List<String> symptoms = Arrays.asList(symptomsStr.split(","));
                diagnosisFiles.add(file);
            }
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
        return diagnosisFiles;
    }


    public Patient getPatientByHIN(int healthInsuranceNumber) throws SQLException {
        String sql = "SELECT * FROM patients WHERE healthInsuranceNumberPatient = ?";
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, healthInsuranceNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String sexStr = rs.getString("sexPatient");
                common.enums.Sex sexEnum = null;

                if (sexStr != null) {
                    try {
                        // Normalize possible single-letter or full names
                        if (sexStr.equalsIgnoreCase("M")) sexEnum = common.enums.Sex.MALE;
                        else if (sexStr.equalsIgnoreCase("F")) sexEnum = common.enums.Sex.FEMALE;
                        else sexEnum = common.enums.Sex.valueOf(sexStr.toUpperCase());
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
                        rs.getInt("userId"));
                return p;
            }
            return null;
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void modifyDiagnosisFile(DiagnosisFile diagnosisFile) {
        try {
            String template = "UPDATE diagnosisFile SET symptoms = ?, diagnosis = ?, medication = ?, date = ?, patientId = ?, WHERE id = ?;";
            PreparedStatement ps;
            ps = c.prepareStatement(template);
            String symptomsSerialized = diagnosisFile.getSymptoms() == null ? null :
                    diagnosisFile.getSymptoms().stream().map(Object::toString).collect(Collectors.joining(", "));
            ps.setString(1, symptomsSerialized);
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
