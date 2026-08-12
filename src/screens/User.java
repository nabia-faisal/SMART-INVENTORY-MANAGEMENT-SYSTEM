package screens;
import screens.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import dao.ConnectionProvider;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ModernTableRenderer;
import ui.ErrorHandler;

import org.mindrot.jbcrypt.BCrypt;

/**
 * USER MANAGEMENT —
 * - Shows ALL users (SuperAdmin, Admin, Customer)
 * - Only SuperAdmin can add/update/reactivate
 * - SuperAdmin accounts are protected
 */
public class User extends JFrame {

    private int appuserPk = 0;
    private String selectedUserRole = "";
    private String currentUserRole;

    private JTextField txtName, txtMobileNumber, txtEmail, txtAddress, txtPassword;
    private JComboBox<String> ComboBoxStatus;
    private JComboBox<String> ComboBoxRole;
    private JTable tableUser;
    private JButton btnSave, btnUpdate, btnReset, btnClose, btnReactivate, btnDelete;

    public User() { this("Admin"); }

    public User(String role) {
        super("Manage Users");
        this.currentUserRole = (role == null) ? "Admin" : role;

        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UIStyle.BACKGROUND);

        createTopBar();
        createMainContent();

        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
                loadUsers();
            }
        });

        setVisible(true);
    }

    private boolean isSuperAdmin() {
        return "SuperAdmin".equalsIgnoreCase(currentUserRole);
    }

    // ───────────────────────── TOP BAR ─────────────────────────
    private void createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyle.BACKGROUND);
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Manage Users");
        title.setFont(UIStyle.FONT_HEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);

        JLabel roleLabel = new JLabel("Logged in as: " + currentUserRole);
        roleLabel.setFont(UIStyle.FONT_SMALL);
        roleLabel.setForeground(UIStyle.TEXT_LIGHT);
        topBar.add(roleLabel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
    }

    // ───────────────────────── MAIN CONTENT ─────────────────────────
    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        createTablePanel(mainPanel);
        createFormPanel(mainPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ───────────────────────── TABLE PANEL ─────────────────────────
    private void createTablePanel(JPanel parent) {

        RoundedPanel card = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        card.setBackground(UIStyle.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lbl = new JLabel("All Users");
        lbl.setFont(UIStyle.FONT_SUBHEADING);
        lbl.setForeground(UIStyle.TEXT_DARK);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(lbl, BorderLayout.NORTH);

        tableUser = new JTable(new DefaultTableModel(
                new Object[]{"ID", "Role", "Name", "Mobile", "Email", "Address", "Status", "Failed Attempts"}, 0
        ));
        tableUser.setFont(UIStyle.FONT_BODY);
        tableUser.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableUser.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableUser.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableUser.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableUser.getTableHeader().setForeground(Color.WHITE);

        tableUser.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) { selectUser(); }
        });

        JScrollPane scroll = new JScrollPane(tableUser);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttons.setOpaque(false);

        btnSave = createBtn("Add", e -> saveUser());
        btnUpdate = createBtn("Update", e -> updateUser());
        btnReactivate = createBtn("Reactivate", e -> reactivateUser());
        btnDelete = createBtn("Delete", e -> deleteUser());
        btnDelete.setBackground(UIStyle.ERROR);
        btnReset = createBtn("Reset", e -> resetForm());
        btnClose = createBtn("Close", e -> dispose());

        buttons.add(btnSave);
        buttons.add(btnUpdate);
        buttons.add(btnReactivate);
        buttons.add(btnDelete);
        buttons.add(btnReset);
        buttons.add(btnClose);

        if (!isSuperAdmin()) {
            btnSave.setEnabled(false);
            btnUpdate.setEnabled(false);
            btnReactivate.setEnabled(false);
            btnDelete.setEnabled(false);
        }

        card.add(buttons, BorderLayout.SOUTH);
        parent.add(card, BorderLayout.CENTER);
    }

    private JButton createBtn(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.setFont(UIStyle.FONT_BODY_BOLD);
        b.setBackground(UIStyle.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(listener);
        return b;
    }

    // ───────────────────────── FORM PANEL ─────────────────────────
    private void createFormPanel(JPanel parent) {
        RoundedPanel form = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        form.setBackground(UIStyle.CARD_BG);
        form.setLayout(new BorderLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        form.setPreferredSize(new Dimension(350, 0));

        JLabel lbl = new JLabel("User Details");
        lbl.setFont(UIStyle.FONT_SUBHEADING);
        lbl.setForeground(UIStyle.TEXT_DARK);
        form.add(lbl, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));

        txtName = addField(fields, "Name");
        txtMobileNumber = addField(fields, "Mobile");
        txtEmail = addField(fields, "Email");
        txtAddress = addField(fields, "Address");
        txtPassword = addField(fields, "Password");

        // Role picker -- used only when adding a new user (saveUser()).
        // Existing users' roles aren't editable here; SuperAdmin accounts
        // stay protected via the checks in updateUser().
        JLabel roleLbl = new JLabel("Role (for new user)");
        roleLbl.setFont(UIStyle.FONT_BODY_BOLD);
        roleLbl.setForeground(UIStyle.TEXT_DARK);
        fields.add(roleLbl);
        ComboBoxRole = new JComboBox<>(new String[]{"Admin", "Customer", "SuperAdmin"});
        ComboBoxRole.setFont(UIStyle.FONT_BODY);
        ComboBoxRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        fields.add(ComboBoxRole);
        fields.add(Box.createVerticalStrut(10));

        ComboBoxStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        ComboBoxStatus.setFont(UIStyle.FONT_BODY);
        ComboBoxStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        fields.add(new JLabel("Status"));
        fields.add(ComboBoxStatus);
        fields.add(Box.createVerticalStrut(10));

        // Wrapped in a scroll pane so nothing gets clipped on smaller windows.
        JScrollPane formScroll = new JScrollPane(fields);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        form.add(formScroll, BorderLayout.CENTER);
        parent.add(form, BorderLayout.EAST);
    }

    private JTextField addField(JPanel p, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIStyle.FONT_BODY_BOLD);
        lbl.setForeground(UIStyle.TEXT_DARK);
        p.add(lbl);

        JTextField t = new JTextField();
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        p.add(t);
        p.add(Box.createVerticalStrut(10));

        return t;
    }

    // ───────────────────────── LOAD USERS ─────────────────────────
    private void loadUsers() {
        try {
            DefaultTableModel model = (DefaultTableModel) tableUser.getModel();
            model.setRowCount(0);

            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT appuser_pk, userRole, name, mobileNumber, email, address, status, " +
                            "COALESCE(failedAttempts, 0) AS attempts FROM appuser ORDER BY userRole, name"
            );

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6),
                        rs.getString(7), rs.getInt(8)
                });
            }

        } catch (Exception e) { ErrorHandler.showError(e, "Loading users"); }
    }

    // ───────────────────────── SELECT USER ─────────────────────────
    private void selectUser() {
        int idx = tableUser.getSelectedRow();
        if (idx < 0) return;

        TableModel m = tableUser.getModel();
        appuserPk = (int) m.getValueAt(idx, 0);
        selectedUserRole = m.getValueAt(idx, 1).toString();

        txtName.setText(m.getValueAt(idx, 2).toString());
        txtMobileNumber.setText(m.getValueAt(idx, 3).toString());
        txtEmail.setText(m.getValueAt(idx, 4).toString());
        txtAddress.setText(m.getValueAt(idx, 5).toString());
        ComboBoxStatus.setSelectedItem(m.getValueAt(idx, 6));

        txtPassword.setText("");
        txtPassword.setEditable(false);

        btnUpdate.setEnabled(isSuperAdmin() && !selectedUserRole.equalsIgnoreCase("SuperAdmin"));
        btnReactivate.setEnabled(isSuperAdmin());
        btnDelete.setEnabled(isSuperAdmin() && !selectedUserRole.equalsIgnoreCase("SuperAdmin"));
    }

    // ───────────────────────── SAVE USER ─────────────────────────
    private void saveUser() {
        if (!isSuperAdmin()) {
            JOptionPane.showMessageDialog(this, "Only SuperAdmin can add users.");
            return;
        }

        if (txtName.getText().isEmpty() ||
                txtMobileNumber.getText().isEmpty() ||
                txtEmail.getText().isEmpty() ||
                txtAddress.getText().isEmpty() ||
                txtPassword.getText().isEmpty()) {

            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        try {
            Connection con = ConnectionProvider.getCon();

            String hashedPw = BCrypt.hashpw(txtPassword.getText(), BCrypt.gensalt());
            String selectedRole = (String) ComboBoxRole.getSelectedItem();

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO appuser (userRole, name, mobileNumber, email, address, password, status, failedAttempts) " +
                            "VALUES(?,?,?,?,?,?, 'Active', 0)"
            );

            ps.setString(1, selectedRole);
            ps.setString(2, txtName.getText());
            ps.setString(3, txtMobileNumber.getText());
            ps.setString(4, txtEmail.getText());
            ps.setString(5, txtAddress.getText());
            ps.setString(6, hashedPw);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "User added successfully.");

            resetForm();

        } catch (Exception e) {
            ErrorHandler.showError(e, "Saving user");
        }
    }

    // ───────────────────────── UPDATE USER ─────────────────────────
    private void updateUser() {
        if (!isSuperAdmin()) {
            JOptionPane.showMessageDialog(this, "Only SuperAdmin can update users.");
            return;
        }
        if (appuserPk == 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        if ("SuperAdmin".equalsIgnoreCase(selectedUserRole)) {
            JOptionPane.showMessageDialog(this, "SuperAdmin accounts are protected.");
            return;
        }

        try {
            Connection con = ConnectionProvider.getCon();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE appuser SET name=?, mobileNumber=?, email=?, address=?, status=? WHERE appuser_pk=?");

            ps.setString(1, txtName.getText());
            ps.setString(2, txtMobileNumber.getText());
            ps.setString(3, txtEmail.getText());
            ps.setString(4, txtAddress.getText());
            ps.setString(5, ComboBoxStatus.getSelectedItem().toString());
            ps.setInt(6, appuserPk);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User updated.");
            resetForm();

        } catch (Exception e) { ErrorHandler.showError(e, "Updating user"); }
    }

    // ───────────────────────── REACTIVATE USER ─────────────────────────
    private void reactivateUser() {
        if (!isSuperAdmin()) {
            JOptionPane.showMessageDialog(this, "Only SuperAdmin can reactivate users.");
            return;
        }
        if (appuserPk == 0) { JOptionPane.showMessageDialog(this, "Select user first."); return; }

        try {
            Connection con = ConnectionProvider.getCon();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE appuser SET status='Active', failedAttempts=0 WHERE appuser_pk=?");

            ps.setInt(1, appuserPk);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User reactivated.");
            resetForm();

        } catch (Exception e) { ErrorHandler.showError(e, "Reactivation"); }
    }

    // ───────────────────────── DELETE USER ─────────────────────────
    private void deleteUser() {
        if (!isSuperAdmin()) {
            JOptionPane.showMessageDialog(this, "Only SuperAdmin can delete users.");
            return;
        }
        if (appuserPk == 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        if ("SuperAdmin".equalsIgnoreCase(selectedUserRole)) {
            JOptionPane.showMessageDialog(this, "SuperAdmin accounts are protected.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete \"" + txtName.getText() + "\"'s account? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("DELETE FROM appuser WHERE appuser_pk=?");
            ps.setInt(1, appuserPk);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User deleted.");
            resetForm();

        } catch (Exception e) { ErrorHandler.showError(e, "Deleting user"); }
    }

    // ───────────────────────── RESET FORM ─────────────────────────
    private void resetForm() {
        txtName.setText("");
        txtMobileNumber.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtPassword.setText("");

        txtPassword.setEditable(true);
        ComboBoxStatus.setSelectedIndex(0);
        ComboBoxRole.setSelectedIndex(0);
        appuserPk = 0;

        tableUser.clearSelection();
        loadUsers();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new User("SuperAdmin"));
    }
}
