package jdbcInterfaces;

import pojos.DiagnosisFile;

import java.sql.SQLException;

public interface DoctorManager {
    void modifyDiagnosisFile(DiagnosisFile diagnosisFile) throws SQLException;
    void deleteDiagnosisFile(int id) throws SQLException;
}
