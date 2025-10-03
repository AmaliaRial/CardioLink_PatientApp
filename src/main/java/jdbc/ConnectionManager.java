package jdbc;
import jdbcInterfaces.PatientManager;

import java.sql.*;
//import java.jdbcInterfaces.PatientManager;

public class ConnectionManager {

    private Connection c;
    private PatientManager patientMan;

    public Connection getConnection() {
        return c;
    }

    public ConnectionManager() {
        this.connect();
        this.patientMan= new JDBCPatientManager(this);
        this.createTables();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:./db/CardioLink.db");
            c.createStatement().execute("PRAGMA foreign_keys=ON");
        } catch (ClassNotFoundException cnfE) {
            System.out.println("Databases libraries not loaded");
            cnfE.printStackTrace();
        } catch (SQLException sqlE) {
            System.out.println("Error with database");
            sqlE.printStackTrace();
        }
    }


    public void close() {
        try {
            c.close();
        } catch (SQLException e) {
            System.out.println("Error closing the database");
            e.printStackTrace();
        }
    }

    private void createTables() {
        try {
            Statement createPatientsTable = c.createStatement();
            String createTablePatients = " CREATE TABLE patients("
                    + "	idPatient INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "	namePatient TEXT NOT NULL,"
                    + " dniPatient TEXT UNIQUE NOT NULL,"
                    +"	dobPatient DATE NOT NULL,"
                    +" emailPatient TEXT NOT NULL,"
                    +" passwordPatient TEXT NOT NULL,"
                    +" sexPatient TEXT NOT NULL,"
                    +" phoneNumberPatient INTEGER NOT NULL,"
                    +" healthInsuranceNumberPatient INTEGER NOT NULL,"
                    + "	emergencyContactPatient INTEGER NOT NULL);";
            createPatientsTable.executeUpdate(createTablePatients);
            createPatientsTable.close();

        }catch (SQLException sqlE) {
            if (sqlE.getMessage().contains("already exist")){

            }
            else {
                System.out.println("Error in query");
                sqlE.printStackTrace();
            }
        }
    }

    public PatientManager getPatientMan() {
        return patientMan;
    }

}

