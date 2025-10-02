package jdbc;

import jdbcInterfaces.PatientManager;
import java.sql.*;


public class JDBCPatientManager implements PatientManager {

    private Connection c;
    private ConnectionManager conMan;

    public JDBCPatientManager(ConnectionManager conMan) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
    }


}
