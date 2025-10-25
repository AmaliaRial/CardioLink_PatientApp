package jdbcInterfaces;

import pojos.Patient;

import java.sql.SQLException;

public interface PatientManager {
    void addPatient(Patient p) throws SQLException;
}
