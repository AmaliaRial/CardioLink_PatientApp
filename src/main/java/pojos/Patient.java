package pojos;

import common.enums.Sex;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Patient {
    //Anotaciones Carmen --> Por ahora en la app del paciente se busca por dni y contraseña, porque quiero aclarar con el grupo
    //si el paciente tendrá nombre y apellido o solo nombre. Si buscamos por username o por contraseña. Porque por ahora
    // no pide el username en la app, solo sus datos personales y la contraseña.
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
    private List<DiagnosisFile> diagnosisList = new ArrayList<>();

    // Constructor vacío
    public Patient() {
    }

    // Constructor sin id
    public Patient(String name, String dni, Date dob, String email, String password, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact) {
        this.namePatient = name;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;
        this.passwordPatient = password;
        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
    }

    // Constructor con id
    public Patient(int idPatient, String name, String dni, Date dob, String email, String password, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact) {
        this.idPatient = idPatient;
        this.namePatient = name;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;
        this.passwordPatient = password;
        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
    }

    public Patient(String name, String dni, Date dob, String email, String password, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact, int doctorId, int MACadress, List<DiagnosisFile> diagnosisList) {
        this.namePatient = name;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;
        this.passwordPatient = password;
        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
        this.doctorId = doctorId;
        this.MACadress = MACadress;
        this.diagnosisList = diagnosisList;
    }

    // Constructor con id
    public Patient(int idPatient, String name, String dni, Date dob, String email, String password, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact, int doctorId, int MACadress, List<DiagnosisFile> diagnosisList) {
        this.idPatient = idPatient;
        this.namePatient = name;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;
        this.passwordPatient = password;
        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
        this.doctorId = doctorId;
        this.MACadress = MACadress;
        this.diagnosisList = diagnosisList;
    }

    public int getIdPatient() { return idPatient; }
    public void setIdPatient(int idPatient) { this.idPatient = idPatient; }
    public String getNamePatient() { return namePatient; }
    public void setNamePatient(String namePatient) { this.namePatient = namePatient; }
    public String getDniPatient() { return dniPatient; }
    public void setDniPatient(String dniPatient) { this.dniPatient = dniPatient; }
    public Date getDobPatient() { return dobPatient; }
    public void setDobPatient(Date dobPatient) { this.dobPatient = dobPatient; }
    public String getEmailPatient() { return emailPatient; }
    public void setEmailPatient(String emailPatient) { this.emailPatient = emailPatient; }
    public String getPasswordPatient() { return passwordPatient; }
    public void setPasswordPatient(String passwordPatient) { this.passwordPatient = passwordPatient; }
    public Sex getSexPatient() { return sexPatient; }
    public void setSexPatient(Sex sexPatient) { this.sexPatient = sexPatient; }
    public int getPhoneNumberPatient() { return phoneNumberPatient; }
    public void setPhoneNumberPatient(int phoneNumberPatient) { this.phoneNumberPatient = phoneNumberPatient; }
    public int getHealthInsuranceNumberPatient() { return healthInsuranceNumberPatient; }
    public void setHealthInsuranceNumberPatient(int healthInsuranceNumberPatient) { this.healthInsuranceNumberPatient = healthInsuranceNumberPatient; }
    public int getEmergencyContactPatient() { return emergencyContactPatient; }
    public void setEmergencyContactPatient(int emergencyContactPatient) { this.emergencyContactPatient = emergencyContactPatient; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public int getMACadress() { return MACadress; }
    public void setMACadress(int MACadress) { this.MACadress = MACadress; }
    public List<DiagnosisFile> getDiagnosisFile() { return diagnosisList; }
    public void setDiagnosisFile(List<DiagnosisFile> diagnosisList) { this.diagnosisList = diagnosisList; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return idPatient == patient.idPatient &&
                phoneNumberPatient == patient.phoneNumberPatient &&
                healthInsuranceNumberPatient == patient.healthInsuranceNumberPatient &&
                emergencyContactPatient == patient.emergencyContactPatient &&
                doctorId == patient.doctorId &&
                MACadress == patient.MACadress &&
                Objects.equals(namePatient, patient.namePatient) &&
                Objects.equals(dniPatient, patient.dniPatient) &&
                Objects.equals(dobPatient, patient.dobPatient) &&
                Objects.equals(emailPatient, patient.emailPatient) &&
                Objects.equals(passwordPatient, patient.passwordPatient) &&
                sexPatient == patient.sexPatient &&
                Objects.equals(diagnosisList, patient.diagnosisList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPatient, namePatient, dniPatient, dobPatient, emailPatient, passwordPatient,
                sexPatient, phoneNumberPatient, healthInsuranceNumberPatient, emergencyContactPatient,
                doctorId, MACadress, diagnosisList);
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
                ", diagnosisFile=" + diagnosisList +
                '}';
    }
}
