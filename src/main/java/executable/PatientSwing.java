package executable;

import bitalino.BITalinoException;
import bitalino.BitalinoManager;
import pojos.Patient;

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


public class PatientSwing extends JFrame {
    BitalinoManager bitalinoManager = new BitalinoManager();

    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private Patient patient;

    // Estados/paneles
    private static final String HOME_PANEL = "Home Panel";
    private static final String AUTH_PANEL = "Auth Panel";
    private static final String LOGIN_PANEL = "Login Panel";
    private static final String REGISTER_PANEL = "Register Panel";
    private static final String BITALINO_PANEL = "Bitalino Panel";
    private static final String BITALINO_RECORDING_PANEL = "Bitalino Recording Panel";
    private static final String SYMPTOMS_SELECTOR_PANEL = "Symptoms Selector Panel";

    private String currentState = "HOME";

    // Paneles como atributos de clase
    private HomePanel homePanel;
    private AuthPanel authPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private BitalinoPanel bitalinoPanel;
    private BitalinoRecordingPanel bitalinoRecordingPanel;
    private SymptomsSelectorPanel symptomsSelectorPanel;

    // Conexión y datos
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private String macAddress = null;
    private String lastHost = null;
    private int lastPort = -1;
    private boolean connectedFlag = false;
    private String currentUsername = null;

    private volatile boolean recording = false;
    private volatile boolean stopRequested = false;

