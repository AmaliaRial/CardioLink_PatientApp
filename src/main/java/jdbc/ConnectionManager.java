package jdbc;

import jdbcInterfaces.PatientManager;
import jdbcInterfaces.UserManager;

import java.sql.*;

public class ConnectionManager {

    private Connection c;
    private PatientManager patientMan;
    private UserManager userMan;



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

    public UserManager getUserMan() { return userMan; }

    public ConnectionManager() {
        connect();
        patientMan = new JDBCPatientManager(this);
        userMan = new JDBCUserManager(this);
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

            DatabaseMetaData meta = c.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "patients", "usernamePatient");
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE patients ADD COLUMN usernamePatient TEXT");
            }
            rs = meta.getColumns(null, null, "patients", "surnamePatient");
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE patients ADD COLUMN surnamePatient TEXT");
            }


            String createTablePatients =
                    "CREATE TABLE IF NOT EXISTS patients (" +
                            "  idPatient INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  usernamePatient TEXT UNIQUE NOT NULL," +
                            "  namePatient TEXT NOT NULL," +
                            "  surnamePatient TEXT NOT NULL," +
                            "  dniPatient TEXT UNIQUE NOT NULL," +
                            "  dobPatient DATE NOT NULL," +
                            "  emailPatient TEXT UNIQUE NOT NULL," +
                            "  sexPatient TEXT NOT NULL," +
                            "  phoneNumberPatient INTEGER UNIQUE NOT NULL," +
                            "  healthInsuranceNumberPatient INTEGER UNIQUE NOT NULL," +
                            "  emergencyContactPatient INTEGER NOT NULL" +
                            ");";
            st.executeUpdate(createTablePatients);

            String createTableUsers =
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "  idUser INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  username TEXT UNIQUE NOT NULL," +
                            "  password TEXT NOT NULL," +
                            "  role TEXT NOT NULL" +
                            ");";
            st.executeUpdate(createTableUsers);
        } catch (SQLException sqlE) {
            if (!sqlE.getMessage().toLowerCase().contains("already exists")) {
                System.out.println("Error creating or updating schema");
                sqlE.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        } catch (SQLException e) {
            System.out.println("Error closing the database connection");
            e.printStackTrace();
        }
    }
}
