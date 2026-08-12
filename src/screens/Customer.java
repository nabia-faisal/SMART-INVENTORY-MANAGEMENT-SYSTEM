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

/**
 * Modernized Customer management window.
 * 
 * PRESERVED METHODS (DO NOT MODIFY):
 * - loadTable() - SQL query preserved
 * - saveCustomer() - SQL INSERT preserved
 * - updateCustomer() - SQL UPDATE preserved
 * - resetForm() - Form reset logic preserved
 * - validateFields() - Validation logic preserved
 * - tableCustomer mouse click handler - Selection logic preserved
 * - txtName, txtMobileNumber, txtEmail field names preserved
 * - btnSave, btnUpdate, btnReset, btnClose button names preserved
 * - customerPk variable name preserved
 */
public class Customer extends JFrame {

    private JTextField txtName, txtMobileNumber, txtEmail;
    private JButton btnSave, btnUpdate, btnReset, btnClose, btnDelete;
    private JTable tableCustomer;
    private int customerPk = 0;
    private String currentUserRole;

    public Customer() { this("Admin"); }

    public Customer(String role) {
        super("Manage Customer");
        this.currentUserRole = (role == null) ? "Admin" : role;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BACKGROUND);
        
        // Top bar
        createTopBar();
        
        // Main content
        createMainContent();
        