    public PatientSwing() {
        super("App for Patients");
        initializeUI();
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

    private void initializeUI() {
        // Configuración del CardLayout
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Inicializamos los paneles
        homePanel = new HomePanel();
        authPanel = new AuthPanel();
        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();
        bitalinoPanel = new BitalinoPanel();
        bitalinoRecordingPanel = new BitalinoRecordingPanel();
        symptomsSelectorPanel = new SymptomsSelectorPanel();

        // Añadimos los paneles al panel principal
        cardsPanel.add(homePanel, HOME_PANEL);
        cardsPanel.add(authPanel, AUTH_PANEL);
        cardsPanel.add(loginPanel, LOGIN_PANEL);
        cardsPanel.add(new JScrollPane(registerPanel), REGISTER_PANEL);
        cardsPanel.add(bitalinoPanel, BITALINO_PANEL);
        cardsPanel.add(bitalinoRecordingPanel, BITALINO_RECORDING_PANEL);
        cardsPanel.add(symptomsSelectorPanel, SYMPTOMS_SELECTOR_PANEL);

        setLayout(new BorderLayout());
        add(buildTopBar(), BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);

        // Estado inicial
        changeState("HOME");
    }

    // Método para cambiar entre paneles según el estado
    public void showPanel(String panelName) {
        cardLayout.show(cardsPanel, panelName);
    }

    // Cambiar de estado
    public void changeState(String newState) {
        this.currentState = newState;

        switch (currentState) {
            case "HOME":
                showPanel(HOME_PANEL);
                break;
            case "AUTH":
                showPanel(AUTH_PANEL);
                break;
            case "LOGIN":
                showPanel(LOGIN_PANEL);
                break;
            case "REGISTER":
                showPanel(REGISTER_PANEL);
                break;
            case "BITALINO":
                showPanel(BITALINO_PANEL);
                break;
            case "BITALINO_RECORDING":
                showPanel(BITALINO_RECORDING_PANEL);
                break;
            case "SYMPTOMS_SELECTOR":
                showPanel(SYMPTOMS_SELECTOR_PANEL);
                break;
            default:
                System.out.println("Unknown state: " + currentState);
        }
    }

    private JPanel buildTopBar() {
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
        return topBar;
    }

    // -----------------------
    // Clases internas para cada panel
    // -----------------------

    // Panel HOME
    class HomePanel extends JPanel {
        public HomePanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(171, 191, 234));
            setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

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
            btnContinue.addActionListener(e -> changeState("AUTH"));

            add(Box.createVerticalGlue());
            add(title);
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(subtitle);
            add(Box.createRigidArea(new Dimension(0, 24)));
            add(btnContinue);
            add(Box.createVerticalGlue());
        }
    }

    // Panel AUTH
    class AuthPanel extends JPanel {
        private JButton btnLogin;
        private JButton btnRegister;

        public AuthPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(171, 191, 234));
            setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

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
            btnLogin.addActionListener(e -> changeState("LOGIN"));

            btnRegister = new JButton("Register");
            btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnRegister.setFont(btnRegister.getFont().deriveFont(Font.BOLD, 15f));
            btnRegister.setBackground(new Color(221, 14, 96));
            btnRegister.setForeground(Color.WHITE);
            btnRegister.setOpaque(true);
            btnRegister.setBorderPainted(false);
            btnRegister.setFocusPainted(false);
            btnRegister.setUI(new BasicButtonUI());
            btnRegister.addActionListener(e -> changeState("REGISTER"));

            JButton btnBackHome = new JButton("Return");
            btnBackHome.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnBackHome.addActionListener(e -> changeState("HOME"));

            btnLogin.setEnabled(false);
            btnRegister.setEnabled(false);

            // Actualizar referencias en la clase principal
            PatientSwing.this.authPanel = this;

            add(Box.createVerticalGlue());
            add(choose);
            add(Box.createRigidArea(new Dimension(0, 18)));
            add(btnLogin);
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(btnRegister);
            add(Box.createRigidArea(new Dimension(0, 18)));
            add(btnBackHome);
            add(Box.createVerticalGlue());
        }

        public void setLoginEnabled(boolean enabled) {
            btnLogin.setEnabled(enabled);
        }

        public void setRegisterEnabled(boolean enabled) {
            btnRegister.setEnabled(enabled);
        }
    }

    // Panel LOGIN
    class LoginPanel extends JPanel {
        private JTextField loginUsername;
        private JPasswordField loginPass;

        public LoginPanel() {
            setLayout(new GridBagLayout());
            setBackground(new Color(171, 191, 234));
            setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(8, 8, 8, 8);
            g.fill = GridBagConstraints.HORIZONTAL;

            JLabel loginTitle = new JLabel("LOG IN", SwingConstants.CENTER);
            loginTitle.setFont(loginTitle.getFont().deriveFont(Font.BOLD, 24f));
            g.gridx = 0;
            g.gridy = 0;
            g.gridwidth = 3;
            g.anchor = GridBagConstraints.CENTER;
            add(loginTitle, g);

            loginUsername = underlineField(18);
            loginPass = (JPasswordField) underlineField(new JPasswordField(18));

            g.gridwidth = 1;
            g.anchor = GridBagConstraints.WEST;
            g.weightx = 0;
            g.gridx = 0;
            g.gridy = 1;
            add(new JLabel("Username:"), g);
            g.gridx = 1;
            g.gridy = 1;
            g.weightx = 1.0;
            add(loginUsername, g);

            g.gridx = 0;
            g.gridy = 2;
            g.weightx = 0;
            add(new JLabel("Password:"), g);
            g.gridx = 1;
            g.gridy = 2;
            g.weightx = 1.0;
            add(loginPass, g);

            JButton btnLoginContinue = new JButton("Continue");
            btnLoginContinue.setBackground(new Color(11, 87, 147));
            btnLoginContinue.setForeground(Color.WHITE);
            btnLoginContinue.setOpaque(true);
            btnLoginContinue.setBorderPainted(false);
            btnLoginContinue.setFocusPainted(false);
            btnLoginContinue.setUI(new BasicButtonUI());
            btnLoginContinue.addActionListener(e -> handleLoginContinue());

            g.gridx = 2;
            g.gridy = 2;
            g.weightx = 0;
            g.fill = GridBagConstraints.NONE;
            add(btnLoginContinue, g);

            JLabel dont = new JLabel("Don't have an account?", SwingConstants.CENTER);
            dont.setForeground(new Color(172, 87, 87));
            g.gridx = 0;
            g.gridy = 3;
            g.gridwidth = 3;
            g.fill = GridBagConstraints.HORIZONTAL;
            add(dont, g);

            JButton goCreate = new JButton("Create an account");
            goCreate.setFocusPainted(false);
            goCreate.addActionListener(e -> changeState("REGISTER"));
            g.gridy = 4;
            add(goCreate, g);

            JButton loginReturn = new JButton("Return");
            loginReturn.addActionListener(e -> changeState("AUTH"));
            g.gridy = 5;
            add(loginReturn, g);
        }

        public String getUsername() {
            return loginUsername.getText().trim();
        }

        public String getPassword() {
            return String.valueOf(loginPass.getPassword()).trim();
        }

        public void clearFields() {
            loginUsername.setText("");
            loginPass.setText("");
        }
    }

    // Panel REGISTER
    class RegisterPanel extends JPanel {
        private JTextField fUsername;
        private JPasswordField fPassword;
        private JTextField fName;
        private JTextField fSurname;
        private JTextField fBirthday;
        private JTextField fSex;
        private JTextField fEmail;
        private JTextField fPhone;
        private JTextField fDni;
        private JTextField fInsurance;
        private JTextField fEmergency;

        public RegisterPanel() {
            setLayout(new GridBagLayout());
            setBackground(new Color(171, 191, 234));
            setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
            GridBagConstraints r = new GridBagConstraints();
            r.insets = new Insets(6, 8, 6, 8);
            r.fill = GridBagConstraints.HORIZONTAL;

            JLabel regTitle = new JLabel("SIGN UP AS A PATIENT", SwingConstants.CENTER);
            regTitle.setFont(regTitle.getFont().deriveFont(Font.BOLD, 22f));
            r.gridx = 0;
            r.gridy = 0;
            r.gridwidth = 6;
            r.anchor = GridBagConstraints.CENTER;
            add(regTitle, r);

            fUsername = underlineField(18);
            fPassword = (JPasswordField) underlineField(new JPasswordField(18));
            fName = underlineField(18);
            fSurname = underlineField(18);
            fBirthday = underlineField(10);
            fBirthday.setToolTipText("dd-MM-yyyy (ej: 31-12-1990)");
            fBirthday.setText("dd-MM-yyyy");
            fSex = underlineField(6);
            fSex.setToolTipText("MALE o FEMALE");
            fEmail = underlineField(22);
            fPhone = underlineField(14);
            fDni = underlineField(14);
            fInsurance = underlineField(20);
            fEmergency = underlineField(14);

            int row = 1;
            r.gridwidth = 1;
            r.anchor = GridBagConstraints.WEST;
            r.weightx = 0;

            addField("Username:", fUsername, r, row++);
            addField("Name:", fName, r, row++);
            addField("Surname:", fSurname, r, row++);
            addField("Password:", fPassword, r, row++);
            addField("DNI:", fDni, r, row++);
            addField("Birthday (yyyy-MM-dd or dd/MM/yyyy):", fBirthday, r, row++);
            addField("Email:", fEmail, r, row++);
            addField("Sex (MALE/FEMALE):", fSex, r, row++);
            addField("Phone Number (7-9 digits):", fPhone, r, row++);
            addField("Health Insurance number (digits up to 10):", fInsurance, r, row++);
            addField("Emergency Contact (7-9 digits):", fEmergency, r, row++);

            JButton regCancel = new JButton("Cancel");
            JButton regCreate = new JButton("Create Account");
            regCreate.setBackground(new Color(17, 49, 85));
            regCreate.setForeground(Color.WHITE);
            regCreate.setOpaque(true);
            regCreate.setBorderPainted(false);
            regCreate.setFocusPainted(false);
            regCreate.addActionListener(e -> handleRegisterCreate());

            JPanel btnRow = new JPanel(new BorderLayout());
            btnRow.setOpaque(false);
            btnRow.add(regCancel, BorderLayout.WEST);
            btnRow.add(regCreate, BorderLayout.EAST);

            r.gridx = 0;
            r.gridy = row;
            r.gridwidth = 6;
            r.weightx = 1;
            r.fill = GridBagConstraints.HORIZONTAL;
            add(btnRow, r);

            JButton regReturn = new JButton("Return");
            regReturn.addActionListener(e -> changeState("AUTH"));
            r.gridy = ++row;
            add(regReturn, r);

            regCancel.addActionListener(e -> clearFields());
        }

        private void addField(String label, JComponent field, GridBagConstraints r, int row) {
            r.gridwidth = 1;
            r.weightx = 0;
            r.gridx = 0;
            r.gridy = row;
            add(new JLabel(label), r);
            r.gridx = 1;
            r.gridy = row;
            r.weightx = 1;
            r.gridwidth = 5;
            add(field, r);
        }

        public void clearFields() {
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
        }

        public Map<String, String> getFormData() {
            Map<String, String> data = new HashMap<>();
            data.put("username", fUsername.getText().trim());
            data.put("name", fName.getText().trim());
            data.put("surname", fSurname.getText().trim());
            data.put("dni", fDni.getText().trim().replaceAll("[\\s-]", "").toUpperCase());
            data.put("password", String.valueOf(fPassword.getPassword()).trim());
            data.put("birthday", fBirthday.getText().trim());
            data.put("email", fEmail.getText().trim());
            data.put("sex", fSex.getText().trim().toUpperCase());
            data.put("phone", fPhone.getText().trim());
            data.put("insurance", fInsurance.getText().trim());
            data.put("emergency", fEmergency.getText().trim());
            return data;
        }
    }

    // Panel BITALINO
    class BitalinoPanel extends JPanel {
        private JButton btnRecordBitalino;
        private JButton btnViewDiagnosisFile;

        public BitalinoPanel() {
            setLayout(new GridBagLayout());
            setBackground(new Color(171, 191, 234));
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
            b.gridx = 0;
            b.gridy = 0;
            b.weightx = 1.0;
            b.weighty = 1.0;
            b.anchor = GridBagConstraints.CENTER;
            add(btnRecordBitalino, b);

            btnViewDiagnosisFile = new JButton("View Diagnosis File");
            btnViewDiagnosisFile.setFont(btnRecordBitalino.getFont().deriveFont(Font.BOLD, 24f));
            btnViewDiagnosisFile.setBackground(new Color(182, 118, 45));
            btnViewDiagnosisFile.setForeground(Color.WHITE);
            btnViewDiagnosisFile.setOpaque(true);
            btnViewDiagnosisFile.setBorderPainted(false);
            btnViewDiagnosisFile.setFocusPainted(false);
            btnViewDiagnosisFile.setPreferredSize(new Dimension(420, 80));
            b.gridx = 1;
            b.gridy = 0;
            add(btnViewDiagnosisFile, b);

            btnViewDiagnosisFile.addActionListener(e -> handleViewDiagnosisFile());
            btnRecordBitalino.addActionListener(e -> changeState("BITALINO_RECORDING"));

            JButton btnBitalinoReturn = new JButton("Return");
            btnBitalinoReturn.setFocusPainted(false);
            btnBitalinoReturn.addActionListener(e -> handleLogout());
            b.gridx = 0;
            b.gridy = 1;
            b.weightx = 0;
            b.weighty = 0;
            b.anchor = GridBagConstraints.SOUTH;
            add(btnBitalinoReturn, b);
        }

        public void setRecordButtonEnabled(boolean enabled) {
            btnRecordBitalino.setEnabled(enabled);
        }
    }

    // Panel BITALINO_RECORDING
    class BitalinoRecordingPanel extends JPanel {
        private JButton btnStart;
        private JButton btnStop;
        private JButton btnLogOut;

        public BitalinoRecordingPanel() {
            setLayout(new GridBagLayout());
            setBackground(new Color(171, 191, 234));
            GridBagConstraints br = new GridBagConstraints();
            br.insets = new Insets(20, 20, 20, 20);
            br.fill = GridBagConstraints.NONE;

            btnStart = new JButton("▶️ Start Recording");
            btnStart.setFont(btnStart.getFont().deriveFont(Font.BOLD, 25f));
            btnStart.setBackground(new Color(46, 204, 113));
            btnStart.setForeground(Color.WHITE);
            btnStart.setOpaque(true);
            btnStart.setBorderPainted(false);
            btnStart.setFocusPainted(false);
            btnStart.setPreferredSize(new Dimension(320, 80));

            btnStop = new JButton("⏹️ Stop Recording");
            btnStop.setFont(btnStop.getFont().deriveFont(Font.BOLD, 25f));
            btnStop.setBackground(new Color(231, 76, 60));
            btnStop.setForeground(Color.WHITE);
            btnStop.setOpaque(true);
            btnStop.setBorderPainted(false);
            btnStop.setFocusPainted(false);
            btnStop.setPreferredSize(new Dimension(320, 80));
            btnStop.setEnabled(false);

            btnLogOut = new JButton("Log Out");
            btnLogOut.setBackground(new Color(11, 87, 147));
            btnLogOut.setForeground(Color.WHITE);
            btnLogOut.setOpaque(true);
            btnLogOut.setBorderPainted(false);
            btnLogOut.setFocusPainted(false);
            btnLogOut.setEnabled(true);


            btnStart.addActionListener(e -> handleStartRecording());
            btnStop.addActionListener(e -> handleStopRecording());
            btnLogOut.addActionListener(e ->handleLogout());

            br.gridx = 0;
            br.gridy = 0;
            add(btnStart, br);
            br.gridx = 1;
            br.gridy = 0;
            add(btnStop, br);
            br.gridx = 0;
            br.gridy = 1;
            br.gridwidth = 2;
            add(btnLogOut, br);

        }

        public void setStartEnabled(boolean enabled) {
            btnStart.setEnabled(enabled);
        }

        public void setStopEnabled(boolean enabled) {
            btnStop.setEnabled(enabled);
        }

        public void setContinueEnabled(boolean enabled) {
            btnLogOut.setEnabled(enabled);
        }

        public void setRecordingState(boolean recording) {
            btnStart.setEnabled(!recording);
            btnStop.setEnabled(recording);
            btnLogOut.setEnabled(!recording);
            // El botón de continuar solo se habilita cuando no se está grabando y ya se ha grabado algo (o cuando se ha detenido)
            // Pero por ahora, lo manejamos en el flujo de stop.
            // En el manejo de stop, luego de detener, se habilita el continue.
        }
    }

    // Panel SYMPTOMS_SELECTOR
    class SymptomsSelectorPanel extends JPanel {
        public SymptomsSelectorPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(171, 191, 234));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel symTitle = new JLabel("Select symptoms (functionality present)", SwingConstants.CENTER);
            symTitle.setFont(symTitle.getFont().deriveFont(Font.BOLD, 18f));
            add(symTitle, BorderLayout.NORTH);

            JTextArea info = new JTextArea("Select symptoms after completing the recording. The client will send the selected options to the server.");
            info.setEditable(false);
            info.setBackground(new Color(171, 191, 234));
            add(new JScrollPane(info), BorderLayout.CENTER);

            JPanel symBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton symBack = new JButton("Back");
            symBack.addActionListener(e -> changeState("BITALINO"));
            symBtns.add(symBack);
            add(symBtns, BorderLayout.SOUTH);
        }
    }

    // -----------------------
    // Handlers (Action logic)
    // -----------------------

    private void handleLoginContinue() {
        String username = loginPanel.getUsername();
        String pass = loginPanel.getPassword();

        if (username.isBlank() || pass.isBlank()) {
            JOptionPane.showMessageDialog(this, "Complete all fields", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] server = askServerHostPortIfNotConnected();
        if (server == null) return;

        new SwingWorker<Void, Void>() {
            private String serverMsg = null;
            private boolean success = false;

            @Override
            protected Void doInBackground() {
                try {
                    ensureConnectedRetry(server[0], Integer.parseInt(server[1]));
                    socket.setSoTimeout(5000);

                    out.writeUTF("LOGIN");
                    out.writeUTF(username);
                    out.writeUTF(pass);
                    out.flush();

                    String statusResp = in.readUTF();
                    System.out.println(statusResp);

                    if ("LOGIN_RESULT".equals(statusResp)) {
                        boolean ok = in.readBoolean();
                        serverMsg = in.readUTF();
                        serverMsg = serverMsg+ "->"+ in.readUTF();
                        success = ok;
                        if (success) currentUsername = username;
                    } else {
                        serverMsg = "Unexpected response from the server";
                        serverMsg = serverMsg+ "->"+ in.readUTF();
                    }
                } catch (SocketTimeoutException ste) {
                    serverMsg = "Timeout while communicating with the server.";
                } catch (EOFException eof) {
                    serverMsg = "Connection closed by the server.";
                    cleanupResources();
                } catch (IOException ex) {
                    serverMsg = "Error I/O: " + ex.getMessage();
                    cleanupResources();
                } finally {
                    try {
                        if (socket != null) socket.setSoTimeout(0);
                    } catch (Exception ignored) {
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (success) {
                    bitalinoPanel.setRecordButtonEnabled(true);
                    authPanel.setLoginEnabled(true);
                    authPanel.setRegisterEnabled(false);
                    JOptionPane.showMessageDialog(PatientSwing.this, serverMsg == null ? "Login successful" : serverMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
                    changeState("BITALINO_RECORDING");
                    loginPanel.clearFields();
                } else {
                    JOptionPane.showMessageDialog(PatientSwing.this, "Login failed: " + (serverMsg == null ? "unknown" : serverMsg), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleRegisterCreate() {
        Map<String, String> formData = registerPanel.getFormData();

        String username = formData.get("username");
        String name = formData.get("name");
        String surname = formData.get("surname");
        String dni = formData.get("dni");
        String pass = formData.get("password");
        String birthdayInput = formData.get("birthday");
        String email = formData.get("email");
        String sex = formData.get("sex");
        String phone = formData.get("phone");
        String insurance = formData.get("insurance");
        String emergency = formData.get("emergency");

        // Validaciones (las mismas que antes)
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
                        msg = in.readUTF();
                        msg = msg+ "->"+ in.readUTF();
                    } else {
                        msg = "Server error: " + resp;
                        msg = msg+ "->"+ in.readUTF();
                    }
                } catch (IOException ex) {
                    msg = "I/O error: " + ex.getMessage();
                    cleanupResources();
                }
                return null;
            }

            @Override
            protected void done() {
                if (ok) {
                    JOptionPane.showMessageDialog(PatientSwing.this, msg != null ? msg : "Account created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    changeState("BITALINO_RECORDING");
                    registerPanel.clearFields();
                } else {
                    JOptionPane.showMessageDialog(PatientSwing.this, "Registration failed: " + (msg != null ? msg : "Unknown error"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleLogout(){
        try {
            out.writeUTF("LOG_OUT");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentUsername = null;
        changeState("AUTH");

    }

    private void handleViewDiagnosisFile() {
        JOptionPane.showMessageDialog(this, "La visualización del fichero de diagnóstico ha sido eliminada.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleStartRecording() {
        if (!connectedFlag || out == null || in == null) {
            JOptionPane.showMessageDialog(this, "Not connected to server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Cambiar el estado de los botones inmediatamente
        bitalinoRecordingPanel.setRecordingState(true);
        stopRequested = false;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    if (!startRecording(out)) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(PatientSwing.this, "Error sending START", "Error", JOptionPane.ERROR_MESSAGE);
                            bitalinoRecordingPanel.setRecordingState(false); // Revertir a estado no grabando
                        });
                        return null;
                    }
                    if (!readyToRecord(in)) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(PatientSwing.this, "Server not ready to record", "Error", JOptionPane.ERROR_MESSAGE);
                            bitalinoRecordingPanel.setRecordingState(false); // Revertir a estado no grabando
                        });
                        return null;
                    }
                    // arrancar hilo de lectura/envío desde BitalinoManager
                    bitalinoManager.startRecordingToServer(out, in);
                    recording = true;
                } catch (BITalinoException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PatientSwing.this, "Error in Bitalino: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        bitalinoRecordingPanel.setRecordingState(false); // Revertir a estado no grabando
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                // No hacemos nada aquí porque el estado se maneja con setRecordingState
            }
        }.execute();
    }

    private void handleStopRecording() {
        if (!connectedFlag || out == null || in == null) {
            JOptionPane.showMessageDialog(this, "Not connected to server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        stopRequested = true;
        // Cambiar el estado de los botones inmediatamente
        bitalinoRecordingPanel.setRecordingState(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (!stopRecording(out)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PatientSwing.this, "Error sending STOP", "Error", JOptionPane.ERROR_MESSAGE);
                        // Revertir a estado grabando porque no se pudo detener
                        bitalinoRecordingPanel.setRecordingState(true);
                    });
                    return null;
                }
                // Pedir al manager que pare la adquisición local
                bitalinoManager.requestStopRecording();
                recording = false;


                if (!RecordingStop(in)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PatientSwing.this, "STOP was not confirmed by the server", "Error", JOptionPane.ERROR_MESSAGE);
                    });
                    return null;
                }
                if (!SelectSymptoms(in)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PatientSwing.this, "The server did not request symptom selection", "Info", JOptionPane.INFORMATION_MESSAGE);
                    });
                    return null;
                }
                String csv = getSymptomsFromUser();
                System.out.println(csv);
                if (csv == null) {
                    return null;
                }
                try (Scanner sc = new Scanner(csv)) {
                    sendSymptoms(sc, out, in);
                }
                if (isSymptomsReceived(in)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PatientSwing.this, "Symptoms sent and confirmed by the server", "Info", JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PatientSwing.this, "The server did not confirm receipt of the symptoms", "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                // Habilitar el botón de continuar después de detener la grabación
                bitalinoRecordingPanel.setContinueEnabled(true);
            }
        }.execute();
    }

    // -----------------------
    // Métodos de utilidad
    // -----------------------

    private String[] askServerHostPortIfNotConnected() {
        if (socket != null && socket.isConnected() && !socket.isClosed() && connectedFlag && lastHost != null && lastPort > 0) {
            return new String[]{lastHost, String.valueOf(lastPort)};
        }
        showConnectDialog();
        return connectedFlag ? new String[]{lastHost, String.valueOf(lastPort)} : null;
    }

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
        p.add(new JLabel("MAC address: "));
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
                JOptionPane.showMessageDialog(dlg, "Invalid Host", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Invalid port", "Error", JOptionPane.ERROR_MESSAGE);
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
                        connectToServer(host, port, mac);
                        bitalinoManager.connect(mac);
                        ok = true;
                        msg = "Connected to server";
                    } catch (IOException ex) {
                        msg = "Connect failed: " + ex.getMessage();
                        cleanupResources();
                        } catch (BITalinoException ex) {
                        throw new RuntimeException(ex);
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
                        authPanel.setLoginEnabled(true);
                        authPanel.setRegisterEnabled(true);
                        JOptionPane.showMessageDialog(dlg, "Connected!", "Info", JOptionPane.INFORMATION_MESSAGE);
                        dlg.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dlg, msg, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        dlg.setContentPane(p);
        dlg.setResizable(false);
        dlg.setVisible(true);
    }

    private synchronized void connectToServer(String host, int port, String mac) throws IOException {
        cleanupResources();
        socket = new Socket(host, port);
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out.writeUTF("Patient");
        out.flush();
        lastHost = host;
        lastPort = port;
        connectedFlag = true;
    }

    private synchronized void ensureConnected(String host, int port) throws IOException {
        if (connectedFlag && socket != null && socket.isConnected() && !socket.isClosed()) return;
        cleanupResources();
        socket = new Socket(host, port);
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out.writeUTF("Patient");
        out.flush();
        lastHost = host;
        lastPort = port;
        connectedFlag = true;
    }

    private synchronized void ensureConnectedRetry(String host, int port) throws IOException {
        try {
            ensureConnected(host, port);
        } catch (IOException firstEx) {
            cleanupResources();
            socket = new Socket(host, port);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
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

    // Métodos de utilidad estáticos (los mismos que antes)
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

    private static boolean validateDNI(String dni) {
        if (dni == null) return false;
        return dni.matches("\\d{8}[A-Z]");
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

    // Recording lifecycle helpers (remain static)
    private static boolean startRecording(DataOutputStream out) {
        if (out == null) return false;
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

    public static void sendFragmentsOfRecording(String dataString, DataOutputStream out) {
        try {
            out.writeUTF("SEND_FRAGMENTS_OF_RECORDING");
            out.writeUTF(dataString);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/*public static void sendFragmentsOfRecording(String dataString, DataOutputStream out) {
        try {

            out.writeUTF(dataString);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    private static boolean stopRecording(DataOutputStream outputStream) {
        if (outputStream == null) return false;
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
            in.readUTF();
            String response = in.readUTF();
            System.out.println("respuesta recivida: "+response);
            return "RECORDING_STOP".equals(response);
        } catch (IOException e) {
            System.err.println("I/O error during RECORDING_STOP: " + e.getMessage());
            return false;
        }
    }

    private static boolean SelectSymptoms(DataInputStream in) {
        if (in == null) return false;
        try {
            String response = in.readUTF();
            return "SELECT_SYMPTOMS".equals(response);
        } catch (IOException e) {
            System.err.println("I/O error during SELECT_SYMPTOMS: " + e.getMessage());
            return false;
        }
    }

    private static void sendSymptoms(Scanner scanner, DataOutputStream out, DataInputStream in) {
        try {
            String line = scanner.nextLine().trim();
            if (line == null) line = "";
            //out.writeUTF("SYMPTOMS");
            out.writeUTF(line);
            out.flush();
        } catch (IOException e) {
            System.err.println("I/O error sending symptoms: " + e.getMessage());
        }
    }

    private static boolean isSymptomsReceived(DataInputStream in) {
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
                panel.add(new JLabel("Select your symptoms:"));
                panel.add(c1);
                panel.add(c2);
                panel.add(c3);
                panel.add(c4);
                panel.add(c5);
                panel.add(c6);

                int ok = JOptionPane.showConfirmDialog(PatientSwing.this, panel, "Select Symptoms", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PatientSwing().setVisible(true));
    }
}