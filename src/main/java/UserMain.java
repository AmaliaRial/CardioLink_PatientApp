import jdbc.ConnectionManager;
import jdbc.Encryption;
import jdbc.JDBCUserManager;

public class UserMain {
    // Replace these with the actual Base64 keys generated
    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2CFFsdTRyHeLWEKkKMV+JaiNzSlmxKUaYM37JBAq8fn6k6McbnsfftWQd4f6HvkwJBsDoVSszn54k64lJEZng7FX2u2+INqW2xOF4OxwcCKqv09n+BGqdx2thxQ6iaEEa/kPeDcsbCyaIF9BA/n+nGpWqacqpHUAtRXPA1cw9hpiY2Pfqhc5hkdJfnV6LlvhM5th/zxhfKT4KnJVynNzLj3zhLXWvRIfFCXZ7/zRkDhQ7hwRnjRY8h+1Fy08eIbSrv07t32vJH6cby2u/vFyvirB21KCZv7KMjkKaPbiZomBwVPkIr9ZgFGvCI2lbukmmZbw0MSKv6L3TjCb0pKUCwIDAQAB";
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDYIUWx1NHId4tYQqQoxX4lqI3NKWbEpRpgzfskECrx+fqToxxuex9+1ZB3h/oe+TAkGwOhVKzOfniTriUkRmeDsVfa7b4g2pbbE4Xg7HBwIqq/T2f4Eap3Ha2HFDqJoQRr+Q94NyxsLJogX0ED+f6calappyqkdQC1Fc8DVzD2GmJjY9+qFzmGR0l+dXouW+Ezm2H/PGF8pPgqclXKc3MuPfOEtda9Eh8UJdnv/NGQOFDuHBGeNFjyH7UXLTx4htKu/Tu3fa8kfpxvLa7+8XK+KsHbUoJm/soyOQpo9uJmiYHBU+Qiv1mAUa8IjaVu6SaZlvDQxIq/ovdOMJvSkpQLAgMBAAECggEABi9oxv6rL1UHr4S8cuCnv1YmRhBWH06w9DrMlTOPYbx6SLkVaUgBbAGaoWd9q9Zy/T8hd6pKWzWua/fLichsa7ARHYUn2sgtEbSdytGZaAW7Sq5wEmdsWttkGuzKiwGilo9jIb9nRS7YyP8uuqyaqVpZ+12dJan7RFWNG/Shs1cjjk2WhzgIxXqN4UTKMZQD5DBcQmX/4r4Ddixl68KOxnN4gTXEHN0UhCwKPCdHvdnIiFzykHu72EtBCdGfc5RHXv/VD2cFZYlDJ5pVB5MWv3ukiQVAkG4NRZDzq4yadVZ0MbDEmRrzwqkX9/y9XSVXW+1Nii7DFiUlFfs6ibPtSQKBgQDyuzt9KEnOBFcHQN44uBc10opApMKoV9uVoZbwxv/rsLN9iouXAuUbrJqRPNMBhNpWpM/Tf39B9jgNMmfuEznJYFsTU+KuTZsOTjhlNDjQuquamrUoqGDQg5NeH+mhcrl4MYkYuRcC4SrldpcYfu5KhNBXZ1iz03dCbfpnbMn53wKBgQDj8cgNQr9AzkbBpDT9RhD3BhvoIaY6JebGtISbi1D39e6NwwCjQ5vLhDkWBl9zfgq1jhSCGV7mFCtSI0k0diK61uZlm0+7Mldc6LXnpZjEdal20fABL1KuICAfLoaBW8m2m6B6cXsfVvTtLydQI+NZoOt9OkfErBiOg0L0hwsDVQKBgGpwE9wECKkgWhFCLq/sebEOS7WhCgLL0+w/WXLnsF1ntK1+TUvA5zpFa9n4NAbcfOm1h7SUmfcQwu92hQBuyc42RHmrNSF9wlp5jl1Ckw9ka891u67CdwG4UKzbjZVQO2grQJToxOBsYGUSpZsGPfPLXZiWJt1kA03L8BveJos9AoGAVVsrm3OcJItZyZdQ1GrRXX8nIhS/p1ScB1p/sbNInaG1M9aKvZhKlbosmkfGpHvVTMkoetM/Sw7QbhCSkBeQx8BDRFcVUzb1qe/mdhj3jNG2pKzWn8r1vgh/ns2QRo51iXDbdh5aiZDJZKvcn9DgiKaOqDUTvNzo0Szr/J85C4UCgYEA3/lPBNpPfjzeOHaS2eoRS8W5TuPtrQ1rUHBpDD5ixWOYSNeSZqSS4gXZduvND/Dm6kLrGg/e6qBFr4G+CKxcrCibsBbTkkP9nak5DMQJ3EMAQEUucQcVQ/cQ/AXdW/PjQbtbm5/blMcPlo1mxfy3Ggd32rX7y+V0Bw8NgiUGtvA=";

    public static void main(String[] args) {
        ConnectionManager cm = new ConnectionManager();
        JDBCUserManager userMan = new JDBCUserManager(cm, PRIVATE_KEY);

        System.out.println("=== RSA ENCRYPTION TEST ===");

        try {
            // Register test user
            if (userMan.getRole("alice") == null) {
                userMan.register("alice", "mypassword123", "doctor");
                System.out.println("User 'alice' created.");
            }

            // Simulate client encryption
            String encrypted = Encryption.encrypt("mypassword123", PUBLIC_KEY);
            System.out.println("Encrypted password sent by client:\n" + encrypted);

            // Server verifies
            boolean verified = userMan.verifyPassword("alice", encrypted);
            System.out.println("\nPassword verified? " + verified);

            // Print role
            System.out.println("Role of alice: " + userMan.getRole("alice"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cm.close();
            System.out.println("\nDatabase connection closed.");
        }
    }
}
