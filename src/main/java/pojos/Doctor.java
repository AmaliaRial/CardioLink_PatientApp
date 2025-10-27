package pojos;

import common.enums.Sex;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.List;

public class Doctor {
    private int idDoctor;
    private String nameDoctor;
    private String dniDoctor;
    private Date dobDoctor;
    private String emailDoctor;
    // private String passwordDoctor;
    private Sex sexDoctor;
    private List<Patient> assignedPatients = new ArrayList<>();
    private int id;

    public Doctor(int idDoc, String name, String dni, Date dob, String email, Sex sex, List<Patient> patients, int id) {
        this.idDoctor = idDoc;
        this.nameDoctor = name;
        this.dniDoctor = dni;
        this.dobDoctor = dob;
        this.emailDoctor = email;
        this.sexDoctor = sex;
        this.assignedPatients = patients;
        this.id = id;
    }

    public String getNameDoctor() {
        return nameDoctor;}
    public void setNameDoctor(String nameDoctor) {
    this.nameDoctor = nameDoctor;}
    public String getDniDoctor() {
        return dniDoctor;}
    public void setDniDoctor(String dniDoctor) {
        this.dniDoctor = dniDoctor;}
    public Date getDobDoctor() {
        return dobDoctor;}
    public void setDobDoctor(Date dobDoctor) {
        this.dobDoctor = dobDoctor;}
    public String getEmailDoctor() {
        return emailDoctor;}
    public void setEmailDoctor(String emailDoctor) {
        this.emailDoctor = emailDoctor;}
    public Sex getSexDoctor() {
        return sexDoctor;}
    public void setIdDoctor(Sex sexDoctor) {
        this.sexDoctor = sexDoctor;}
    public List<Patient> getAssignedPatients() {
        return assignedPatients;}
    public void setAssignedPatients(List<Patient> assignedPatients) {
        this.assignedPatients = assignedPatients;}
    public int getId() {
        return id;}
    public void setId(int id){
        this.id = id;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return idDoctor == doctor.idDoctor &&
                Objects.equals(nameDoctor, doctor.nameDoctor) &&
                Objects.equals(dniDoctor, doctor.dniDoctor) &&
                Objects.equals(dobDoctor, doctor.dobDoctor) &&
                Objects.equals(emailDoctor, doctor.emailDoctor) &&
                sexDoctor == doctor.sexDoctor &&
                Objects.equals(assignedPatients, doctor.assignedPatients) &&
                id == doctor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDoctor, nameDoctor, dniDoctor, dobDoctor, emailDoctor, sexDoctor, assignedPatients, id);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "idDoctor=" + idDoctor +
                ", name='" + nameDoctor + '\'' +
                ", dni='" + dniDoctor + '\'' +
                ", dob=" + dobDoctor +
                ", email='" + emailDoctor + '\'' +
                ", sex=" + sexDoctor +
                ", assigned Patients=" + assignedPatients +
                ", id=" + id +
                '}';
    }


}
