package screens;
import screens.*;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

import dao.ConnectionProvider;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ErrorHandler;

import java.time.LocalDateTime;
import java.sql.Timestamp;

import common.OTPGenerator;
import common.EmailService;

public class login extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnExit;
    private JCheckBox chkShowPassword;



    public login() {
        super("SISMS - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BACKGROUND);

        createLeftPanel();
        createRightPanel();

        setVisible(true);
    }

    private void createLeftPanel() {
        RoundedPanel leftPanel = new RoundedPanel(0, false);
        leftPanel.setBackground(UIStyle.PRIMARY);
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(600, 0));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(Box.createVerticalGlue());

        JLabel lblTitle = new JLabel("SISMS");
        lblTitle.setFont(UIStyle.getFont(Font.PLAIN, 48));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblTitle);

        contentPanel.add(Box.createVerticalStrut(15));

        JLabel lblSubtitle = new JLabel("Secure Inventory and Supply Chain Management System");
        lblSubtitle.setFont(UIStyle.FONT_BODY);
        lblSubtitle.setForeground(new Color(255, 255, 255, 200));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblSubtitle);

        contentPanel.add(Box.createVerticalGlue());

        leftPanel.add(contentPanel, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
    }

    private void createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(true);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(100, 80, 100, 80));

        JLabel lblWelcome = new JLabel("Welcome Back");
        lblWelcome.setFont(UIStyle.FONT_HEADING);
        lblWelcome.setForeground(UIStyle.TEXT_DARK);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblWelcome);

        rightPanel.add(Box.createVerticalStrut(10));

        JLabel lblSubtitle = new JLabel("Sign in to continue to your account");
        lblSubtitle.setFont(UIStyle.FONT_BODY);
        lblSubtitle.setForeground(UIStyle.TEXT_LIGHT);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblSubtitle);

        rightPanel.add(Box.createVerticalStrut(50));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(UIStyle.FONT_BODY_BOLD);
        lblEmail.setForeground(UIStyle.TEXT_DARK);
        lblEmail.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblEmail);
        centerPanel.add(Box.createVerticalStrut(8));

        txtEmail = new JTextField();
        txtEmail.setFont(UIStyle.FONT_BODY);
        txtEmail.setBackground(Color.WHITE);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtEmail.setMaximumSize(new Dimension(400, UIStyle.INPUT_HEIGHT));
        centerPanel.add(txtEmail);
        centerPanel.add(Box.createVerticalStrut(20));

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(UIStyle.FONT_BODY_BOLD);
        lblPassword.setForeground(UIStyle.TEXT_DARK);
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblPassword);
        centerPanel.add(Box.createVerticalStrut(8));

        JPanel passwordPanel = new JPanel(null);
        passwordPanel.setOpaque(false);
        passwordPanel.setPreferredSize(new Dimension(400, UIStyle.INPUT_HEIGHT));
        passwordPanel.setMaximumSize(new Dimension(400, UIStyle.INPUT_HEIGHT));
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(UIStyle.FONT_BODY);
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtPassword.setBounds(0, 0, 400, UIStyle.INPUT_HEIGHT);
        passwordPanel.add(txtPassword);

        chkShowPassword = new JCheckBox("Show");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFont(UIStyle.FONT_SMALL);
        chkShowPassword.addActionListener(e ->
                txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : '•')
        );
        chkShowPassword.setBounds(350, 0, 50, UIStyle.INPUT_HEIGHT);
        passwordPanel.add(chkShowPassword);

        centerPanel.add(passwordPanel);
        centerPanel.add(Box.createVerticalStrut(30));

        btnLogin = new JButton("Login");
        btnLogin.setFont(UIStyle.getFont(Font.BOLD, 13));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(UIStyle.PRIMARY);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(400, UIStyle.BUTTON_HEIGHT));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> loginAction());
        centerPanel.add(btnLogin);

        centerPanel.add(Box.createVerticalStrut(15));

        btnExit = new JButton("Exit");
        btnExit.setFont(UIStyle.getFont(Font.BOLD, 13));
        btnExit.setForeground(UIStyle.TEXT_DARK);
        btnExit.setBackground(UIStyle.SEPARATOR);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setMaximumSize(new Dimension(400, UIStyle.BUTTON_HEIGHT));
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExit.addActionListener(e -> exitAction());
        centerPanel.add(btnExit);

        rightPanel.add(centerPanel);
        rightPanel.add(Box.createVerticalGlue());

        add(rightPanel, BorderLayout.CENTER);
    }

    // -------------------------------------------------------
    // LOGIN LOGIC (OTP + Lockout)
    // -------------------------------------------------------
    private void loginAction() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM appuser WHERE email=?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect email or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String storedPassword = rs.getString("password");
            String role = rs.getString("userRole");
            int attempts = rs.getInt("failedAttempts");
            String status = rs.getString("status");

            if ("Inactive".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this,
                        "Your account is locked. Contact admin.",
                        "Account Locked", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean passwordMatches = storedPassword.startsWith("$2a$")
                    ? org.mindrot.jbcrypt.BCrypt.checkpw(password, storedPassword)
                    : password.equals(storedPassword);

            if (!passwordMatches) {
                attempts++;
                PreparedStatement psUpdate = con.prepareStatement(
                        "UPDATE appuser SET failedAttempts=? WHERE email=?");
                psUpdate.setInt(1, attempts);
                psUpdate.setString(2, email);
                psUpdate.executeUpdate();

                if (attempts >= 3) {

                    PreparedStatement psLock = con.prepareStatement(
                            "UPDATE appuser SET status='Inactive' WHERE email=?");
                    psLock.setString(1, email);
                    psLock.executeUpdate();

                    // SEND NOTIFICATION EMAIL TO THE USER
                    sendFailedAttemptsEmail(email);

                    JOptionPane.showMessageDialog(this,
                            "Your account is now inactive after 3 failed attempts.",
                            "Account Locked", JOptionPane.ERROR_MESSAGE);

                    return;
                }
                else {
                    JOptionPane.showMessageDialog(this,
                            "Incorrect password. Attempts left: " + (3 - attempts),
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            // Reset attempts
            PreparedStatement psReset = con.prepareStatement(
                    "UPDATE appuser SET failedAttempts=0 WHERE email=?");
            psReset.setString(1, email);
            psReset.executeUpdate();

            // Generate OTP
            String otp = OTPGenerator.generateOTP();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

            PreparedStatement psOtp = con.prepareStatement(
                    "UPDATE appuser SET otp=?, otpExpiry=? WHERE email=?");
            psOtp.setString(1, otp);
            psOtp.setTimestamp(2, Timestamp.valueOf(expiry));
            psOtp.setString(3, email);
            psOtp.executeUpdate();

            // SEND OTP EMAIL (NO POPUPS)
            String message =
                    "Dear user,\n" +
                            "Your One Time Password (OTP) is: " + otp + "\n\n" +
                            "In order to protect your account from misuse / fraudulent activity, " +
                            "it is requested to please never share the OTP with anyone.\n\n" +
                            "Note: Please do not reply to this email as this is a system generated email. For further queries, contact us at support@sisms.com.\n\n" +
                            "Yours sincerely,\n" +
                            "SISMS";

            try { EmailService.sendEmail(email, "SISMS One Time Password (OTP)", message); }
            catch (Throwable mailEx) { System.out.println("OTP email error: " + mailEx); }


            // OPEN OTP WINDOW
            setVisible(false);
            new OTPVerification(email, role).setVisible(true);

        } catch (Exception ex) {
            ErrorHandler.showError(ex, "logging in");
        }
    }

    private void sendFailedAttemptsEmail(String toEmail) {
        String subject = "Your SISMS Account Has Been Deactivated";

        String body =
                "Dear User,\n\n" +
                        "You have entered the wrong password 3 times.\n" +
                        "Your SISMS account has now been deactivated for security reasons.\n\n" +
                        "To reactivate your account, please contact the SuperAdmin at nabiafaisal26@gmail.com\n" +
                        "Regards,\n" +
                        "SISMS Security System";

        try { EmailService.sendEmail(toEmail, subject, body); }
        catch (Exception mailEx) { System.out.println("Lockout email error: " + mailEx.getMessage()); }
    }


    private void exitAction() {
        int a = JOptionPane.showConfirmDialog(this,
                "Do you want to close the application?",
                "Exit Confirmation", JOptionPane.YES_NO_OPTION);
        if (a == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(login::new);
    }
}
