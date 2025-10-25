package executable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

import jdbc.ConnectionManager;
import jdbc.JDBCPatientManager;
import jdbc.JDBCUserManager;
import jdbcInterfaces.PatientManager;
import jdbcInterfaces.UserManager;
import pojos.Patient;
import common.enums.Sex;
import pojos.User;

public class MenuPatientSwing extends JFrame {

    private final ConnectionManager conMan = new ConnectionManager();
    private final PatientManager patientMan = new JDBCPatientManager(conMan);
    private final UserManager userMan = new JDBCUserManager(conMan);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public MenuPatientSwing() {
        super("App for Patients");
        initUI();
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
        btnExit.addActionListener(e -> System.exit(0));

        JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftWrap.setOpaque(false);
        leftWrap.add(btnExit);
        topBar.add(leftWrap, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        // PANTALLA 1
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

        // PANTALLA 2
        JPanel auth = new JPanel();
        auth.setLayout(new BoxLayout(auth, BoxLayout.Y_AXIS));
        auth.setBackground(new Color(171, 191, 234));
        auth.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel choose = new JLabel("Choose an option", SwingConstants.CENTER);
        choose.setAlignmentX(Component.CENTER_ALIGNMENT);
        choose.setFont(choose.getFont().deriveFont(Font.BOLD, 22f));

        JButton btnLogin = new JButton("Login");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 15f));
        btnLogin.setBackground(new Color(205, 103, 106));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setUI(new BasicButtonUI());
        btnLogin.addActionListener(e -> cardLayout.show(cards, "login"));

        JButton btnRegister = new JButton("Register");
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

