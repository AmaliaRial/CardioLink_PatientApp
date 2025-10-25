package jdbcInterfaces;

import pojos.User;

import java.sql.SQLException;

public interface UserManager {

    void register(String username, String password, String role);
    boolean verifyPassword(String username, String password);
    String getRole(String username);
    User getUserByCredentials(String username, String password) throws SQLException;

    User getUserByUsername(String username);
}
