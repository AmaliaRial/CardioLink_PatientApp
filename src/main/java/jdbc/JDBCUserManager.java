package jdbc;

import jdbcInterfaces.UserManager;
import pojos.User;

import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class JDBCUserManager implements UserManager{
    private Connection c;
    private ConnectionManager conMan;
    private String privateKey;

    public JDBCUserManager(ConnectionManager conMan) {
        this.conMan = conMan;
    }

    public JDBCUserManager(ConnectionManager conMan, String privateKey) {
        this.conMan = conMan;
        this.c = conMan.getConnection();
        this.privateKey = privateKey;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conMan.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }

    //decrypt RSA + compare hash
    @Override
    public boolean verifyPassword(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conMan.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");

                //decrypt incoming password
                String decryptedPassword = Encryption.decrypt(password, privateKey);

                String hasedPassword = hashPassword(decryptedPassword);

                return storedHash.equals(hasedPassword);
            }
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public User getUserByCredentials(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection c = conMan.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("idUser"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                return u;
            }
            return null;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? ";
        try (Connection c = conMan.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
                        ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setIdUser(rs.getInt("idUser"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                return u;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getRole(String username) {
        String role = null;
        String sql = "SELECT role FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conMan.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                role = rs.getString("role");
            }
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
        return role;
    }

    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users";
        try (Statement st = conMan.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) users.add(rs.getString("username"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

}