        JTextField loginUsername = underlineField(18); // usernamePatient
        JPasswordField loginPass = (JPasswordField) underlineField(new JPasswordField(18)); // passwordPatient

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
            try {
                String username = loginUsername.getText().trim();
                String pass = String.valueOf(loginPass.getPassword()).trim();

                if (username.isBlank() || pass.isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Por favor, introduce Username y contraseña.",
                            "Faltan datos", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //Patient p = patientMan.getPatientByUsernameAndPassword(username, pass);
                boolean verified = userMan.verifyPassword(username, pass);
                Patient p = null;
                if (verified) {
                    User u = userMan.getUserByUsername(username);
                    p = patientMan.getPatientByUserId(u.getIdUser());
                }


                if (p != null) {
                    JOptionPane.showMessageDialog(this,
                            "Bienvenido, " + p.getNamePatient() + " " + p.getSurnamePatient() + "!",
                            "Login correcto", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(cards, "bitalino");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Usuario o contraseña incorrectos.",
                            "Login fallido", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error de base de datos: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
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

        // Fecha DD/MM/YYYY
        JTextField fDay   = underlineField(2);
        JTextField fMonth = underlineField(2);
        JTextField fYear  = underlineField(4);
        fDay.setHorizontalAlignment(JTextField.CENTER);
        fMonth.setHorizontalAlignment(JTextField.CENTER);
        fYear.setHorizontalAlignment(JTextField.CENTER);
        fDay.setPreferredSize(new Dimension(32, 24));
        fMonth.setPreferredSize(new Dimension(32, 24));
        fYear.setPreferredSize(new Dimension(56, 24));
        JPanel birthdayFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        birthdayFields.setOpaque(false);
        birthdayFields.add(fDay); birthdayFields.add(new JLabel("/"));
        birthdayFields.add(fMonth); birthdayFields.add(new JLabel("/"));
        birthdayFields.add(fYear);

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
        r.gridx = 0; r.gridy = row; register.add(new JLabel("Birthday (DD/MM/YYYY):"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(birthdayFields, r);

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
            fDay.setText("");
            fMonth.setText("");
            fYear.setText("");
            fEmail.setText("");
            fSex.setText("");
            fPhone.setText("");
            fInsurance.setText("");
            fEmergency.setText("");
        });

        regCreate.addActionListener(e -> {
            try {
                String username = fUsername.getText().trim();
                String name = fName.getText().trim();
                String surname = fSurname.getText().trim();
                String dni = fDni.getText().trim();
                String pass = String.valueOf(fPassword.getPassword()).trim();
                if (username.isBlank() || name.isBlank() || surname.isBlank() || dni.isBlank() || pass.isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Username, Name, Surname, DNI y contraseña son obligatorios.",
                            "Faltan datos", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                java.util.Date dob = null;
                String dd = fDay.getText().trim(), mm = fMonth.getText().trim(), yy = fYear.getText().trim();
                if (!dd.isBlank() && !mm.isBlank() && !yy.isBlank()) {
                    dob = new SimpleDateFormat("dd/MM/yyyy").parse(dd + "/" + mm + "/" + yy);
                }

                Sex sexVal = null;
                String sx = fSex.getText().trim();
                if (sx.equalsIgnoreCase("F") || sx.equalsIgnoreCase("Female")) sexVal = Sex.FEMALE;
                else if (sx.equalsIgnoreCase("M") || sx.equalsIgnoreCase("Male")) sexVal = Sex.MALE;

                User u = new User(username, pass, "patient");

                int userid = u.getIdUser();

                Patient p = new Patient(
                        name,
                        surname,
                        dni,
                        dob,
                        fEmail.getText().trim(),
                        sexVal,
                        parseIntSafe(fPhone.getText().trim()),
                        parseIntSafe(fInsurance.getText().trim()),
                        parseIntSafe(fEmergency.getText().trim()),
                        userid
                );

                patientMan.addPatient(p);
                JOptionPane.showMessageDialog(this, "¡Cuenta creada!", "Registro",
                        JOptionPane.INFORMATION_MESSAGE);

                regCancel.doClick();
                cardLayout.show(cards, "auth");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // PANTALLA 5 : BitalinoRecording
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

        // PANTALLA 6 : Start/Stop/Continue Recording
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
            //para iniciar la grabación
        });

        btnStop.addActionListener(e -> {
            btnStop.setEnabled(false);
            btnStart.setEnabled(true);
            btnContinueRec.setEnabled(true);
            //para parar la grabación
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

        // PANTALLA 7 : Symptoms Selector
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

        // Acción para pasar de pantalla 5 a 6
        btnRecordBitalino.addActionListener(e -> cardLayout.show(cards, "bitalinoRecording"));

        cardLayout.show(cards, "home");
    }

    //  Add symptoms selector
    public JPanel createSymptomsSelectorPanel(java.util.function.Consumer<java.util.List<String>> onSave) {
        final String[] ALL = {
                "FAINTED", "WEAKNESS", "SHAKENESS", "HEAD PAIN",
                "PALPITATION", "ARM PAIN", "FIVER", "CHEAST PAIN"
        };
        final java.awt.Color CHIP_BG = new java.awt.Color(179, 212, 238);
        final java.util.Set<String> selected = new java.util.LinkedHashSet<>();
        final java.util.Map<String, javax.swing.JButton> addBtns = new java.util.HashMap<>();

        JPanel root = new JPanel(new java.awt.BorderLayout(12,12));
        root.setBorder(new javax.swing.border.EmptyBorder(8,8,8,8));

        JLabel title = new JLabel("Add symptoms", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        root.add(title, BorderLayout.NORTH);

        final JPanel selectedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width, Math.max(80, d.height));
            }
        };
        JLabel lblSel = new JLabel("Selected");
        JPanel selBox = new JPanel();
        selBox.setLayout(new BoxLayout(selBox, BoxLayout.Y_AXIS));
        selBox.add(lblSel);
        JPanel selBorder = new JPanel(new BorderLayout());
        selBorder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                new javax.swing.border.EmptyBorder(6,6,6,6)));
        selBorder.add(selectedPanel, BorderLayout.CENTER);
        selBox.add(selBorder);

        final JPanel availablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        for (String s : ALL) {
            JButton b = new JButton(s) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(CHIP_BG);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFocusPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(false);
            b.setBorder(new javax.swing.border.EmptyBorder(8,16,8,16));
            b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> {
                if (selected.add(s)) {
                    JPanel chip = new JPanel(new GridBagLayout()) {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(CHIP_BG);
                            g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                            g2.dispose();
                            super.paintComponent(g);
                        }
                    };
                    chip.setOpaque(false);
                    chip.setBorder(new javax.swing.border.EmptyBorder(6,10,6,6));
                    JLabel txt = new JLabel(s);
                    txt.setFont(txt.getFont().deriveFont(Font.BOLD, 13f));
                    JButton close = new JButton("x");
                    close.setFocusPainted(false);
                    close.setMargin(new java.awt.Insets(0,8,0,8));
                    close.addActionListener(ev -> {
                        selected.remove(s);
                        selectedPanel.remove(chip);
                        selectedPanel.revalidate();
                        selectedPanel.repaint();
                        JButton add = addBtns.get(s);
                        if (add != null) add.setEnabled(true);
                    });
                    GridBagConstraints c = new GridBagConstraints();
                    c.gridx=0; c.insets=new java.awt.Insets(0,0,0,6);
                    chip.add(txt, c);
                    c.gridx=1; c.insets=new java.awt.Insets(0,0,0,0);
                    chip.add(close, c);
                    selectedPanel.add(chip);
                    selectedPanel.revalidate();
                    selectedPanel.repaint();
                    b.setEnabled(false);
                }
            });
            addBtns.put(s, b);
            availablePanel.add(b);
        }
        JScrollPane scroll = new JScrollPane(availablePanel);
        scroll.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel lblAvail = new JLabel("Available");
        JPanel availBox = new JPanel();
        availBox.setLayout(new BoxLayout(availBox, BoxLayout.Y_AXIS));
        availBox.add(lblAvail);
        JPanel availBorder = new JPanel(new BorderLayout());
        availBorder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                new javax.swing.border.EmptyBorder(6,6,6,6)));
        availBorder.add(scroll, BorderLayout.CENTER);
        availBox.add(availBorder);

        JButton btnReturn = new JButton("Return");
        btnReturn.setFont(btnReturn.getFont().deriveFont(Font.BOLD, 14f));
        btnReturn.addActionListener(e -> cardLayout.show(cards, "bitalinoRecording"));

        JButton btnContinue = new JButton("Continue >>");
        btnContinue.setFont(btnContinue.getFont().deriveFont(Font.BOLD, 14f));
        //btnContinue.addActionListener(e -> JOptionPane.showMessageDialog(root, "Continuar a la siguiente pantalla"));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.add(btnReturn);
        bottom.add(btnContinue);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(selBox);
        center.add(Box.createVerticalStrut(8));
        center.add(availBox);

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);


        return root;
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

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPatientSwing().setVisible(true));
    }
}
