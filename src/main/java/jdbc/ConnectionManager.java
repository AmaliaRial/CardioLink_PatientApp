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
                c = DriverManager.getConnection("jdbc:sqlite:CardioLink.db");
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
        ensureSchema(c);
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:CardioLink.db");
            c.createStatement().execute("PRAGMA foreign_keys=ON");
        } catch (ClassNotFoundException cnfE) {
            System.out.println("Databases libraries not loaded");
            cnfE.printStackTrace();
        } catch (SQLException sqlE) {
            System.out.println("Error with database");
            sqlE.printStackTrace();
        }
    }

    public void ensureSchema(Connection c) {
        try (Statement st = c.createStatement()) {


            String createTableUsers =
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "  idUser INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  username TEXT UNIQUE NOT NULL," +
                            "  password TEXT NOT NULL," +
                            "  role TEXT NOT NULL" +
                            ");";
            st.executeUpdate(createTableUsers);

            String createTablePatients =
                    "CREATE TABLE IF NOT EXISTS patients (" +
                            "  idPatient INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  namePatient TEXT NOT NULL," +
                            "  surnamePatient TEXT NOT NULL," +
                            "  dniPatient TEXT UNIQUE NOT NULL," +
                            "  dobPatient DATE NOT NULL," +
                            "  emailPatient TEXT UNIQUE NOT NULL," +
                            "  sexPatient TEXT NOT NULL," +
                            "  phoneNumberPatient INTEGER UNIQUE NOT NULL," +
                            "  healthInsuranceNumberPatient INTEGER UNIQUE NOT NULL," +
                            "  emergencyContactPatient INTEGER NOT NULL," +
                            "  userId INTEGER UNIQUE, " +
                            "  FOREIGN KEY(userId) REFERENCES users(idUser)" +
                            ");";
            st.executeUpdate(createTablePatients);


            String createTableDiagnosisFile =
                    "CREATE TABLE IF NOT EXISTS diagnosisFile (" +
                            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "  symptoms TEXT," +
                            "  diagnosis TEXT," +
                            "  medication TEXT," +
                            "  date DATE NOT NULL," +
                            "  patientId INTEGER NOT NULL," +
                            "  sensorDataECG TEXT," +
                            "  sensorDataEDA TEXT," +
                            "  FOREIGN KEY(patientId) REFERENCES patients(idPatient)" +
                            ");";
            st.executeUpdate(createTableDiagnosisFile);

            //Now that tables exist, check columns safely
            DatabaseMetaData meta = c.getMetaData();
            ResultSet rs;

            rs = meta.getColumns(null, null, "patients", "userId");
            if (!rs.next()) {
                // SQLite does not support adding foreign keys in ALTER TABLE,
                // so we only add the column (relationship logic is handled manually)
                st.executeUpdate("ALTER TABLE patients ADD COLUMN userId INTEGER UNIQUE");
                System.out.println("Added userId column to patients table.");
            }

            rs = meta.getColumns(null, null, "patients", "surnamePatient");
            if (!rs.next()) {
                st.executeUpdate("ALTER TABLE patients ADD COLUMN surnamePatient TEXT");
            }

            System.out.println("Database schema verified successfully.");

        } catch (SQLException sqlE) {
            System.out.println("Error creating or updating schema");
            sqlE.printStackTrace();
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
