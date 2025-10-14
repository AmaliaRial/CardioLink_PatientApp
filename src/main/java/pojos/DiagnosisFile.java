package pojos;
import java.time.LocalDate;
import java.util.Objects;

public class DiagnosisFile {
    private int id;
    private String symptoms;
    private String diagnosis;
    private String medication;
    private LocalDate date;
    private int patientId;
    private String sensorDataECG;
    private String sensorDataEDA;

    public DiagnosisFile(int id) {
        this.date=LocalDate.now();
        this.id = id;

    }

    public DiagnosisFile(int id, String symptoms, String diagnosis, String medication, LocalDate date, int patientId) {
        this.id = id;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.medication = medication;
        this.date = date;
        this.patientId= patientId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
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
