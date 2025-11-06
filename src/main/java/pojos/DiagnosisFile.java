package pojos;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class DiagnosisFile {
    private int id;
    private ArrayList<String> symptoms;
    private String diagnosis;
    private String medication;
    private LocalDate date;
    private int patientId;
    private String sensorDataECG;
    private String sensorDataEDA;
    private boolean status;

    public DiagnosisFile(int id) {
        this.date=LocalDate.now();
        this.id = id;

    }
    public DiagnosisFile() {

    }

    public DiagnosisFile(int id, ArrayList<String> symptoms, String diagnosis, String medication, LocalDate date, int patientId) {
        this.id = id;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.medication = medication;
        this.date = date;
        this.patientId= patientId;
    }

    public DiagnosisFile(int id, ArrayList<String> symptoms, String diagnosis, String medication, LocalDate date, int patientId, String sensorDataECG, String sensorDataEDA) {
        this.id = id;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.medication = medication;
        this.date = date;
        this.patientId= patientId;
        this.sensorDataECG = sensorDataECG;
        this.sensorDataEDA = sensorDataEDA;
        this.status = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(ArrayList<String> symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int id) {
        this.patientId = id;
    }

    public String getSensorDataECG() {
        return sensorDataECG;
    }

    public void setSensorDataECG(String sensorDataECG) {
        this.sensorDataECG = sensorDataECG;
    }

    public String getSensorDataEDA() {
        return sensorDataEDA;
    }
    public void setSensorDataEDA(String sensorDataEDA) {
        this.sensorDataEDA = sensorDataEDA;
    }

    public boolean getStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "id='" + id + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", medication='" + medication + '\'' +
                ", date=" + date +
                ", patient id=" + patientId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiagnosisFile that = (DiagnosisFile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