        // Load data when shown
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                loadTable(); // PRESERVED METHOD CALL
            }
        });
        
        setVisible(true);
    }

    private boolean isSuperAdmin() {
        return "SuperAdmin".equalsIgnoreCase(currentUserRole);
    }

    /**
     * Creates the top bar with title.
     */
    private void createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyle.BACKGROUND);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.SEPARATOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel title = new JLabel("Manage Customer");
        title.setFont(UIStyle.FONT_HEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);
        
        add(topBar, BorderLayout.NORTH);
    }
    
    /**
     * Creates the main content area with table and form.
     */
    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Left: Table (70%)
        createTablePanel(mainPanel);
        
        // Right: Form (30%)
        createFormPanel(mainPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the table panel with action buttons at the bottom.
     */
    private void createTablePanel(JPanel parent) {
        RoundedPanel tableCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        tableCard.setBackground(UIStyle.CARD_BG);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        // Table header
        JLabel tableTitle = new JLabel("Customers");
        tableTitle.setFont(UIStyle.FONT_SUBHEADING);
        tableTitle.setForeground(UIStyle.TEXT_DARK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);
        
        // Table
        tableCustomer = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Mobile Number", "Email"}, 0));
        tableCustomer.setFont(UIStyle.FONT_BODY);
        tableCustomer.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableCustomer.setShowGrid(false);
        tableCustomer.setIntercellSpacing(new Dimension(0, 0));
        tableCustomer.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableCustomer.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableCustomer.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableCustomer.getTableHeader().setForeground(Color.WHITE);
        tableCustomer.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // PRESERVED: Mouse click handler for table selection
        tableCustomer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = tableCustomer.getSelectedRow();
                if (index >= 0) {
                    TableModel model = tableCustomer.getModel();
                    customerPk = Integer.parseInt(model.getValueAt(index, 0).toString());
                    txtName.setText(model.getValueAt(index, 1).toString());
                    txtMobileNumber.setText(model.getValueAt(index, 2).toString());
                    txtEmail.setText(model.getValueAt(index, 3).toString());
                    btnSave.setEnabled(false);
                    btnUpdate.setEnabled(isSuperAdmin());
                    btnDelete.setEnabled(isSuperAdmin());
                }
            }
        });
        
        // Wrap scroll pane in panel with visible gap - using colored border for visibility
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(true);
        scrollWrapper.setBackground(UIStyle.CARD_BG);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane scroll = new JScrollPane(tableCustomer);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        scroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        scrollWrapper.add(scroll, BorderLayout.CENTER);
        tableCard.add(scrollWrapper, BorderLayout.CENTER);
        
        // Action buttons at bottom of table
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        btnSave = createActionButton("Add", UIStyle.PRIMARY, e -> saveCustomer()); // PRESERVED ACTION
        btnUpdate = createActionButton("Update", UIStyle.PRIMARY, e -> updateCustomer()); // PRESERVED ACTION
        btnUpdate.setEnabled(false);
        btnDelete = createActionButton("Delete", UIStyle.ERROR, e -> deleteCustomer()); // Red button
        JButton btnExport = createActionButton("Export", UIStyle.PRIMARY, null);
        btnReset = createActionButton("Reset", UIStyle.PRIMARY, e -> resetForm()); // PRESERVED ACTION
        btnClose = createActionButton("Close", UIStyle.PRIMARY, e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnClose);

        // Customers are a manually-managed contact list: only SuperAdmin
        // may add, edit, or remove them.
        if (!isSuperAdmin()) {
            btnSave.setEnabled(false);
            btnUpdate.setEnabled(false);
            btnDelete.setEnabled(false);
        }
        
        tableCard.add(buttonPanel, BorderLayout.SOUTH);
        
        parent.add(tableCard, BorderLayout.CENTER);
    }
    
    /**
     * Creates the form panel on the right.
     */
    private void createFormPanel(JPanel parent) {
        RoundedPanel formCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        formCard.setBackground(UIStyle.CARD_BG);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        formCard.setPreferredSize(new Dimension(350, 0));
        
        JLabel formTitle = new JLabel("Customer Details");
        formTitle.setFont(UIStyle.FONT_SUBHEADING);
        formTitle.setForeground(UIStyle.TEXT_DARK);
        formTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        formCard.add(formTitle, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        
        // Name field - removed asterisk
        addFormField(formPanel, "Name", txtName = new JTextField());
        
        // Mobile Number field - removed asterisk
        addFormField(formPanel, "Mobile Number", txtMobileNumber = new JTextField());
        
        // Email field - removed asterisk
        addFormField(formPanel, "Email", txtEmail = new JTextField());
        
        formCard.add(formPanel, BorderLayout.CENTER);
        
        parent.add(formCard, BorderLayout.EAST);
    }
    
    /**
     * Helper to add a form field.
     */
    private void addFormField(JPanel parent, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIStyle.FONT_BODY_BOLD);
        lbl.setForeground(UIStyle.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(8));
        
        field.setFont(UIStyle.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(field);
        parent.add(Box.createVerticalStrut(15));
    }
    
    /**
     * Creates a styled action button.
     */
    private JButton createActionButton(String text, Color bgColor, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyle.getFont(Font.BOLD, 13)); // Bold text
        if (bgColor == UIStyle.ERROR) {
            btn.setForeground(Color.WHITE);
            btn.setBackground(UIStyle.ERROR); // Very dark red for delete
        } else {
            btn.setForeground(Color.WHITE);
            btn.setBackground(UIStyle.PRIMARY); // Very dark blue for all others
        }
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, UIStyle.BUTTON_HEIGHT));
        if (listener != null) {
            btn.addActionListener(listener);
        }
        return btn;
    }
    
    /**
     * PRESERVED METHOD - Do not modify validation logic.
     */
    private boolean validateFields() {
        return txtName.getText().trim().isEmpty() || txtMobileNumber.getText().trim().isEmpty() || 
               txtEmail.getText().trim().isEmpty();
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL query.
     */
    private void loadTable() {
        DefaultTableModel model = (DefaultTableModel) tableCustomer.getModel();
        model.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading customers");
                return;
            }
            Statement st = con.createStatement();
            // PRESERVED SQL QUERY
            ResultSet rs = st.executeQuery("SELECT * FROM customer");
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("customer_pk"), 
                    rs.getString("name"), 
                    rs.getString("mobileNumber"), 
                    rs.getString("email")
                });
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading customers");
        }
        btnUpdate.setEnabled(false);
        btnSave.setEnabled(isSuperAdmin());
    }

    /**
     * PRESERVED METHOD - Do not modify SQL INSERT statement.
     */
    private void saveCustomer() {
        if (!isSuperAdmin()) {
            JOptionPane.showMessageDialog(this, "Only SuperAdmin can add customers.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (validateFields()) {
            JOptionPane.showMessageDialog(null, "All fields are required",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "saving customer");
                return;
            }
            // PRESERVED SQL QUERY
            PreparedStatement ps = con.prepareStatement("INSERT INTO customer (name, mobileNumber, email) VALUES (?, ?, ?)");
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtMobileNumber.getText().trim());
            ps.setString(3, txtEmail.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Customer added successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "saving customer");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL UPDATE statement.
     */
    private void updateCustomer() {
        if (!isSuperAdmin()) {
            JOptionPane.showMessageDialog(this, "Only SuperAdmin can update customers.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (validateFields()) {
            JOptionPane.showMessageDialog(null, "All fields are required",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "updating customer");
                return;
            }
            // PRESERVED SQL QUERY
            PreparedStatement ps = con.prepareStatement("UPDATE customer SET name=?, mobileNumber=?, email=? WHERE customer_pk=?");
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtMobileNumber.getText().trim());
            ps.setString(3, txtEmail.getText().trim());
            ps.setInt(4, customerPk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Customer updated successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "updating customer");
        }
    }
    
    /**
     * Deletes the currently selected customer after confirmation. SuperAdmin
     * only, matching add/update.
     */
    private void deleteCustomer() {
        if (!isSuperAdmin()) {
            JOptionPane.showMessageDialog(this, "Only SuperAdmin can delete customers.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (customerPk == 0) {
            JOptionPane.showMessageDialog(this, "Select a customer first.", "No Customer Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + txtName.getText() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "deleting customer");
                return;
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM customer WHERE customer_pk=?");
            ps.setInt(1, customerPk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Customer deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "deleting customer");
        }
    }

    /**
     * PRESERVED METHOD - Do not modify reset logic.
     */
    private void resetForm() {
        txtName.setText("");
        txtMobileNumber.setText("");
        txtEmail.setText("");
        customerPk = 0;
        btnSave.setEnabled(isSuperAdmin());
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        tableCustomer.clearSelection();
        loadTable();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Customer::new);
    }
}
