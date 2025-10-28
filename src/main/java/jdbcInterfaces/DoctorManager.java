package jdbcInterfaces;

import pojos.DiagnosisFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface DoctorManager {
    void modifyDiagnosisFile(DiagnosisFile diagnosisFile) throws SQLException;
    void deleteDiagnosisFile(int id) throws SQLException;
    List<DiagnosisFile> listRecentlyFinishedFiles();
    List<DiagnosisFile> listAllFinishedFiles() ;
    void downloadFileInComputer(DiagnosisFile diagnosisFile) throws IOException;
}
