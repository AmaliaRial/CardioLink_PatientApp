// src/main/java/executable/MenuPatientSwing.java
package executable;

import bitalino.SignalFilePlotter;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Ventana principal del paciente.
 * - UI con flujo de Login/Register/Recording.
 * - Login robusto: usa ensureConnectedRetry, timeout para lectura y manejo de respuestas tolerante.
 * - Start Recording: solicita al servidor abrir DiagnosisFile, recibe id, envía START, simula envío DATA_BLOCK y guarda CSV local para plot.
 * - Stop Recording: envía END y muestra panel de diagnosis.
 */
public class MenuPatientSwing extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private JButton btnLogin;
    private JButton btnRegister;
    private JButton btnRecordBitalino; // campo para permitir habilitar desde login

    private String macAddress = null;

    // Últimos parámetros de conexión
    private String lastHost = null;
    private int lastPort = -1;
    private boolean connectedFlag = false;

    // Usuario logueado
    private String currentUsername = null;

    // Diagnosis actual durante la grabación
    private int currentDiagnosisId = -1;
    private File currentRecordingFile = null;
    private volatile boolean recording = false;
    private SwingWorker<Void, Void> recordingWorker = null;

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
                            boolean ok = false;
                            try { ok = in.readBoolean(); } catch (EOFException ignored) {}
                            String msg = "";
                            try { msg = in.readUTF();
                            } catch (EOFException ignored) {}
                            serverMsg = msg;
                            if (ok) {
                                success = true;
                                currentUsername = username; // fijar usuario logueado
                            } else {
                                success = false;
                            }
                        } else {
                            serverMsg = "Unexpected response: " + statusResp;
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
                        btnRecordBitalino.setEnabled(true);
                        btnLogin.setEnabled(true);
                        btnRegister.setEnabled(false);
                        JOptionPane.showMessageDialog(MenuPatientSwing.this, serverMsg == null ? "Login successful" : serverMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
                        // Navegar a la pantalla con el botón "Record Bitalino Signal"
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

        // SIGN UP
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

            // Validaciones
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

                        // **PROTOCOLO EXACTO: "SIGNUP" + campos en ORDEN ESPECÍFICO**
                        out.writeUTF("SIGNUP");
                        out.writeUTF(username);
                        out.writeUTF(pass);
                        out.writeUTF(name);
                        out.writeUTF(surname);
                        out.writeUTF(birthdayInput); // **FORMATO: dd-MM-yyyy**
                        out.writeUTF(sex);
                        out.writeUTF(email);
                        out.writeUTF(phone);
                        out.writeUTF(dni);
                        out.writeUTF(insurance);
                        out.writeUTF(emergency);
                        out.flush();

                        // **RESPUESTA ESPERADA: "ACK" o "ERROR"**
                        String resp = in.readUTF();
                        if ("ACK".equals(resp)) {
                            ok = true;
                            msg = in.readUTF(); // Leer mensaje adicional
                        } else if ("ERROR".equals(resp)) {
                            msg = in.readUTF();
                            ok = false;
                        } else {
                            msg = "Unexpected response: " + resp;
                            ok = false;
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

        // Bitalino and recording panels

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

        // Acción: intenta abrir el archivo de grabación actual o muestra mensaje
        btnViewDiagnosisFile.addActionListener(e -> {
            if (currentRecordingFile == null || !currentRecordingFile.exists()) {
                JOptionPane.showMessageDialog(this, "Recording file not available", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(currentRecordingFile);
                } else {
                    new SignalFilePlotter(currentRecordingFile.getAbsolutePath());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Cannot open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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

        // START action
        btnStart.addActionListener(e -> {
            if (currentUsername == null) {
                JOptionPane.showMessageDialog(this, "Login as patient first", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] server = askServerHostPortIfNotConnected();
            if (server == null) return;

            btnStart.setEnabled(false);

            new SwingWorker<Boolean, Void>() {
                private String msg = null;
                private boolean ok = false;
                @Override
                protected Boolean doInBackground() {
                    try {
                        ensureConnectedRetry(server[0], Integer.parseInt(server[1]));
                        // request open new diagnosis
                        out.writeUTF("OPEN_NEW_DIAGNOSIS_FILE");
                        out.writeUTF(currentUsername == null ? "" : currentUsername);
                        out.writeUTF(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                        out.flush();

                        String resp = in.readUTF();
                        if ("DIAGNOSIS_OPENED".equals(resp)) {
                            int id = in.readInt();
                            currentDiagnosisId = id;
                            // send START <id>
                            out.writeUTF("START");
                            out.writeInt(currentDiagnosisId);
                            out.flush();

                            // Prepare CSV file local
                            currentRecordingFile = File.createTempFile("recording_diag_" + currentDiagnosisId + "_", ".csv");
                            try (PrintWriter pw = new PrintWriter(new FileWriter(currentRecordingFile))) {
                                pw.println("ECG,EDA");
                            }

                            // start simulated recording
                            recording = true;
                            startSimulatedRecordingWorker();
                            ok = true;
                            msg = "Recording started. Diagnosis id: " + id;
                        } else {
                            if ("ERROR".equals(resp)) {
                                String em = in.readUTF();
                                msg = "Server error: " + em;
                            } else {
                                msg = "Unexpected server response: " + resp;
                            }
                            ok = false;
                        }
                    } catch (IOException ex) {
                        msg = "I/O error: " + ex.getMessage();
                        cleanupResources();
                    }
                    return ok;
                }

                @Override
                protected void done() {
                    btnStart.setEnabled(true);
                    try { boolean res = get(); if (res) { btnStop.setEnabled(true); btnContinueRec.setEnabled(false); cardLayout.show(cards, "bitalinoRecording"); } } catch (Exception ignored) {}
                    JOptionPane.showMessageDialog(MenuPatientSwing.this, msg == null ? "Done" : msg);
                }
            }.execute();
        });

        btnStop.addActionListener(e -> {
            if (!recording) return;
            recording = false;
            btnStop.setEnabled(false);
            btnContinueRec.setEnabled(true);
            if (recordingWorker != null) recordingWorker.cancel(true);
            // send END
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        if (out != null) {
                            out.writeUTF("END");
                            out.flush();
                        }
                    } catch (IOException ignored) {}
                    return null;
                }
                @Override
                protected void done() {
                    // show diagnosis panel
                    SwingUtilities.invokeLater(() -> showDiagnosisPanel(currentDiagnosisId, currentRecordingFile));
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

        // Symptoms selector
        JPanel symptomsSelectorPanel = createSymptomsSelectorPanel(list -> {
            if (currentDiagnosisId <= 0) {
                JOptionPane.showMessageDialog(this, "No diagnosis id available", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // list contains strings like "1 - Pain" => extract ids
            List<String> ids = new ArrayList<>();
            for (String s : list) {
                String[] parts = s.split(" - ");
                ids.add(parts[0].trim());
            }
            String csv = String.join(",", ids);
            sendSymptomsToServer(currentDiagnosisId, csv);
        });

        cards.add(home, "home");
        cards.add(auth, "auth");
        cards.add(login, "login");
        cards.add(new JScrollPane(register), "register");
        cards.add(bitalinoPanel, "bitalino");
        cards.add(bitalinoRecordingPanel, "bitalinoRecording");
        cards.add(symptomsSelectorPanel, "symptomsSelector");
        add(cards, BorderLayout.CENTER);

        btnRecordBitalino.addActionListener(e -> cardLayout.show(cards, "bitalinoRecording"));

        cardLayout.show(cards, "home");
    }

    private void startSimulatedRecordingWorker() {
        recordingWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Random rnd = new Random();
                int blockSize = 10;
                try {
                    while (recording && !isCancelled()) {
                        int frames = blockSize;
                        try {
                            out.writeUTF("DATA_BLOCK");
                            out.writeInt(frames);
                            for (int i = 0; i < frames; i++) {
                                long ts = System.currentTimeMillis();
                                int[] analog = new int[]{500 + rnd.nextInt(200) - 100, 200 + rnd.nextInt(100) - 50};
                                out.writeLong(ts);
                                out.writeInt(analog.length);
                                for (int v : analog) out.writeInt(v);
                                // append CSV
                                try (PrintWriter pw = new PrintWriter(new FileWriter(currentRecordingFile, true))) {
                                    pw.println(analog[0] + "," + analog[1]);
                                } catch (IOException ioe) {
                                    System.err.println("CSV append failed: " + ioe.getMessage());
                                }
                            }
                            out.flush();
                        } catch (IOException e) {
                            System.err.println("I/O sending data block: " + e.getMessage());
                            try {
                                if (lastHost != null && lastPort > 0) {
                                    ensureConnectedRetry(lastHost, lastPort);
                                }
                            } catch (IOException ex) {
                                System.err.println("Reconnect failed: " + ex.getMessage());
                                recording = false;
                                break;
                            }
                        }
                        try { Thread.sleep(200); } catch (InterruptedException ignored) { break; }
                    }
                } finally {
                    try {
                        if (out != null) {
                            out.writeUTF("END");
                            out.flush();
                        }
                    } catch (IOException ignored) {}
                }
                return null;
            }
        };
        recordingWorker.execute();
    }

    // Pedir host/port si no hay conexión; si ya conectado, NO pregunta, reutiliza
    private String[] askServerHostPortIfNotConnected() {
        // Si ya hay conexión establecida, la reutilizamos directamente sin preguntar
        if (socket != null && socket.isConnected() && !socket.isClosed()
                && connectedFlag && lastHost != null && lastPort > 0) {
            return new String[]{ lastHost, String.valueOf(lastPort) };
        }
        // Si no hay conexión todavía, mostramos el diálogo una vez
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
                        // Guardar datos y notificar al usuario
                        lastHost = host;
                        lastPort = port;
                        connectedFlag = true;
                        macAddress = mac.isBlank() ? null : mac;
                        btnLogin.setEnabled(true);
                        btnRegister.setEnabled(true);
                        // Mensaje claro al usuario
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

    // Enviar síntomas con string "1,2,3"
    private void sendSymptomsToServer(int diagnosisId, String csvIds) {
        String[] server = askServerHostPortIfNotConnected();
        if (server == null) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureConnectedRetry(server[0], Integer.parseInt(server[1]));
                    out.writeUTF("SYMPTOMS");
                    out.writeInt(diagnosisId);
                    String[] toks = csvIds == null || csvIds.trim().isEmpty() ? new String[0] : csvIds.split(",");
                    out.writeInt(toks.length);
                    for (String t : toks) {
                        try { out.writeInt(Integer.parseInt(t.trim())); } catch (NumberFormatException ex) { out.writeInt(-1); }
                    }
                    out.writeUTF(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    out.flush();
                    // read best-effort ACK
                    try {
                        socket.setSoTimeout(3000);
                        String resp = in.readUTF();
                        if ("ACK".equals(resp)) {
                            String msg = in.readUTF();
                            JOptionPane.showMessageDialog(MenuPatientSwing.this, "Server: " + msg);
                        }
                    } catch (SocketTimeoutException ignored) {
                    } catch (IOException ignored) {}
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(MenuPatientSwing.this, "Error enviando síntomas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try { if (socket != null) socket.setSoTimeout(0); } catch (SocketException ignored) {}
                }
                return null;
            }
        }.execute();
    }

    // Helpers: validations and UI helpers
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

    // Selector de síntomas con IDs numéricos
    public JPanel createSymptomsSelectorPanel(Consumer<List<String>> onSave) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(171, 191, 234));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Select symptoms (choose IDs)", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        p.add(title, BorderLayout.NORTH);

        String[] symptoms = {
                "1 - Pain",
                "2 - Difficulty holding objects",
                "3 - Trouble breathing",
                "4 - Trouble swallowing",
                "5 - Trouble sleeping",
                "6 - Fatigue"
        };

        JList<String> list = new JList<>(symptoms);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        btns.add(cancel);
        btns.add(save);
        p.add(btns, BorderLayout.SOUTH);

        cancel.addActionListener(e -> cardLayout.show(cards, "bitalinoRecording"));

        save.addActionListener(e -> {
            List<String> sel = list.getSelectedValuesList();
            List<String> ids = new ArrayList<>();
            for (String s : sel) {
                String[] parts = s.split(" - ");
                ids.add(parts[0].trim());
            }
            onSave.accept(ids);
            cardLayout.show(cards, "bitalino");
        });

        return p;
    }

    private void showDiagnosisPanel(int diagnosisId, File csvFile) {
        JFrame diag = new JFrame("Diagnosis " + diagnosisId);
        diag.setSize(1000, 700);
        diag.setLocationRelativeTo(this);

        JPanel main = new JPanel(new BorderLayout());
        JPanel info = new JPanel(new GridLayout(0,1));
        info.setBorder(BorderFactory.createTitledBorder("Diagnosis info"));
        info.add(new JLabel("ID: " + diagnosisId));
        info.add(new JLabel("Patient: " + (currentUsername == null ? "unknown" : currentUsername)));
        info.add(new JLabel("Recording file: " + (csvFile == null ? "n/a" : csvFile.getAbsolutePath())));
        main.add(info, BorderLayout.WEST);

        JButton btnOpenPlot = new JButton("Open Recording Plot");
        btnOpenPlot.addActionListener(e -> {
            if (csvFile == null || !csvFile.exists()) {
                JOptionPane.showMessageDialog(this, "Recording file not available", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new SignalFilePlotter(csvFile.getAbsolutePath());
        });
        main.add(btnOpenPlot, BorderLayout.CENTER);

        JPanel symptoms = new JPanel(new BorderLayout());
        symptoms.setBorder(BorderFactory.createTitledBorder("Symptoms"));
        String[] cols = {"ID", "Description"};
        String[][] data = {
                {"1","Pain"},{"2","Difficulty holding objects"},{"3","Trouble breathing"},
                {"4","Trouble swallowing"},{"5","Trouble sleeping"},{"6","Fatigue"}
        };
        JTable table = new JTable(data, cols);
        symptoms.add(new JScrollPane(table), BorderLayout.CENTER);
        main.add(symptoms, BorderLayout.EAST);

        diag.add(main);
        diag.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPatientSwing().setVisible(true));
    }
}
