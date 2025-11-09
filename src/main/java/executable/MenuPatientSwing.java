package executable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class MenuPatientSwing extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private JButton btnLogin;
    private JButton btnRegister;


    private String macAddress = null;

    // Últimos parámetros de conexión
    private String lastHost = null;
    private int lastPort = -1;
    private boolean connectedFlag = false;

    public MenuPatientSwing() {
        super("App for Patients");
        initUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupResources();
            }
        });

        SwingUtilities.invokeLater(this::showConnectDialog);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 560);
        setLocationRelativeTo(null);
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



        add(topBar, BorderLayout.NORTH);

        // PANTALLA 1: Home
        JPanel home = new JPanel();
        home.setLayout(new BoxLayout(home, BoxLayout.Y_AXIS));
        home.setBackground(new Color(171, 191, 234));
        home.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("App for Patients", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitle = new JLabel("CadioLink", SwingConstants.CENTER);
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

        // PANTALLA 2: Auth
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

        // PANTALLA 3 : LOGIN
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
        btnLoginContinue.addActionListener(e -> {
            String username = loginUsername.getText().trim();
            String pass = String.valueOf(loginPass.getPassword()).trim();

            if (username.isBlank() || pass.isBlank()) {
                JOptionPane.showMessageDialog(this, "Complete all fields", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] server = askServerHostPortIfNotConnected();
            if (server == null) return; // usuario canceló or not connected

            btnLoginContinue.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                private String serverMsg = null;
                @Override
                protected Boolean doInBackground() {
                    try {
                        ensureConnected(server[0], Integer.parseInt(server[1]));
                        out.writeUTF("LOGIN");
                        out.writeUTF(username);
                        out.writeUTF(pass);
                        out.flush();

                        String response = in.readUTF();
                        if ("LOGIN_RESULT".equals(response)) {
                            boolean ok = in.readBoolean();
                            serverMsg = in.readUTF();
                            return ok;
                        } else {
                            serverMsg = "Unexpected response: " + response;
                            return false;
                        }
                    } catch (Exception ex) {
                        serverMsg = "I/O error: " + ex.getMessage();
                        return false;
                    }
                }
                @Override
                protected void done() {
                    btnLoginContinue.setEnabled(true);
                    boolean ok = false;
                    try { ok = get(); } catch (Exception ignored) {}
                    JOptionPane.showMessageDialog(MenuPatientSwing.this, serverMsg == null ? (ok ? "Logged in" : "Login failed") : serverMsg);
                    if (ok) {
                        cardLayout.show(cards, "bitalino");
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

        // PANTALLA 4 : Register
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
        fUsername.setToolTipText("Unique username");
        JTextField fName       = underlineField(18);
        fName.setToolTipText("Name");
        JTextField fSurname    = underlineField(18);
        fSurname.setToolTipText("Surname");
        JPasswordField fPassword = (JPasswordField) underlineField(new JPasswordField(18));
        JTextField fDni        = underlineField(14);

        JTextField fBirthday   = underlineField(10);
        fBirthday.setToolTipText("yyyy-MM-dd");
        fBirthday.setText("yyyy-MM-dd");

        JTextField fEmail      = underlineField(22);
        JTextField fSex        = underlineField(6);
        JTextField fPhone      = underlineField(14);
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
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Birthday (yyyy-MM-dd):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fBirthday, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Email:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fEmail, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Sex (M/F):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fSex, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Phone Number:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fPhone, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Health Insurance number:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fInsurance, r);

        r.gridwidth = 1; r.weightx = 0;
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Emergency Contact:"), r);
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
            fBirthday.setText("yyyy-MM-dd");
            fEmail.setText("");
            fSex.setText("");
            fPhone.setText("");
            fInsurance.setText("");
            fEmergency.setText("");
        });

        regCreate.addActionListener(e -> {
            String username = fUsername.getText().trim();
            String name = fName.getText().trim();
            String surname = fSurname.getText().trim();
            String dni = fDni.getText().trim();
            String pass = String.valueOf(fPassword.getPassword()).trim();
            String birthdayIso = fBirthday.getText().trim();
            String email = fEmail.getText().trim();
            String sex = fSex.getText().trim();
            String phone = fPhone.getText().trim();
            String insurance = fInsurance.getText().trim();
            String emergency = fEmergency.getText().trim();

            if (username.isBlank() || name.isBlank() || surname.isBlank() || dni.isBlank() || pass.isBlank()) {
                JOptionPane.showMessageDialog(this, "Complete required fields", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!isValidPhone(phone) || !isValidPhone(emergency)) {
                JOptionPane.showMessageDialog(this, "Teléfonos inválidos. Deben ser 7-15 dígitos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidIsoDate(birthdayIso)) {
                JOptionPane.showMessageDialog(this, "Fecha de nacimiento inválida. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] server = askServerHostPortIfNotConnected();
            if (server == null) return;

            regCreate.setEnabled(false);
            new SwingWorker<Void, Void>() {
                private String serverMsg = null;
                private boolean success = false;
                @Override
                protected Void doInBackground() {
                    try {
                        ensureConnected(server[0], Integer.parseInt(server[1]));
                        out.writeUTF("SIGNUP");
                        out.writeUTF(username);
                        out.writeUTF(pass);
                        out.writeUTF(name);
                        out.writeUTF(surname);
                        out.writeUTF(birthdayIso);
                        out.writeUTF(sex);
                        out.writeUTF(email);
                        out.writeUTF(phone);
                        out.writeUTF(dni);
                        out.writeUTF(insurance);
                        out.writeUTF(emergency);
                        out.flush();

                        String response = in.readUTF();
                        if ("ACK".equals(response)) {
                            serverMsg = in.readUTF();
                            success = true;
                        } else if ("ERROR".equals(response)) {
                            serverMsg = in.readUTF();
                            success = false;
                        } else {
                            serverMsg = "Unexpected server response: " + response;
                        }
                    } catch (IOException ex) {
                        serverMsg = "I/O error: " + ex.getMessage();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    regCreate.setEnabled(true);
                    JOptionPane.showMessageDialog(MenuPatientSwing.this, serverMsg == null ? (success ? "Registered" : "Failed") : serverMsg);
                    if (success) cardLayout.show(cards, "auth");
                }
            }.execute();
        });

        // PANTALLA 5...7: Bitalino y selector síntomas (simplificado)
        JPanel bitalinoPanel = new JPanel(new GridBagLayout());
        bitalinoPanel.setBackground(new Color(171, 191, 234));
        GridBagConstraints b = new GridBagConstraints();
        b.insets = new Insets(20, 20, 20, 20);
        b.fill = GridBagConstraints.NONE;

        JButton btnRecordBitalino = new JButton("Record Bitalino Signal");
        btnRecordBitalino.setFont(btnRecordBitalino.getFont().deriveFont(Font.BOLD, 24f));
        btnRecordBitalino.setBackground(new Color(182, 118, 45));
        btnRecordBitalino.setForeground(Color.WHITE);
        btnRecordBitalino.setOpaque(true);
        btnRecordBitalino.setBorderPainted(false);
        btnRecordBitalino.setFocusPainted(false);
        btnRecordBitalino.setPreferredSize(new Dimension(420, 80));
        b.gridx = 0; b.gridy = 0; b.weightx = 1.0; b.weighty = 1.0; b.anchor = GridBagConstraints.CENTER;
        bitalinoPanel.add(btnRecordBitalino, b);

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

        btnStart.addActionListener(e -> {
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        });

        btnStop.addActionListener(e -> {
            btnStop.setEnabled(false);
            btnStart.setEnabled(true);
            btnContinueRec.setEnabled(true);
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

        JPanel symptomsSelectorPanel = createSymptomsSelectorPanel(list -> {
            JOptionPane.showMessageDialog(this, "Síntomas guardados: " + String.join(", ", list));
            cardLayout.show(cards, "bitalino");
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

    // Pedir host/port si no hay conexión; si ya conectado devuelve conexión actual
    private String[] askServerHostPortIfNotConnected() {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            if (lastHost != null && lastPort > 0) {
                return new String[]{ lastHost, String.valueOf(lastPort) };
            } else {
                try {
                    return new String[]{ socket.getInetAddress().getHostAddress(), String.valueOf(socket.getPort()) };
                } catch (Exception ignored) {}
            }
        }
        // No hay conexión: mostrar diálogo de conexión y esperar resultado
        showConnectDialog();
        return connectedFlag ? new String[]{ lastHost, String.valueOf(lastPort) } : null;
    }

    /**
     * Muestra el diálogo modal de conexión. El botón Connect valida host/port/mac y
     * realiza la conexión en segundo plano con un SwingWorker. Si la conexión es exitosa
     * se escribirá la identificación "Patient" al servidor y se habilitarán los botones.
     */
    private void showConnectDialog() {
        JDialog dlg = new JDialog(this, "Connect to Server", true);
        dlg.setSize(420, 220);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField txtHost = new JTextField(lastHost == null ? "localhost" : lastHost);
        JTextField txtPort = new JTextField(lastPort <= 0 ? "5000" : String.valueOf(lastPort));
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

        btnCancel.addActionListener(e -> {
            dlg.dispose();
        });

        btnConnect.addActionListener(e -> {
            String host = txtHost.getText().trim();
            String portStr = txtPort.getText().trim();
            String mac = txtMac.getText().trim();

            if (!isValidIPAddress(host)) {
                JOptionPane.showMessageDialog(dlg, "Invalid IP/host", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int port;
            try { port = Integer.parseInt(portStr); }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid port", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            btnConnect.setEnabled(false);
            status.setText("Connecting...");
            new SwingWorker<Void, Void>() {
                private Exception error;
                @Override
                protected Void doInBackground() {
                    try {
                        ensureConnected(host, port);
                        // identify as Patient
                        out.writeUTF("Patient");
                        out.flush();
                        macAddress = mac.isEmpty() ? null : mac;
                        // store last successful params
                        lastHost = host;
                        lastPort = port;
                        connectedFlag = true;
                    } catch (Exception ex) {
                        error = ex;
                        connectedFlag = false;
                    }
                    return null;
                }
                @Override
                protected void done() {
                    btnConnect.setEnabled(true);
                    status.setText(" ");
                    if (error != null) {
                        JOptionPane.showMessageDialog(dlg, "Connection failed: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dlg, "Connected to server", "Info", JOptionPane.INFORMATION_MESSAGE);
                        btnLogin.setEnabled(true);
                        btnRegister.setEnabled(true);
                        dlg.dispose();
                    }
                }
            }.execute();
        });

        dlg.setContentPane(p);
        dlg.setResizable(false);
        dlg.setVisible(true); // modal: blocks until disposed
    }

    private synchronized void ensureConnected(String host, int port) throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) return;
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    private void cleanupResources() {
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
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
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("\\d{7,15}");
    }

    private static boolean isValidIsoDate(String iso) {
        if (iso == null) return false;
        try {
            LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
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

    public JPanel createSymptomsSelectorPanel(Consumer<List<String>> onSave) {
        final String[] ALL = {
                "Chest pain", "Shortness of breath", "Palpitations", "Dizziness",
                "Fatigue", "Nausea", "Sweating", "Syncope", "Cough"
        };
        final java.util.Set<String> selected = new LinkedHashSet<>();
        final java.util.Map<String, JButton> addBtns = new HashMap<>();

        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new javax.swing.border.EmptyBorder(8,8,8,8));

        JLabel title = new JLabel("Add symptoms", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        root.add(title, BorderLayout.NORTH);

        final JPanel selectedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        selectedPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel availablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        for (String s : ALL) {
            JButton b = new JButton("Add");
            b.addActionListener(e -> {
                if (selected.add(s)) {
                    selectedPanel.add(new JLabel(s));
                    selectedPanel.revalidate();
                    selectedPanel.repaint();
                }
            });
            addBtns.put(s, b);
            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
            item.add(new JLabel(s));
            item.add(Box.createHorizontalStrut(8));
            item.add(b);
            availablePanel.add(item);
        }

        JScrollPane scroll = new JScrollPane(availablePanel);
        scroll.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(new JLabel("Selected:"));
        center.add(selectedPanel);
        center.add(Box.createVerticalStrut(8));
        center.add(new JLabel("Available:"));
        center.add(scroll);

        JButton btnReturn = new JButton("Return");
        btnReturn.addActionListener(e -> cardLayout.show(cards, "bitalinoRecording"));

        JButton btnContinue = new JButton("Continue >>");
        btnContinue.addActionListener(e -> {
            onSave.accept(new ArrayList<>(selected));
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        bottom.add(btnReturn);
        bottom.add(btnContinue);

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        return root;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPatientSwing().setVisible(true));
    }
}
