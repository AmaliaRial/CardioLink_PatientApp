package pojos;
import java.time.LocalDate;
import java.util.Objects;

public class MedicalRecord {
    private String id;
    private String symptoms;
    private String diagnosis;
    private String medication;
    private LocalDate date;


    public MedicalRecord() {
    }

    public MedicalRecord(String id, String symptoms, String diagnosis, String medication, LocalDate date) {
        this.id = id;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.medication = medication;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "id='" + id + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", medication='" + medication + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
