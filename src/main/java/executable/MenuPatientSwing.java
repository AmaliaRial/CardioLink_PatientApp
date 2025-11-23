package executable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;


public class MenuPatientSwing extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private JButton btnLogin;
    private JButton btnRegister;
    private JButton btnRecordBitalino; // UI preserved pero sin lógica de grabación

    private String macAddress = null;

    // Últimos parámetros de conexión
    private String lastHost = null;
    private int lastPort = -1;
    private boolean connectedFlag = false;

    // Usuario logueado
    private String currentUsername = null;

    // Estado de grabación
    private volatile boolean recording = false;
    private volatile boolean stopRequested = false;

    public MenuPatientSwing() {
        super("App for Patients");
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 640);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupResources();
            }
        });

        SwingUtilities.invokeLater(this::showConnectDialog);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Barra superior
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(171, 191, 234));
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JButton btnExit = new JButton("✖");
        btnExit.setToolTipText("Exit");
        btnExit.setForeground(Color.WHITE);
        btnExit.setBackground(new Color(200, 0, 0));
        btnExit.setOpaque(true);
        btnExit.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btnExit.setFocusPainted(false);
        btnExit.setUI(new BasicButtonUI());
        btnExit.addActionListener(e -> {
            cleanupResources();
            System.exit(0);
        });
        topBar.add(btnExit, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Home
        JPanel home = new JPanel();
        home.setLayout(new BoxLayout(home, BoxLayout.Y_AXIS));
        home.setBackground(new Color(171, 191, 234));
        home.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("App for Patients", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitle = new JLabel("CardioLink", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 20f));
        subtitle.setForeground(new Color(80, 80, 80));

        JButton btnContinue = new JButton("Continue >>");
        btnContinue.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnContinue.setFont(btnContinue.getFont().deriveFont(Font.BOLD, 16f));
        btnContinue.setBackground(new Color(11, 87, 147));
        btnContinue.setForeground(Color.WHITE);
        btnContinue.setOpaque(true);
        btnContinue.setBorderPainted(false);
        btnContinue.setFocusPainted(false);
        btnContinue.setUI(new BasicButtonUI());
        btnContinue.addActionListener(e -> cardLayout.show(cards, "auth"));

        home.add(Box.createVerticalGlue());
        home.add(title);
        home.add(Box.createRigidArea(new Dimension(0, 10)));
        home.add(subtitle);
        home.add(Box.createRigidArea(new Dimension(0, 24)));
        home.add(btnContinue);
        home.add(Box.createVerticalGlue());

        // Auth selection
        JPanel auth = new JPanel();
        auth.setLayout(new BoxLayout(auth, BoxLayout.Y_AXIS));
        auth.setBackground(new Color(171, 191, 234));
        auth.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel choose = new JLabel("Choose an option", SwingConstants.CENTER);
        choose.setAlignmentX(Component.CENTER_ALIGNMENT);
        choose.setFont(choose.getFont().deriveFont(Font.BOLD, 22f));

        btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 15f));
        btnLogin.setBackground(new Color(205, 103, 106));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setUI(new BasicButtonUI());
        btnLogin.addActionListener(e -> cardLayout.show(cards, "login"));

        btnRegister = new JButton("Register");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setFont(btnRegister.getFont().deriveFont(Font.BOLD, 15f));
        btnRegister.setBackground(new Color(221, 14, 96));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setOpaque(true);
        btnRegister.setBorderPainted(false);
        btnRegister.setFocusPainted(false);
        btnRegister.setUI(new BasicButtonUI());
        btnRegister.addActionListener(e -> cardLayout.show(cards, "register"));

        JButton btnBackHome = new JButton("Return");
        btnBackHome.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBackHome.addActionListener(e -> cardLayout.show(cards, "home"));

        // Initially disabled until connected
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);

        auth.add(Box.createVerticalGlue());
        auth.add(choose);
        auth.add(Box.createRigidArea(new Dimension(0, 18)));
        auth.add(btnLogin);
        auth.add(Box.createRigidArea(new Dimension(0, 10)));
        auth.add(btnRegister);
        auth.add(Box.createRigidArea(new Dimension(0, 18)));
        auth.add(btnBackHome);
        auth.add(Box.createVerticalGlue());

        // LOGIN panel
        JPanel login = new JPanel(new GridBagLayout());
        login.setBackground(new Color(171, 191, 234));
        login.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginTitle = new JLabel("LOG IN", SwingConstants.CENTER);
        loginTitle.setFont(loginTitle.getFont().deriveFont(Font.BOLD, 24f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 3; g.anchor = GridBagConstraints.CENTER;
        login.add(loginTitle, g);

        JTextField loginUsername = underlineField(18);
        JPasswordField loginPass = (JPasswordField) underlineField(new JPasswordField(18));

        g.gridwidth = 1; g.anchor = GridBagConstraints.WEST; g.weightx = 0;
        g.gridx = 0; g.gridy = 1; login.add(new JLabel("Username:"), g);
        g.gridx = 1; g.gridy = 1; g.weightx = 1.0; login.add(loginUsername, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 0; login.add(new JLabel("Password:"), g);
        g.gridx = 1; g.gridy = 2; g.weightx = 1.0; login.add(loginPass, g);

        JButton btnLoginContinue = new JButton("Continue");
        btnLoginContinue.setBackground(new Color(11, 87, 147));
        btnLoginContinue.setForeground(Color.WHITE);
        btnLoginContinue.setOpaque(true);
        btnLoginContinue.setBorderPainted(false);
        btnLoginContinue.setFocusPainted(false);
        btnLoginContinue.setUI(new BasicButtonUI());

        // LOGIN handler
        btnLoginContinue.addActionListener(e -> {
            String username = loginUsername.getText().trim();
            String pass = String.valueOf(loginPass.getPassword()).trim();

            if (username.isBlank() || pass.isBlank()) {
                JOptionPane.showMessageDialog(this, "Complete all fields", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] server = askServerHostPortIfNotConnected();
            if (server == null) return;

            btnLoginContinue.setEnabled(false);
            new SwingWorker<Void, Void>() {
                private String serverMsg = null;
                private boolean success = false;

                @Override
                protected Void doInBackground() {
                    try {
                        ensureConnectedRetry(server[0], Integer.parseInt(server[1]));
                        socket.setSoTimeout(5000);

                        // **PROTOCOLO EXACTO: "LOGIN" + username + password**
                        out.writeUTF("LOGIN");
                        out.writeUTF(username);
                        out.writeUTF(pass);
                        out.flush();

                        // **RESPUESTA ESPERADA: "LOGIN_RESULT" + boolean + mensaje**
                        String statusResp = in.readUTF();

                        if ("LOGIN_RESULT".equals(statusResp)) {
                            // lectura simplificada para mantener compilación
                            boolean ok = in.readBoolean();
                            serverMsg = in.readUTF();
                            success = ok;
                            if (success) currentUsername = username;
                        } else {
                            serverMsg = "Respuesta inesperada del servidor";
                        }

                    } catch (SocketTimeoutException ste) {
                        serverMsg = "Timeout al comunicarse con el servidor.";
                    } catch (EOFException eof) {
                        serverMsg = "Conexión cerrada por el servidor.";
                        cleanupResources();
                    } catch (IOException ex) {
                        serverMsg = "Error I/O: " + ex.getMessage();
                        cleanupResources();
                    } finally {
                        try { if (socket != null) socket.setSoTimeout(0); } catch (Exception ignored) {}
                    }
                    return null;
                }

                @Override
                protected void done() {
                    btnLoginContinue.setEnabled(true);
                    if (success) {
                        if (btnRecordBitalino != null) btnRecordBitalino.setEnabled(true);
                        btnLogin.setEnabled(true);
                        btnRegister.setEnabled(false);
                        JOptionPane.showMessageDialog(MenuPatientSwing.this, serverMsg == null ? "Login successful" : serverMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
                        cardLayout.show(cards, "bitalino");
                    } else {
                        JOptionPane.showMessageDialog(MenuPatientSwing.this, "Login failed: " + (serverMsg == null ? "unknown" : serverMsg), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        g.gridx = 2; g.gridy = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        login.add(btnLoginContinue, g);

        JLabel dont = new JLabel("Don’t have an account?", SwingConstants.CENTER);
        dont.setForeground(new Color(172, 87, 87));
        g.gridx = 0; g.gridy = 3; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        login.add(dont, g);

        JButton goCreate = new JButton("Create an account");
        goCreate.setFocusPainted(false);
        goCreate.addActionListener(e -> cardLayout.show(cards, "register"));
        g.gridy = 4; login.add(goCreate, g);

        JButton loginReturn = new JButton("Return");
        loginReturn.addActionListener(e -> cardLayout.show(cards, "auth"));
        g.gridy = 5; login.add(loginReturn, g);

        // REGISTER panel
        JPanel register = new JPanel(new GridBagLayout());
        register.setBackground(new Color(171, 191, 234));
        register.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(6, 8, 6, 8);
        r.fill = GridBagConstraints.HORIZONTAL;

        JLabel regTitle = new JLabel("SIGN UP AS A PATIENT", SwingConstants.CENTER);
        regTitle.setFont(regTitle.getFont().deriveFont(Font.BOLD, 22f));
        r.gridx = 0; r.gridy = 0; r.gridwidth = 6; r.anchor = GridBagConstraints.CENTER;
        register.add(regTitle, r);

        JTextField fUsername   = underlineField(18);
        JPasswordField fPassword = (JPasswordField) underlineField(new JPasswordField(18));
        JTextField fName       = underlineField(18);
        JTextField fSurname    = underlineField(18);
        JTextField fBirthday   = underlineField(10);
        fBirthday.setToolTipText("dd-MM-yyyy (ej: 31-12-1990)");
        fBirthday.setText("dd-MM-yyyy");
        JTextField fSex        = underlineField(6);
        fSex.setToolTipText("MALE o FEMALE");
        JTextField fEmail      = underlineField(22);
        JTextField fPhone      = underlineField(14);
        JTextField fDni        = underlineField(14);
        JTextField fInsurance  = underlineField(20);
        JTextField fEmergency  = underlineField(14);

        int row = 1;
        r.gridwidth = 1; r.anchor = GridBagConstraints.WEST; r.weightx = 0;

        r.gridx = 0; r.gridy = row; register.add(new JLabel("Username:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fUsername, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Name:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fName, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Surname:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fSurname, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Password:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fPassword, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("DNI:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fDni, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Birthday (yyyy-MM-dd or dd/MM/yyyy):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fBirthday, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Email:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fEmail, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Sex (MALE/FEMALE):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fSex, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Phone Number (7-9 digits):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fPhone, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Health Insurance number (digits up to 10):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fInsurance, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Emergency Contact (7-9 digits):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fEmergency, r);

        JButton regCancel = new JButton("Cancel");
        JButton regCreate = new JButton("Create Account");
        regCreate.setBackground(new Color(17, 49, 85));
        regCreate.setForeground(Color.WHITE);
        regCreate.setOpaque(true);
        regCreate.setBorderPainted(false);
        regCreate.setFocusPainted(false);

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.add(regCancel, BorderLayout.WEST);
        btnRow.add(regCreate, BorderLayout.EAST);

        r.gridx = 0; r.gridy = row; r.gridwidth = 6; r.weightx = 1; r.fill = GridBagConstraints.HORIZONTAL;
        register.add(btnRow, r);

        JButton regReturn = new JButton("Return");
        regReturn.addActionListener(e -> cardLayout.show(cards, "auth"));
        r.gridy = ++row; register.add(regReturn, r);

        regCancel.addActionListener(e -> {
            fUsername.setText("");
            fName.setText("");
            fSurname.setText("");
            fPassword.setText("");
            fDni.setText("");
            fBirthday.setText("dd-mm-yyyy");
            fEmail.setText("");
            fSex.setText("");
            fPhone.setText("");
            fInsurance.setText("");
            fEmergency.setText("");
        });

        // SIGN UP (simplificado para compilar)
        regCreate.addActionListener(e -> {
            String username = fUsername.getText().trim();
            String name = fName.getText().trim();
            String surname = fSurname.getText().trim();
            String dni = fDni.getText().trim().replaceAll("[\\s-]", "").toUpperCase();
            String pass = String.valueOf(fPassword.getPassword()).trim();
            String birthdayInput = fBirthday.getText().trim();
            String email = fEmail.getText().trim();
            String sex = fSex.getText().trim().toUpperCase();
            String phone = fPhone.getText().trim();
            String insurance = fInsurance.getText().trim();
            String emergency = fEmergency.getText().trim();

            if (username.isBlank() || name.isBlank() || surname.isBlank() || dni.isBlank() || pass.isBlank() ||
                    birthdayInput.isBlank() || email.isBlank() || sex.isBlank() || phone.isBlank() ||
                    insurance.isBlank() || emergency.isBlank()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (pass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!validateDNI(dni)) {
                JOptionPane.showMessageDialog(this, "Invalid DNI format. Expected 8 digits + letter (ej: 12345678A)", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!isValidPhone(phone) || !isValidPhone(emergency)) {
                JOptionPane.showMessageDialog(this, "Invalid phone format (7-15 digits)", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!birthdayInput.matches("\\d{2}-\\d{2}-\\d{4}")) {
                JOptionPane.showMessageDialog(this, "Birthday must be in dd-MM-yyyy format", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sex.equals("MALE") && !sex.equals("FEMALE")) {
                JOptionPane.showMessageDialog(this, "Sex must be MALE or FEMALE", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] server = askServerHostPortIfNotConnected();
            if (server == null) return;

            regCreate.setEnabled(false);
            new SwingWorker<Void, Void>() {
                private String msg = null;
                private boolean ok = false;

                @Override
                protected Void doInBackground() {
                    try {
                        ensureConnectedRetry(server[0], Integer.parseInt(server[1]));
                        out.writeUTF("SIGNUP");
                        out.writeUTF(username);
                        out.writeUTF(pass);
                        out.writeUTF(name);
                        out.writeUTF(surname);
                        out.writeUTF(birthdayInput);
                        out.writeUTF(sex);
                        out.writeUTF(email);
                        out.writeUTF(phone);
                        out.writeUTF(dni);
                        out.writeUTF(insurance);
                        out.writeUTF(emergency);
                        out.flush();

                        String resp = in.readUTF();
                        if ("ACK".equals(resp)) {
                            ok = true;
                            msg = "Account created";
                        } else {
                            msg = "Server error: " + resp;
                        }
                    } catch (IOException ex) {
                        msg = "I/O error: " + ex.getMessage();
                        cleanupResources();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    regCreate.setEnabled(true);
                    if (ok) {
                        JOptionPane.showMessageDialog(MenuPatientSwing.this, msg != null ? msg : "Account created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        cardLayout.show(cards, "login");
                    } else {
                        JOptionPane.showMessageDialog(MenuPatientSwing.this, "Registration failed: " + (msg != null ? msg : "Unknown error"), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        // Bitalino and recording panels (UI preserved, logic implemented here)

        JPanel bitalinoPanel = new JPanel(new GridBagLayout());
        bitalinoPanel.setBackground(new Color(171, 191, 234));
        GridBagConstraints b = new GridBagConstraints();
        b.insets = new Insets(20, 20, 20, 20);
        b.fill = GridBagConstraints.NONE;

        btnRecordBitalino = new JButton("Record Bitalino Signal");
        btnRecordBitalino.setFont(btnRecordBitalino.getFont().deriveFont(Font.BOLD, 24f));
        btnRecordBitalino.setBackground(new Color(182, 118, 45));
        btnRecordBitalino.setForeground(Color.WHITE);
        btnRecordBitalino.setOpaque(true);
        btnRecordBitalino.setBorderPainted(false);
        btnRecordBitalino.setFocusPainted(false);
        btnRecordBitalino.setPreferredSize(new Dimension(420, 80));
        b.gridx = 0; b.gridy = 0; b.weightx = 1.0; b.weighty = 1.0; b.anchor = GridBagConstraints.CENTER;
        bitalinoPanel.add(btnRecordBitalino, b);

        // Botón añadido: "View Diagnosis File" a la derecha con mismas propiedades visuales
        JButton btnViewDiagnosisFile = new JButton("View Diagnosis File");
        btnViewDiagnosisFile.setFont(btnRecordBitalino.getFont().deriveFont(Font.BOLD, 24f));
        btnViewDiagnosisFile.setBackground(new Color(182, 118, 45));
        btnViewDiagnosisFile.setForeground(Color.WHITE);
        btnViewDiagnosisFile.setOpaque(true);
        btnViewDiagnosisFile.setBorderPainted(false);
        btnViewDiagnosisFile.setFocusPainted(false);
        btnViewDiagnosisFile.setPreferredSize(new Dimension(420, 80));
        b.gridx = 1; b.gridy = 0; b.weightx = 1.0; b.weighty = 1.0; b.anchor = GridBagConstraints.CENTER;
        bitalinoPanel.add(btnViewDiagnosisFile, b);

        // Acción: ahora solo informa que la funcionalidad fue eliminada
        btnViewDiagnosisFile.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "La visualización del fichero de diagnóstico ha sido eliminada.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnBitalinoReturn = new JButton("Return");
        btnBitalinoReturn.setFocusPainted(false);
        btnBitalinoReturn.addActionListener(e -> cardLayout.show(cards, "auth"));
        b.gridx = 0; b.gridy = 1; b.weightx = 0; b.weighty = 0; b.anchor = GridBagConstraints.SOUTH;
        bitalinoPanel.add(btnBitalinoReturn, b);

        JPanel bitalinoRecordingPanel = new JPanel(new GridBagLayout());
        bitalinoRecordingPanel.setBackground(new Color(171, 191, 234));
        GridBagConstraints br = new GridBagConstraints();
        br.insets = new Insets(20, 20, 20, 20);
        br.fill = GridBagConstraints.NONE;

        JButton btnStart = new JButton("▶️ Start Recording");
        btnStart.setFont(btnStart.getFont().deriveFont(Font.BOLD, 25f));
        btnStart.setBackground(new Color(46, 204, 113));
        btnStart.setForeground(Color.WHITE);
        btnStart.setOpaque(true);
        btnStart.setBorderPainted(false);
        btnStart.setFocusPainted(false);
        btnStart.setPreferredSize(new Dimension(320, 80));

        JButton btnStop = new JButton("⏹️ Stop Recording");
        btnStop.setFont(btnStop.getFont().deriveFont(Font.BOLD, 25f));
        btnStop.setBackground(new Color(231, 76, 60));
        btnStop.setForeground(Color.WHITE);
        btnStop.setOpaque(true);
        btnStop.setBorderPainted(false);
        btnStop.setFocusPainted(false);
        btnStop.setPreferredSize(new Dimension(320, 80));
        btnStop.setEnabled(false);

        JButton btnContinueRec = new JButton("Continue >>");
        btnContinueRec.setBackground(new Color(11, 87, 147));
        btnContinueRec.setForeground(Color.BLACK);
        btnContinueRec.setOpaque(true);
        btnContinueRec.setBorderPainted(false);
        btnContinueRec.setFocusPainted(false);
        btnContinueRec.setEnabled(false);

        JButton btnReturnRec = new JButton("Return");
        btnReturnRec.addActionListener(e -> cardLayout.show(cards, "bitalino"));

        // New recording logic: Start button
        btnStart.addActionListener(e -> {
            if (!connectedFlag || out == null || in == null) {
                JOptionPane.showMessageDialog(this, "No conectado al servidor", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            stopRequested = false;

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        // 1) START
                        if (!startRecording(out)) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MenuPatientSwing.this, "Error enviando START", "Error", JOptionPane.ERROR_MESSAGE));
                            return null;
                        }
                        // 2) READY_TO_RECORD
                        if (!readyToRecord(in)) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MenuPatientSwing.this, "Servidor no listo para grabar", "Error", JOptionPane.ERROR_MESSAGE));
                            return null;
                        }
                        recording = true;

                        // 3) Enviar fragmentos periódicamente hasta que se pida stop
                        int fragIdx = 0;
                        while (!stopRequested) {
                            String fragment = "fragment_data_" + fragIdx++; // sustituir por datos reales si procede
                            sendFragmentsOfRecording(fragment, out);
                            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                        }
                    } finally {
                        recording = false;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                }
            }.execute();
        });

        // Stop button logic
        btnStop.addActionListener(e -> {
            if (!connectedFlag || out == null || in == null) {
                JOptionPane.showMessageDialog(this, "No conectado al servidor", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            stopRequested = true;
            btnStop.setEnabled(false);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    // 1) STOP
                    if (!stopRecording(out)) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MenuPatientSwing.this, "Error enviando STOP", "Error", JOptionPane.ERROR_MESSAGE));
                        return null;
                    }
                    // 2) RECORDING_STOP
                    if (!RecordingStop(in)) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MenuPatientSwing.this, "No se confirmó STOP por el servidor", "Error", JOptionPane.ERROR_MESSAGE));
                        return null;
                    }

                    // 3) Esperar/leer comando SELECT_SYMPTOMS del servidor
                    if (!SelectSymptoms(in)) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MenuPatientSwing.this, "Servidor no solicitó selección de síntomas", "Info", JOptionPane.INFORMATION_MESSAGE));
                        return null;
                    }

                    // 4) Mostrar diálogo de selección de síntomas en EDT y obtener CSV
                    String csv = getSymptomsFromUser();
                    if (csv == null) {
                        // usuario canceló
                        return null;
                    }

                    // 5) Enviar síntomas usando el helper existente
                    try (Scanner sc = new Scanner(csv)) {
                        sendSymptoms(sc, out, in);
                    }

                    // 6) Comprobar recepción
                    if (isSymptomsReceived(in)) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MenuPatientSwing.this, "Síntomas enviados y confirmados por el servidor", "Info", JOptionPane.INFORMATION_MESSAGE));
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(MenuPatientSwing.this, "Servidor no confirmó la recepción de síntomas", "Error", JOptionPane.ERROR_MESSAGE));
                    }

                    return null;
                }

                @Override
                protected void done() {
                    // volver a vista o actualizar estado
                    cardLayout.show(cards, "symptomsSelector");
                    btnStart.setEnabled(true);
                    btnStop.setEnabled(false);
                }
            }.execute();
        });

        btnContinueRec.addActionListener(e -> cardLayout.show(cards, "symptomsSelector"));
        btnReturnRec.addActionListener(e -> cardLayout.show(cards, "bitalino"));

        br.gridx = 0; br.gridy = 0;
        bitalinoRecordingPanel.add(btnStart, br);
        br.gridx = 1; br.gridy = 0;
        bitalinoRecordingPanel.add(btnStop, br);
        br.gridx = 0; br.gridy = 1; br.gridwidth = 2;
        bitalinoRecordingPanel.add(btnContinueRec, br);
        br.gridx = 0; br.gridy = 2; br.gridwidth = 2;
        bitalinoRecordingPanel.add(btnReturnRec, br);

        // Placeholder para selector de síntomas (UI preservada)
        JPanel symptomsSelectorPlaceholder = new JPanel(new BorderLayout());
        symptomsSelectorPlaceholder.setBackground(new Color(171, 191, 234));
        symptomsSelectorPlaceholder.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel symTitle = new JLabel("Select symptoms (functionality present)", SwingConstants.CENTER);
        symTitle.setFont(symTitle.getFont().deriveFont(Font.BOLD, 18f));
        symptomsSelectorPlaceholder.add(symTitle, BorderLayout.NORTH);
        JTextArea info = new JTextArea("Seleccione síntomas tras finalizar la grabación. El cliente enviará la selección al servidor.");
        info.setEditable(false);
        info.setBackground(new Color(171, 191, 234));
        symptomsSelectorPlaceholder.add(new JScrollPane(info), BorderLayout.CENTER);
        JPanel symBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton symBack = new JButton("Back");
        symBack.addActionListener(e -> cardLayout.show(cards, "bitalino"));
        symBtns.add(symBack);
        symptomsSelectorPlaceholder.add(symBtns, BorderLayout.SOUTH);

        cards.add(home, "home");
        cards.add(auth, "auth");
        cards.add(login, "login");
        cards.add(new JScrollPane(register), "register");
        cards.add(bitalinoPanel, "bitalino");
        cards.add(bitalinoRecordingPanel, "bitalinoRecording");
        cards.add(symptomsSelectorPlaceholder, "symptomsSelector");
        add(cards, BorderLayout.CENTER);

        btnRecordBitalino.addActionListener(e -> cardLayout.show(cards, "bitalinoRecording"));

        cardLayout.show(cards, "home");
    }

    // Pedir host/port si no hay conexión; si ya conectado, NO pregunta, reutiliza
    private String[] askServerHostPortIfNotConnected() {
        if (socket != null && socket.isConnected() && !socket.isClosed()
                && connectedFlag && lastHost != null && lastPort > 0) {
            return new String[]{ lastHost, String.valueOf(lastPort) };
        }
        showConnectDialog();
        return connectedFlag ? new String[]{ lastHost, String.valueOf(lastPort) } : null;
    }

    /**
     * Diálogo modal de conexión, con MAC opcional e identificación "Patient".
     */
    private void showConnectDialog() {
        JDialog dlg = new JDialog(this, "Connect to Server", true);
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField txtHost = new JTextField(lastHost == null ? "localhost" : lastHost);
        JTextField txtPort = new JTextField(lastPort <= 0 ? "9000" : String.valueOf(lastPort));
        JTextField txtMac = new JTextField(macAddress == null ? "" : macAddress);

        p.add(new JLabel("Server IP or hostname:"));
        p.add(txtHost);
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(new JLabel("Port:"));
        p.add(txtPort);
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(new JLabel("MAC address (optional for Bitalino):"));
        p.add(txtMac);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnConnect = new JButton("Connect");
        JButton btnCancel = new JButton("Cancel");
        btnPanel.add(btnCancel);
        btnPanel.add(btnConnect);
        p.add(Box.createRigidArea(new Dimension(0, 8)));
        p.add(btnPanel);

        final JLabel status = new JLabel(" ");
        status.setForeground(Color.DARK_GRAY);
        p.add(status);

        btnCancel.addActionListener(e -> dlg.dispose());

        btnConnect.addActionListener(e -> {
            String host = txtHost.getText().trim();
            String portStr = txtPort.getText().trim();
            String mac = txtMac.getText().trim();
            if (!isValidIPAddress(host)) {
                JOptionPane.showMessageDialog(dlg, "Host inválido", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int port;
            try { port = Integer.parseInt(portStr); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Puerto inválido", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            btnConnect.setEnabled(false);
            status.setText("Connecting...");
            new SwingWorker<Void, Void>() {
                private String msg = null;
                private boolean ok = false;
                @Override
                protected Void doInBackground() {
                    try {
                        ensureConnectedRetry(host, port);
                        ok = true;
                        msg = "Connected to server";
                    } catch (IOException ex) {
                        msg = "Connect failed: " + ex.getMessage();
                        cleanupResources();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    btnConnect.setEnabled(true);
                    status.setText(msg);
                    if (ok) {
                        lastHost = host;
                        lastPort = port;
                        connectedFlag = true;
                        macAddress = mac.isBlank() ? null : mac;
                        btnLogin.setEnabled(true);
                        btnRegister.setEnabled(true);
                        JOptionPane.showMessageDialog(dlg, "¡Conectado!", "Info", JOptionPane.INFORMATION_MESSAGE);
                        dlg.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dlg, msg, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        dlg.setContentPane(p);
        dlg.setResizable(false);
        dlg.setVisible(true); // modal
    }

    private synchronized void ensureConnected(String host, int port) throws IOException {
        if (connectedFlag && socket != null && socket.isConnected() && !socket.isClosed()) return;
        cleanupResources();
        socket = new Socket(host, port);
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        in  = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        // Handshake: anunciar que somos un paciente
        out.writeUTF("Patient");
        out.flush();

        lastHost = host;
        lastPort = port;
        connectedFlag = true;
    }

    // Retry wrapper
    private synchronized void ensureConnectedRetry(String host, int port) throws IOException {
        try {
            ensureConnected(host, port);
        } catch (IOException firstEx) {
            cleanupResources();
            socket = new Socket(host, port);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // Handshake también en el reintento
            out.writeUTF("Patient");
            out.flush();

            lastHost = host;
            lastPort = port;
            connectedFlag = true;
        }
    }

    private void cleanupResources() {
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        out = null;
        in = null;
        socket = null;
        connectedFlag = false;
    }

    private static boolean isValidIPAddress(String ip) {
        if (ip == null) return false;
        if (ip.equalsIgnoreCase("localhost")) return true;
        String ipv4 = "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.|$)){4}$";
        String hostname = "^[a-zA-Z0-9.-]+$";
        return Pattern.matches(ipv4, ip) || Pattern.matches(hostname, ip);
    }

    private static boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("\\d{7,15}");
    }

    private static boolean isValidPhoneForDb(String phone) {
        if (phone == null) return false;
        if (!phone.matches("\\d{7,9}")) return false;
        return fitsInInt(phone);
    }

    private static boolean isValidInsuranceForDb(String insurance) {
        if (insurance == null) return false;
        if (!insurance.matches("\\d{1,10}")) return false;
        return fitsInInt(insurance);
    }

    private static boolean fitsInInt(String s) {
        if (s == null) return false;
        s = s.trim();
        if (!s.matches("\\d+")) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean validateDNI(String dni) {
        if (dni == null) return false;
        return dni.matches("\\d{8}[A-Z]");
    }

    // Helper: acepta yyyy-MM-dd o dd/MM/yyyy; devuelve yyyy-MM-dd
    private static String formatToJdbcDate(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String s = input.trim();
        try {
            LocalDate ld = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
            return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {}
        try {
            DateTimeFormatter alt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate ld = LocalDate.parse(s, alt);
            return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {}
        return null;
    }

    private static JTextField underlineField(int columns) {
        JTextField f = new JTextField(columns);
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0,0,0,90)));
        f.setOpaque(false);
        return f;
    }

    private static JComponent underlineField(JComponent f) {
        f.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0,0,0,90)));
        f.setOpaque(false);
        return f;
    }

    // Recorging lifecycle
//"START"
    private static boolean startRecording(DataOutputStream out){
        if( out == null) return false;
        try {
            out.writeUTF("START");
            out.flush();
            return true;


        } catch (IOException e) {
            System.err.println("I/O error during START: " + e.getMessage());
            return false;
        }
    }
    private static boolean readyToRecord(DataInputStream in) {
        if (in == null) return false;
        try {
            String response = in.readUTF();
            return "READY_TO_RECORD".equals(response);
        } catch (IOException e) {
            System.err.println("I/O error during READY_TO_RECORD: " + e.getMessage());
            return false;
        }
    }




    // SEND FRAGMENT OF RECORDING
    public static void sendFragmentsOfRecording(String dataString, DataOutputStream out) {
        try {
            // 1. Mandar comando
            out.writeUTF("SEND_FRAGMENTS_OF_RECORDING");


            // 2. Mandar datos
            out.writeUTF(dataString);


            out.flush(); // aseguramos envío


        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private static boolean stopRecording(DataOutputStream outputStream){
        if( outputStream == null) return false;
        try {
            outputStream.writeUTF("STOP");
            outputStream.flush();
            return true;


        } catch (IOException e) {
            System.err.println("I/O error during STOP: " + e.getMessage());
            return false;
        }
    }
    private static boolean RecordingStop(DataInputStream in) {
        if (in == null) return false;
        try {
            String response = in.readUTF();
            return "RECORDING_STOP".equals(response);
        } catch (IOException e) {
            System.err.println("I/O error during RECORDING_STOP: " + e.getMessage());
            return false;
        }
    }
    private static boolean SelectSymptoms (DataInputStream in) {
        if (in == null) return false;
        try {
            String response = in.readUTF();
            return "SELECT_SYMPTOMS".equals(response);
        } catch (IOException e) {
            System.err.println("I/O error during SELECT_SYMPTOMS: " + e.getMessage());
            return false;
        }
    }




    private static void sendSymptoms(Scanner scanner,
                                     DataOutputStream out,
                                     DataInputStream in) {
        try {
            System.out.println("Pain,Difficulty holding objects,trouble breathing,Trouble swallowing,Trouble sleeping,Fatigue");
            String line = scanner.nextLine().trim();
            if (line == null) line = "";


            out.writeUTF("SYMPTOMS");
            out.writeUTF(line); // enviamos la lista de síntomas como una única cadena CSV
            out.flush();


        } catch (IOException e) {
            System.err.println("I/O error sending symptoms: " + e.getMessage());
        }
    }


    private static boolean isSymptomsReceived (DataInputStream in) {
        if (in == null) return false;
        try {
            String response = in.readUTF();
            return "SYMPTOMS_RECEIVED".equals(response);
        } catch (IOException e) {
            System.err.println("I/O error during SYMPTOMS_RECEIVED: " + e.getMessage());
            return false;
        }
    }


    private String getSymptomsFromUser() {
        final String[] result = new String[1];
        result[0] = null;
        try {
            SwingUtilities.invokeAndWait(() -> {
                JCheckBox c1 = new JCheckBox("Pain");
                JCheckBox c2 = new JCheckBox("Difficulty holding objects");
                JCheckBox c3 = new JCheckBox("trouble breathing");
                JCheckBox c4 = new JCheckBox("Trouble swallowing");
                JCheckBox c5 = new JCheckBox("Trouble sleeping");
                JCheckBox c6 = new JCheckBox("Fatigue");

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(new JLabel("Seleccione los síntomas:"));
                panel.add(c1);
                panel.add(c2);
                panel.add(c3);
                panel.add(c4);
                panel.add(c5);
                panel.add(c6);

                int ok = JOptionPane.showConfirmDialog(MenuPatientSwing.this, panel, "Select Symptoms", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ok == JOptionPane.OK_OPTION) {
                    List<String> chosen = new ArrayList<>();
                    if (c1.isSelected()) chosen.add("Pain");
                    if (c2.isSelected()) chosen.add("Difficulty holding objects");
                    if (c3.isSelected()) chosen.add("trouble breathing");
                    if (c4.isSelected()) chosen.add("Trouble swallowing");
                    if (c5.isSelected()) chosen.add("Trouble sleeping");
                    if (c6.isSelected()) chosen.add("Fatigue");
                    result[0] = String.join(",", chosen);
                } else {
                    result[0] = null;
                }
            });
        } catch (Exception ex) {
            result[0] = null;
        }
        return result[0];
    }
    // DiagnosisFile lifecycle

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPatientSwing().setVisible(true));
    }
}
