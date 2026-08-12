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
 * Modernized Category management window.
 * 
 * PRESERVED METHODS (DO NOT MODIFY):
 * - loadTable() - SQL query preserved
 * - saveCategory() - SQL INSERT preserved
 * - updateCategory() - SQL UPDATE preserved
 * - resetForm() - Form reset logic preserved
 * - validateFields() - Validation logic preserved
 * - tableCategory mouse click handler - Selection logic preserved
 * - txtName field name preserved
 * - btnSave, btnUpdate, btnReset, btnClose button names preserved
 * - categoryPk variable name preserved
 */
public class Category extends JFrame {
    
    private JTextField txtName;
    private JButton btnSave, btnUpdate, btnReset, btnClose, btnDelete;
    private JTable tableCategory;
    private int categoryPk = 0;
    
    public Category() {
        super("Manage Category");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Changed from EXIT_ON_CLOSE
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
        
        JLabel title = new JLabel("Manage Category");
        title.setFont(UIStyle.FONT_HEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);
        
        add(topBar, BorderLayout.NORTH);
    }
    
    /**
     * Creates the main content area with table and form below.
     */
    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Table panel
        createTablePanel(mainPanel);
        
        // Form panel below table
        createFormPanel(mainPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the table panel.
     */
    private void createTablePanel(JPanel parent) {
        RoundedPanel tableCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        tableCard.setBackground(UIStyle.CARD_BG);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        // Table header
        JLabel tableTitle = new JLabel("Categories");
        tableTitle.setFont(UIStyle.FONT_SUBHEADING);
        tableTitle.setForeground(UIStyle.TEXT_DARK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);
        
        // Table
        tableCategory = new JTable(new DefaultTableModel(new Object[]{"ID", "Name"}, 0));
        tableCategory.setFont(UIStyle.FONT_BODY);
        tableCategory.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableCategory.setShowGrid(false);
        tableCategory.setIntercellSpacing(new Dimension(0, 0));
        tableCategory.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableCategory.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableCategory.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableCategory.getTableHeader().setForeground(Color.WHITE);
        tableCategory.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // PRESERVED: Mouse click handler for table selection
        tableCategory.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = tableCategory.getSelectedRow();
                if (index >= 0) {
                    TableModel model = tableCategory.getModel();
                    categoryPk = Integer.parseInt(model.getValueAt(index, 0).toString());
                    txtName.setText(model.getValueAt(index, 1).toString());
                    btnSave.setEnabled(false);
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                }
            }
        });
        
        // Wrap scroll pane in panel with visible gap - using colored border for visibility
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(true);
        scrollWrapper.setBackground(UIStyle.CARD_BG);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane scroll = new JScrollPane(tableCategory);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        scroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        scrollWrapper.add(scroll, BorderLayout.CENTER);
        tableCard.add(scrollWrapper, BorderLayout.CENTER);
        
        parent.add(tableCard, BorderLayout.CENTER);
    }
    
    /**
     * Creates the form panel below the table with name field and buttons.
     */
    private void createFormPanel(JPanel parent) {
        RoundedPanel formCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        formCard.setBackground(UIStyle.CARD_BG);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        formPanel.setOpaque(false);
        
        // Name field - removed asterisk
        JLabel lblName = new JLabel("Name");
        lblName.setFont(UIStyle.FONT_BODY_BOLD);
        lblName.setForeground(UIStyle.TEXT_DARK);
        formPanel.add(lblName);
        
        txtName = new JTextField(20); // PRESERVED FIELD NAME
        txtName.setFont(UIStyle.FONT_BODY);
        txtName.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtName.setPreferredSize(new Dimension(200, UIStyle.INPUT_HEIGHT));
        formPanel.add(txtName);
        
        // Buttons
        btnSave = createActionButton("Add", UIStyle.PRIMARY, e -> saveCategory()); // PRESERVED ACTION
        btnUpdate = createActionButton("Update", UIStyle.PRIMARY, e -> updateCategory()); // PRESERVED ACTION
        btnUpdate.setEnabled(false);
        btnDelete = createActionButton("Delete", UIStyle.ERROR, e -> deleteCategory());
        btnDelete.setEnabled(false);
        btnReset = createActionButton("Reset", UIStyle.PRIMARY, e -> resetForm()); // PRESERVED ACTION
        btnClose = createActionButton("Close", UIStyle.PRIMARY, e -> dispose());

        formPanel.add(btnSave);
        formPanel.add(btnUpdate);
        formPanel.add(btnDelete);
        formPanel.add(btnReset);
        formPanel.add(btnClose);
        
        formCard.add(formPanel, BorderLayout.CENTER);
        
        parent.add(formCard, BorderLayout.SOUTH);
    }
    
    /**
     * Creates a styled action button.
     */
    private JButton createActionButton(String text, Color bgColor, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyle.getFont(Font.BOLD, 13)); // Bold text
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor == UIStyle.ERROR ? UIStyle.ERROR : UIStyle.PRIMARY);
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
        return txtName.getText().trim().isEmpty();
    }

    /**
     * Checks whether a category with this name already exists, ignoring case
     * and surrounding whitespace, so "Sports", "sports", "SPORTS" etc. are
     * treated as the same category. excludePk lets updateCategory() skip the
     * row being edited (so renaming "Sports" to "sports" on itself is fine).
     */
    private boolean categoryNameExists(String name, int excludePk) {
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM category WHERE LOWER(TRIM(name)) = LOWER(TRIM(?)) AND category_pk <> ?");
            ps.setString(1, name);
            ps.setInt(2, excludePk);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            ErrorHandler.showError(e, "checking for duplicate category");
            return false;
        }
    }

    /**
     * PRESERVED METHOD - Do not modify SQL query.
     */
    private void loadTable() {
        DefaultTableModel model = (DefaultTableModel) tableCategory.getModel();
        model.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading categories");
                return;
            }
            Statement st = con.createStatement();
            // PRESERVED SQL QUERY
            ResultSet rs = st.executeQuery("SELECT * FROM category");
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("category_pk"), rs.getString("name")});
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading categories");
        }
        btnUpdate.setEnabled(false);
        btnSave.setEnabled(true);
        btnDelete.setEnabled(false);
    }

    /**
     * PRESERVED METHOD - Do not modify SQL INSERT statement.
     */
    private void saveCategory() {
        if (validateFields()) {
            JOptionPane.showMessageDialog(null, "Name field is required",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (categoryNameExists(txtName.getText().trim(), 0)) {
            JOptionPane.showMessageDialog(null,
                "A category named \"" + txtName.getText().trim() + "\" already exists (category names are case-insensitive).",
                "Duplicate Category", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "saving category");
                return;
            }
            // PRESERVED SQL QUERY
            PreparedStatement ps = con.prepareStatement("INSERT INTO category (name) VALUES (?)");
            ps.setString(1, txtName.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Category added successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "saving category");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL UPDATE statement.
     */
    private void updateCategory() {
        if (validateFields()) {
            JOptionPane.showMessageDialog(null, "Name field is required",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (categoryNameExists(txtName.getText().trim(), categoryPk)) {
            JOptionPane.showMessageDialog(null,
                "A category named \"" + txtName.getText().trim() + "\" already exists (category names are case-insensitive).",
                "Duplicate Category", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "updating category");
                return;
            }
            // PRESERVED SQL QUERY
            PreparedStatement ps = con.prepareStatement("UPDATE category SET name=? WHERE category_pk=?");
            ps.setString(1, txtName.getText().trim());
            ps.setInt(2, categoryPk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Category updated successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "updating category");
        }
    }
    
    /**
     * Deletes the currently selected category after confirmation. Any
     * product in this category isn't deleted -- product.category_fk just
     * gets cleared back to NULL (ON DELETE SET NULL), so it survives as
     * "uncategorized" instead of vanishing.
     */
    private void deleteCategory() {
        if (categoryPk == 0) {
            JOptionPane.showMessageDialog(this, "Select a category first.", "No Category Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + txtName.getText() + "\"? Any products in this category will become uncategorized.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "deleting category");
                return;
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM category WHERE category_pk=?");
            ps.setInt(1, categoryPk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Category deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "deleting category");
        }
    }

    /**
     * PRESERVED METHOD - Do not modify reset logic.
     */
    private void resetForm() {
        txtName.setText("");
        categoryPk = 0;
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        tableCategory.clearSelection();
        loadTable();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Category::new);
    }
}
