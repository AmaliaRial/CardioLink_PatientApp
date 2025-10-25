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
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDYIUWx1NHId4tYQqQoxX4lqI3NKWbEpRpgzfskECrx+fqToxxuex9+1ZB3h/oe+TAkGwOhVKzOfniTriUkRmeDsVfa7b4g2pbbE4Xg7HBwIqq/T2f4Eap3Ha2HFDqJoQRr+Q94NyxsLJogX0ED+f6calappyqkdQC1Fc8DVzD2GmJjY9+qFzmGR0l+dXouW+Ezm2H/PGF8pPgqclXKc3MuPfOEtda9Eh8UJdnv/NGQOFDuHBGeNFjyH7UXLTx4htKu/Tu3fa8kfpxvLa7+8XK+KsHbUoJm/soyOQpo9uJmiYHBU+Qiv1mAUa8IjaVu6SaZlvDQxIq/ovdOMJvSkpQLAgMBAAECggEABi9oxv6rL1UHr4S8cuCnv1YmRhBWH06w9DrMlTOPYbx6SLkVaUgBbAGaoWd9q9Zy/T8hd6pKWzWua/fLichsa7ARHYUn2sgtEbSdytGZaAW7Sq5wEmdsWttkGuzKiwGilo9jIb9nRS7YyP8uuqyaqVpZ+12dJan7RFWNG/Shs1cjjk2WhzgIxXqN4UTKMZQD5DBcQmX/4r4Ddixl68KOxnN4gTXEHN0UhCwKPCdHvdnIiFzykHu72EtBCdGfc5RHXv/VD2cFZYlDJ5pVB5MWv3ukiQVAkG4NRZDzq4yadVZ0MbDEmRrzwqkX9/y9XSVXW+1Nii7DFiUlFfs6ibPtSQKBgQDyuzt9KEnOBFcHQN44uBc10opApMKoV9uVoZbwxv/rsLN9iouXAuUbrJqRPNMBhNpWpM/Tf39B9jgNMmfuEznJYFsTU+KuTZsOTjhlNDjQuquamrUoqGDQg5NeH+mhcrl4MYkYuRcC4SrldpcYfu5KhNBXZ1iz03dCbfpnbMn53wKBgQDj8cgNQr9AzkbBpDT9RhD3BhvoIaY6JebGtISbi1D39e6NwwCjQ5vLhDkWBl9zfgq1jhSCGV7mFCtSI0k0diK61uZlm0+7Mldc6LXnpZjEdal20fABL1KuICAfLoaBW8m2m6B6cXsfVvTtLydQI+NZoOt9OkfErBiOg0L0hwsDVQKBgGpwE9wECKkgWhFCLq/sebEOS7WhCgLL0+w/WXLnsF1ntK1+TUvA5zpFa9n4NAbcfOm1h7SUmfcQwu92hQBuyc42RHmrNSF9wlp5jl1Ckw9ka891u67CdwG4UKzbjZVQO2grQJToxOBsYGUSpZsGPfPLXZiWJt1kA03L8BveJos9AoGAVVsrm3OcJItZyZdQ1GrRXX8nIhS/p1ScB1p/sbNInaG1M9aKvZhKlbosmkfGpHvVTMkoetM/Sw7QbhCSkBeQx8BDRFcVUzb1qe/mdhj3jNG2pKzWn8r1vgh/ns2QRo51iXDbdh5aiZDJZKvcn9DgiKaOqDUTvNzo0Szr/J85C4UCgYEA3/lPBNpPfjzeOHaS2eoRS8W5TuPtrQ1rUHBpDD5ixWOYSNeSZqSS4gXZduvND/Dm6kLrGg/e6qBFr4G+CKxcrCibsBbTkkP9nak5DMQJ3EMAQEUucQcVQ/cQ/AXdW/PjQbtbm5/blMcPlo1mxfy3Ggd32rX7y+V0Bw8NgiUGtvA=";


    public JDBCUserManager(ConnectionManager conMan) {
        this.conMan = conMan;
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
    public void register(String username, String encryptedPassword, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conMan.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, encryptedPassword);  // store RSA-encrypted password directly
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error in the database");
            e.printStackTrace();
        }
    }



    @Override
    public boolean verifyPassword(String username, String inputPassword) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection c = conMan.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String encryptedStoredPass = rs.getString("password");
                String decryptedStoredPass = Encryption.decrypt(encryptedStoredPass, PRIVATE_KEY);
                return decryptedStoredPass.equals(inputPassword);
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption error: " + e.getMessage(), e);
        }
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
