package jdbcInterfaces;

public interface UserManager {

    void register(String username, String password, String role);
    boolean verifyPassword(String username, String password);
    String getRole(String username);

}
