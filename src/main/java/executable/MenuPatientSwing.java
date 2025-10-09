package executable;


import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;


import jdbc.ConnectionManager;
import jdbc.JDBCPatientManager;
import jdbcInterfaces.PatientManager;
import pojos.Patient;
import common.enums.Sex;

public class MenuPatientSwing extends JFrame {

    // Conexión con JDBC
    private final ConnectionManager conMan = new ConnectionManager();
    private final PatientManager patientMan = new JDBCPatientManager(conMan);


    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout); // container for screens

    public MenuPatientSwing() {
        super("App for Patients");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(680, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());


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
        btnLogin.setBackground(new Color(34, 139, 34));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setUI(new BasicButtonUI());
        btnLogin.addActionListener(e -> cardLayout.show(cards, "login"));

        JButton btnRegister = new JButton("Register");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setFont(btnRegister.getFont().deriveFont(Font.BOLD, 15f));
        btnRegister.setBackground(new Color(46, 204, 113));
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
        auth.add(choose);               auth.add(Box.createRigidArea(new Dimension(0, 18)));
        auth.add(btnLogin);             auth.add(Box.createRigidArea(new Dimension(0, 10)));
        auth.add(btnRegister);          auth.add(Box.createRigidArea(new Dimension(0, 18)));
        auth.add(btnBackHome);
        auth.add(Box.createVerticalGlue());

        //PANTALLA 3
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

        JTextField loginUser = underlineField(18);
        JPasswordField loginPass = (JPasswordField) underlineField(new JPasswordField(18));

        g.gridwidth = 1; g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 1; login.add(new JLabel("User Name:"), g);
        g.gridx = 1; g.gridy = 1; g.weightx = 1.0; login.add(loginUser, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 0; login.add(new JLabel("Password:"), g);
        g.gridx = 1; g.gridy = 2; g.weightx = 1.0; login.add(loginPass, g);

        JButton btnLoginContinue = new JButton("Continue");
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

        // PANTALLA 4
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


        JTextField fName       = underlineField(18); // namePatient
        JPasswordField fPassword = (JPasswordField) underlineField(new JPasswordField(18)); // passwordPatient
        JTextField fDni        = underlineField(14); // dniPatient

        // DD / MM / YYYY
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

        JTextField fEmail      = underlineField(22); // emailPatient
        JTextField fSex        = underlineField(6);  // sexPatient (M/F)
        JTextField fPhone      = underlineField(14); // phoneNumberPatient
        JTextField fInsurance  = underlineField(20); // healthInsuranceNumberPatient
        JTextField fEmergency  = underlineField(14); // emergencyContactPatient

        int row = 1;
        r.gridwidth = 1; r.anchor = GridBagConstraints.WEST; r.weightx = 0;

        r.gridx = 0; r.gridy = row; register.add(new JLabel("Name:"), r);
        r.gridx = 1; r.gridy = row++; r.weightx = 1; r.gridwidth = 5; register.add(fName, r);

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

        // Botones inferiores
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

        //Limpieza de campos
        regCancel.addActionListener(e -> {
            fName.setText("");
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

        // INSERTAR EN BBDD
        regCreate.addActionListener(e -> {
            try {

                if (fName.getText().isBlank()
                        || fDni.getText().isBlank()
                        || String.valueOf(fPassword.getPassword()).isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Please fill Name, DNI and Password.",
                            "Missing data", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Parse de fecha
                java.util.Date dob = new java.text.SimpleDateFormat("dd/MM/yyyy")
                        .parse(fDay.getText().trim()+"/"+fMonth.getText().trim()+"/"+fYear.getText().trim());

                // Sexo: solo M o F
                String sx = fSex.getText().trim();
                Sex sexVal;
                if (sx.equalsIgnoreCase("F") || sx.equalsIgnoreCase("Female")) {
                    sexVal = Sex.FEMALE;
                } else if (sx.equalsIgnoreCase("M") || sx.equalsIgnoreCase("Male")) {
                    sexVal = Sex.MALE;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Sex must be 'M' or 'F'.",
                            "Invalid value", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Construcción de Patient (sin id)
                Patient p = new Patient(
                        fName.getText().trim(),
                        fDni.getText().trim(),
                        dob,
                        fEmail.getText().trim(),
                        String.valueOf(fPassword.getPassword()),
                        sexVal,
                        parseIntSafe(fPhone.getText().trim()),
                        parseIntSafe(fInsurance.getText().trim()),
                        parseIntSafe(fEmergency.getText().trim())
                );

                patientMan.addPatient(p);
                JOptionPane.showMessageDialog(this, "Account Created!", "Register",
                        JOptionPane.INFORMATION_MESSAGE);

                regCancel.doClick();
                cardLayout.show(cards, "auth");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });


        cards.add(home, "home");
        cards.add(auth, "auth");
        cards.add(login, "login");
        cards.add(new JScrollPane(register), "register"); // scroll por si hace falta
        add(cards, BorderLayout.CENTER);

        cardLayout.show(cards, "home");
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

    //
    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPatientSwing().setVisible(true));
    }
}
