package screens;
import screens.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.LocalDateTime;

import dao.ConnectionProvider;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ErrorHandler;

public class OTPVerification extends JFrame {

    private final String userEmail;
    private final String userRole;

    private JTextField txtOtp;
    private JButton btnVerify;
    private JButton btnCancel;

    public OTPVerification(String userEmail, String userRole) {
        super("SISMS - OTP Verification");
        this.userEmail = userEmail;
        this.userRole = userRole;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(450, 260);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BACKGROUND);

        createUI();

        setVisible(true);
    }

    private void createUI() {
        RoundedPanel main = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        main.setBackground(UIStyle.CARD_BG);
        main.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Enter One-Time Password (OTP)");
        lblTitle.setFont(UIStyle.FONT_SUBHEADING);
        lblTitle.setForeground(UIStyle.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInfo = new JLabel(
                "<html>An OTP has been sent to:<br><b>" + userEmail + "</b></html>",
                SwingConstants.CENTER
        );
        lblInfo.setFont(UIStyle.FONT_SMALL);
        lblInfo.setForeground(UIStyle.TEXT_LIGHT);
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        main.add(lblTitle);
        main.add(Box.createVerticalStrut(10));
        main.add(lblInfo);
        main.add(Box.createVerticalStrut(20));

        txtOtp = new JTextField();
        txtOtp.setFont(UIStyle.FONT_BODY);
        txtOtp.setHorizontalAlignment(JTextField.CENTER);
        txtOtp.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        txtOtp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        main.add(txtOtp);
        main.add(Box.createVerticalStrut(20));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setOpaque(false);

        btnVerify = new JButton("Verify");
        btnVerify.setFont(UIStyle.getFont(Font.BOLD, 13));
        btnVerify.setForeground(Color.WHITE);
        btnVerify.setBackground(UIStyle.PRIMARY);
        btnVerify.setBorderPainted(false);
        btnVerify.setFocusPainted(false);
        btnVerify.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerify.setPreferredSize(new Dimension(110, UIStyle.BUTTON_HEIGHT));
        btnVerify.addActionListener(this::verifyAction);

        btnCancel = new JButton("Cancel");
        btnCancel.setFont(UIStyle.getFont(Font.BOLD, 13));
        btnCancel.setForeground(UIStyle.TEXT_DARK);
        btnCancel.setBackground(UIStyle.SEPARATOR);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setPreferredSize(new Dimension(110, UIStyle.BUTTON_HEIGHT));
        btnCancel.addActionListener(e -> cancelAction());

        btnPanel.add(btnVerify);
        btnPanel.add(btnCancel);

        main.add(btnPanel);

        add(main, BorderLayout.CENTER);
    }

    private void verifyAction(ActionEvent e) {
        String enteredOtp = txtOtp.getText().trim();

        if (enteredOtp.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the OTP sent to your email.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "verifying OTP");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "SELECT otp, otpExpiry FROM appuser WHERE email=?"
            );
            ps.setString(1, userEmail);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "No user found for this email.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String dbOtp = rs.getString("otp");
            Timestamp ts = rs.getTimestamp("otpExpiry");
            LocalDateTime expiry = (ts != null) ? ts.toLocalDateTime() : null;

            if (dbOtp == null || expiry == null) {
                JOptionPane.showMessageDialog(this,
                        "No active OTP found. Please login again.",
                        "Invalid OTP", JOptionPane.ERROR_MESSAGE);
                dispose();
                new login().setVisible(true);
                return;
            }

            if (LocalDateTime.now().isAfter(expiry)) {
                // Expired → clear OTP
                PreparedStatement psClear = con.prepareStatement(
                        "UPDATE appuser SET otp=NULL, otpExpiry=NULL WHERE email=?");
                psClear.setString(1, userEmail);
                psClear.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "OTP has expired. Please login again to receive a new OTP.",
                        "OTP Expired", JOptionPane.ERROR_MESSAGE);
                dispose();
                new login().setVisible(true);
                return;
            }

            if (!enteredOtp.equals(dbOtp)) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect OTP. Please try again.",
                        "Invalid OTP", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // OTP correct → clear OTP + open Home
            PreparedStatement psClear = con.prepareStatement(
                    "UPDATE appuser SET otp=NULL, otpExpiry=NULL WHERE email=?");
            psClear.setString(1, userEmail);
            psClear.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "OTP verified successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();
            new Home(userRole, userEmail).setVisible(true);

        } catch (Exception ex) {
            ErrorHandler.showError(ex, "verifying OTP");
        }
    }

    private void cancelAction() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel login and go back to the login screen?",
                "Cancel OTP", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new login().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new OTPVerification("demo@example.com", "Admin"));
    }
}
