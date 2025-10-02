package jdbc;
import java.sql.*;


public class ConnectionManager {
    private Connection c;
    public Connection getConnection() {
        return c;
    }
}

