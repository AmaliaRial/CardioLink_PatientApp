package pojos;

import common.enums.Sex;

import java.util.Date;
import java.util.Objects;
//bueno

public class Patient {
    private int idPatient;
    private String name;
    private String dni;
    private Date dob;
    private String email;
    private String password;
    private Sex sex;
    private int phoneNumber;
    private int healthInsuranceNumber;
    private int emergencyContact;

    public Patient(int idPatient, String name, String dni, Date dob, String email,String password, Sex sex, int phoneNumber, int healthInsuranceNumber, int emergencyContact) {
        this.idPatient = idPatient;
        this.name=name;
        this.dni=dni;
        this.dob=dob;
        this.email=email;
        this.password=password;
        this.sex=sex;
        this.phoneNumber=phoneNumber;
        this.healthInsuranceNumber=healthInsuranceNumber;
        this.emergencyContact=emergencyContact;
    }

    public int getIdPatient() {
        return idPatient;
    }
    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDni() {
        return dni;
    }
    public void setDni(String dni) {
        this.dni = dni;
    }
    public Date getDob() {
        return dob;
    }
    public void setDob(Date dob) {
        this.dob = dob;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public int getHealthInsuranceNumber() {
        return healthInsuranceNumber;
    }
    public void setHealthInsuranceNumber(int healthInsuranceNumber) {
        this.healthInsuranceNumber = healthInsuranceNumber;
    }
    public int getEmergencyContact() {
        return emergencyContact;
    }
    public void setEmergencyContact(int emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return idPatient == patient.idPatient && phoneNumber == patient.phoneNumber && healthInsuranceNumber == patient.healthInsuranceNumber && emergencyContact == patient.emergencyContact && Objects.equals(name, patient.name) && Objects.equals(dni, patient.dni) && Objects.equals(dob, patient.dob) && Objects.equals(email, patient.email) && Objects.equals(password, patient.password) && sex == patient.sex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPatient, name, dni, dob, email, password, sex, phoneNumber, healthInsuranceNumber, emergencyContact);
    }


    @Override
    public String toString() {
        return "Patient{" +
                "idPatient=" + idPatient +
                ", name='" + name + '\'' +
                ", dni='" + dni + '\'' +
                ", dob=" + dob +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", sex=" + sex +
                ", phoneNumber=" + phoneNumber +
                ", healthInsuranceNumber=" + healthInsuranceNumber +
                ", emergencyContact=" + emergencyContact +
                '}';
    }
}
