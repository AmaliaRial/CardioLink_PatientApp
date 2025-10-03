import jdbc.ConnectionManager;

public class Main {
    public static void main(String[] args) {
        ConnectionManager cm = new ConnectionManager();
        System.out.println("Base de datos inicializada correctamente.");
        cm.close();
    }
}
