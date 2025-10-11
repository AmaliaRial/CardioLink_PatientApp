package pojos;

import common.enums.Sex;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class Patient {
    private int idPatient;
    private String namePatient;
    private String dniPatient;
    private Date dobPatient;
    private String emailPatient;
    private String passwordPatient;
    private Sex sexPatient;
    private int phoneNumberPatient;
    private int healthInsuranceNumberPatient;
    private int emergencyContactPatient;
    private int doctorId;
    private int MACadress;
    List<String> diagnosisFile = new ArrayList<>();

    public Patient(int idPatient, String name, String dni, Date dob, String email, String password, Sex sex, int phoneNumber, int healthInsuranceNumber, int emergencyContact, int doctorId, int MACadress, List<String> diagnosisFile) {
        this.idPatient = idPatient;
        this.namePatient =name;
        this.dniPatient =dni;
        this.dobPatient =dob;
        this.emailPatient =email;
        this.passwordPatient =password;
        this.sexPatient =sex;
        this.phoneNumberPatient =phoneNumber;
        this.healthInsuranceNumberPatient =healthInsuranceNumber;
        this.emergencyContactPatient =emergencyContact;
        this.doctorId = doctorId;
        this.MACadress = MACadress;
        this.diagnosisFile = diagnosisFile;
    }

    public int getIdPatient() {
        return idPatient;
    }
    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }
    public String getNamePatient() {
        return namePatient;
    }
    public void setNamePatient(String namePatient) {
        this.namePatient = namePatient;
    }
    public String getDniPatient() {
        return dniPatient;
    }
    public void setDniPatient(String dniPatient) {
        this.dniPatient = dniPatient;
    }
    public Date getDobPatient() { return dobPatient;}
    public void setDobPatient(Date dobPatient) {
        this.dobPatient = dobPatient;
    }
    public String getEmailPatient() {
        return emailPatient;
    }
    public void setEmailPatient(String emailPatient) {
        this.emailPatient = emailPatient;
    }
    public String getPasswordPatient() {
        return passwordPatient;
    }
    public void setPasswordPatient(String passwordPatient) {
        this.passwordPatient = passwordPatient;
    }
    public int getPhoneNumberPatient() { return phoneNumberPatient;}
    public void setPhoneNumberPatient(int phoneNumberPatient) {
        this.phoneNumberPatient = phoneNumberPatient;
    }
    public int getHealthInsuranceNumberPatient() {
        return healthInsuranceNumberPatient;
    }
    public void setHealthInsuranceNumberPatient(int healthInsuranceNumberPatient) {
        this.healthInsuranceNumberPatient = healthInsuranceNumberPatient; }
    public int getEmergencyContactPatient() {
        return emergencyContactPatient;
    }
    public void setEmergencyContactPatient(int emergencyContactPatient) {
        this.emergencyContactPatient = emergencyContactPatient;}
    public int getDoctorId() { return doctorId;}
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public int getMACadress() { return MACadress; }
    public void setMACadress(int MACadress) { this.MACadress = MACadress; }
    public List<String> getDiagnosisFile() { return diagnosisFile; }
    public void setDiagnosisFile(List<String> diagnosisFile) { this.diagnosisFile = diagnosisFile; }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return idPatient == patient.idPatient && phoneNumberPatient == patient.phoneNumberPatient && healthInsuranceNumberPatient == patient.healthInsuranceNumberPatient && emergencyContactPatient == patient.emergencyContactPatient && doctorId == patient.doctorId && MACadress == patient.MACadress && Objects.equals(namePatient, patient.namePatient) && Objects.equals(dniPatient, patient.dniPatient) && Objects.equals(dobPatient, patient.dobPatient) && Objects.equals(emailPatient, patient.emailPatient) && Objects.equals(passwordPatient, patient.passwordPatient) && sexPatient == patient.sexPatient && Objects.equals(diagnosisFile, patient.diagnosisFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPatient, namePatient, dniPatient, dobPatient, emailPatient, passwordPatient, sexPatient, phoneNumberPatient, healthInsuranceNumberPatient, emergencyContactPatient, doctorId, MACadress, diagnosisFile);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "idPatient=" + idPatient +
                ", namePatient='" + namePatient + '\'' +
                ", dniPatient='" + dniPatient + '\'' +
                ", dobPatient=" + dobPatient +
                ", emailPatient='" + emailPatient + '\'' +
                ", passwordPatient='" + passwordPatient + '\'' +
                ", sexPatient=" + sexPatient +
                ", phoneNumberPatient=" + phoneNumberPatient +
                ", healthInsuranceNumberPatient=" + healthInsuranceNumberPatient +
                ", emergencyContactPatient=" + emergencyContactPatient +
                ", doctorId=" + doctorId +
                ", MACadress=" + MACadress +
                ", diagnosisFile=" + diagnosisFile +
                '}';
    }
    public Sex getSexPatient() {
        return sexPatient;
    }
    public void setSexPatient(Sex sexPatient) {
        this.sexPatient = sexPatient;
    }

    // Constructor  (sin id):
    public Patient(String name, String dni, Date dob, String email, String password,
                   Sex sex, int phoneNumber, int healthInsuranceNumber, int emergencyContact) {
        this(0, name, dni, dob, email, password, sex, phoneNumber, healthInsuranceNumber, emergencyContact);
    }
}
