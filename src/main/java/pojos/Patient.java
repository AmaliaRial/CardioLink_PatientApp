package pojos;

import pojos.Interfaces.PatientInterface;
import pojos.enums.Sex;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Patient implements PatientInterface {

    private int idPatient;
    //private String usernamePatient;
    private String namePatient;
    private String surnamePatient;
    private String dniPatient;
    private Date dobPatient;
    private String emailPatient;
    //private String passwordPatient;
    private Sex sexPatient;
    private int phoneNumberPatient;
    private int healthInsuranceNumberPatient;
    private int emergencyContactPatient;
    private int doctorId;
    private int MACadress;
    private List<DiagnosisFile> diagnosisList = new ArrayList<>();
    private int userId;

    // Constructor vacío
    public Patient() {
    }

    // Constructor sin id
    public Patient(String name, String surname, String dni, Date dob, String email, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact, int userId) {
        //this.usernamePatient = username;
        this.namePatient = name;
        this.surnamePatient = surname;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;
        //this.passwordPatient = password;
        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
        this.userId = userId;
    }

    // Constructor con id
    public Patient(int idPatient, String name, String surname, String dni, Date dob, String email, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact, int userId) {
        this.idPatient = idPatient;
        //this.usernamePatient= username;
        this.namePatient = name;
        this.surnamePatient=surname;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;
        //this.passwordPatient = password;
        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
        this.userId = userId;
    }

    public Patient(String name, String surname, String dni, Date dob, String email, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact, int doctorId, int MACadress, List<DiagnosisFile> diagnosisList, int userId) {
        this.namePatient = name;

        this.surnamePatient=surname;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;

        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
        this.doctorId = doctorId;
        this.MACadress = MACadress;
        this.diagnosisList = diagnosisList;
        this.userId = userId;
    }

    // Constructor con id
    public Patient(int idPatient, String name, String dni, Date dob, String email, Sex sex,
                   int phoneNumber, int healthInsuranceNumber, int emergencyContact, int doctorId, int MACadress, List<DiagnosisFile> diagnosisList, int userId) {
        this.idPatient = idPatient;
        this.namePatient = name;
        this.dniPatient = dni;
        this.dobPatient = dob;
        this.emailPatient = email;
        ;
        this.sexPatient = sex;
        this.phoneNumberPatient = phoneNumber;
        this.healthInsuranceNumberPatient = healthInsuranceNumber;
        this.emergencyContactPatient = emergencyContact;
        this.doctorId = doctorId;
        this.MACadress = MACadress;
        this.diagnosisList = diagnosisList;
        this.userId = userId;
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
    public List<DiagnosisFile> getDiagnosisList() { return diagnosisList; }
    public void setDiagnosisList(List<DiagnosisFile> diagnosisList) { this.diagnosisList = diagnosisList; }

   //Nuevos getters y setters
       public String getSurnamePatient() { return surnamePatient; }
    public void setSurnamePatient(String surnamePatient) { this.surnamePatient = surnamePatient; }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

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
                sexPatient == patient.sexPatient &&
                Objects.equals(diagnosisList, patient.diagnosisList) &&
                userId == patient.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPatient, namePatient, dniPatient, dobPatient, emailPatient,
                sexPatient, phoneNumberPatient, healthInsuranceNumberPatient, emergencyContactPatient,
                doctorId, MACadress, diagnosisList, userId);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "idPatient=" + idPatient +
                ", namePatient='" + namePatient + '\'' +
                ", dniPatient='" + dniPatient + '\'' +
                ", dobPatient=" + dobPatient +
                ", emailPatient='" + emailPatient + '\'' +
                ", sexPatient=" + sexPatient +
                ", phoneNumberPatient=" + phoneNumberPatient +
                ", healthInsuranceNumberPatient=" + healthInsuranceNumberPatient +
                ", emergencyContactPatient=" + emergencyContactPatient +
                ", doctorId=" + doctorId +
                ", MACadress=" + MACadress +
                ", diagnosisFile=" + diagnosisList +
                ", userId=" + userId +
                '}';
    }

    public void addDiagnosisToDiagnosisList(DiagnosisFile diagnosis) {
        this.diagnosisList.add(diagnosis);
    }

    public void receiveData( List<int[]> sensorData) {
        DiagnosisFile diagnosis= new DiagnosisFile(this.idPatient);

        String ecgDataString = fromAcquiredIntegerToString(sensorData, 0);
        String edaDataString = fromAcquiredIntegerToString(sensorData, 1);
        //diagnosis.setSensorDataECG(ecgDataString);
        //diagnosis.setSensorDataEDA(edaDataString);
        this.addDiagnosisToDiagnosisList(diagnosis);

        System.out.println("Paciente " + this.namePatient + " recibió datos: ECG="
                + sensorData.get(0)+ " EDA=" + sensorData.get(1));
    }

    @Override
    public String fromAcquiredIntegerToString(List<int[]> sensorData, int channel) {
        // Verificación del canal
        if (channel < 0 || channel > 1) {
            throw new IllegalArgumentException("El canal debe ser 0 (ECG) o 1 (EDA).");
        }

        // Crear un StringBuilder para construir el String final
        StringBuilder sb = new StringBuilder();

        // Iterar sobre la lista de arrays y extraer el valor del canal deseado
        for (int i = 0; i < sensorData.size(); i++) {
            int[] frame = sensorData.get(i);

            // Tomar solo el valor del canal seleccionado
            int value = frame[channel];
            sb.append(value);

            // Agregar ";" si no es el último valor
            if (i < sensorData.size() - 1) {
                sb.append(";");
            }
        }

        // Devolver el String final
        return sb.toString();
    }



}
