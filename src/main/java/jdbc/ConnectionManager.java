package jdbc;

import jdbcInterfaces.PatientManager;
import java.sql.*;

public class ConnectionManager {

    private Connection c;
    private PatientManager patientMan;

    public Connection getConnection() {
        try {
            if (c == null || c.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                c = DriverManager.getConnection("jdbc:sqlite:./db/CardioLink.db");
                c.createStatement().execute("PRAGMA foreign_keys=ON");
            }
        } catch (Exception e) {
            System.out.println("Error reopening the database connection");
            e.printStackTrace();
        }
        return c;
    }

    public PatientManager getPatientMan() { return patientMan; }

    public ConnectionManager() {
        connect();
        patientMan = new JDBCPatientManager(this);
        ensureSchema();
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

    public void ensureSchema() {
        try (Statement st = c.createStatement()) {
            String createTablePatients =
                    "CREATE TABLE IF NOT EXISTS patients (" +
                            "  idPatient INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  namePatient TEXT NOT NULL," +
                            "  dniPatient TEXT UNIQUE NOT NULL," +
                            "  dobPatient TEXT NOT NULL," +
                            "  emailPatient TEXT NOT NULL," +
                            "  passwordPatient TEXT NOT NULL," +
                            "  sexPatient TEXT NOT NULL," +
                            "  phoneNumberPatient INTEGER NOT NULL," +
                            "  healthInsuranceNumberPatient INTEGER NOT NULL," +
                            "  emergencyContactPatient INTEGER NOT NULL" +
                            ");";
            st.executeUpdate(createTablePatients);
        } catch (SQLException sqlE) {
            if (!sqlE.getMessage().toLowerCase().contains("already exists")) {
                System.out.println("Error creating schema");
                sqlE.printStackTrace();
            }
        }
    }

    public void close() {
        try { if (c != null) c.close(); }
        catch (SQLException e) {
            System.out.println("Error closing the database");
            e.printStackTrace();
        }
    }
}
