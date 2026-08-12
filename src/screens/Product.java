package screens;
import screens.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import dao.ConnectionProvider;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ModernTableRenderer;
import ui.ErrorHandler;

/**
 * Modernized Product management window with two-column layout (70% table, 30% form).
 * Action buttons are placed at the bottom of the table.
 * 
 * PRESERVED METHODS (DO NOT MODIFY):
 * - loadCategories() - SQL query preserved
 * - loadProducts() - SQL query preserved
 * - saveProduct() - SQL INSERT preserved
 * - updateProduct() - SQL UPDATE preserved
 * - selectProduct() - Selection logic preserved
 * - loadCategoriesExceptCurrent() - SQL query preserved
 * - validateFields() - Validation logic preserved
 * - All field names preserved (txtName, txtQuantity, txtPrice, etc.)
 * - ComboBoxCategory name preserved
 * - btnSave, btnUpdate, btnReset, btnClose button names preserved
 * - productPk, totalQuantity variable names preserved
 */
public class Product extends JFrame {

private int productPk = 0;
private int totalQuantity = 0;

private JTextField txtName, txtQuantity, txtPrice, txtDescription;
private JComboBox<String> ComboBoxCategory;
// Location is now via normalized foreign keys instead of free-text Warehouse/Shelf/City
private JComboBox<String> ComboBoxShelf, ComboBoxSupplier;
private JTable tableProduct;
private JButton btnSave, btnUpdate, btnReset, btnClose, btnDelete;
// The side "Product Details" panel has its own Save/Update buttons, separate
// from the ones under the table. Both pairs must be kept in sync (enabled/
// disabled together) or clicking the side panel's "Save" while a product is
// selected silently inserts a brand-new row instead of updating the one you
// meant to edit -- which is exactly the "price update creates a new product"
// bug this fixes.
private JButton btnFormSave, btnFormUpdate;

public Product() {
    super("Manage Products");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Changed from EXIT_ON_CLOSE
        setSize(1400, 750);
    setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BACKGROUND);
        
        // Top bar
        createTopBar();
        
        // Main content with search/filter
        createMainContent();
        
        // Load data when shown
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
                loadCategories(); // PRESERVED METHOD CALL
                loadShelves();
                loadSuppliers();
                loadProducts(); // PRESERVED METHOD CALL
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

    JLabel title = new JLabel("Manage Products");
        title.setFont(UIStyle.FONT_HEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);
        
        add(topBar, BorderLayout.NORTH);
    }
    
    /**
     * Creates the main content area with table (70%) and form (30%).
     */
    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Left: Table panel (70%)
        createTablePanel(mainPanel);
        
        // Right: Form panel (30%)
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
        
        // Table header - removed filter
        JLabel tableTitle = new JLabel("Products");
        tableTitle.setFont(UIStyle.FONT_SUBHEADING);
        tableTitle.setForeground(UIStyle.TEXT_DARK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);
        
        // Table - simplified columns for better UX
        tableProduct = new JTable(new DefaultTableModel(
            new Object[]{"#", "Product Name", "SKU", "Category", "Price", "Stock", "Warehouse", "Shelf", "Supplier"}, 0
        ));
        tableProduct.setFont(UIStyle.FONT_BODY);
        tableProduct.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableProduct.setShowGrid(false);
        tableProduct.setIntercellSpacing(new Dimension(0, 0));
        tableProduct.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableProduct.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableProduct.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableProduct.getTableHeader().setForeground(Color.WHITE);
        tableProduct.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // PRESERVED: Mouse click handler
        tableProduct.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                selectProduct(); // PRESERVED METHOD CALL
            }
        });
        
        // Wrap scroll pane in panel with visible gap - using colored border for visibility
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(true);
        scrollWrapper.setBackground(UIStyle.CARD_BG);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane scroll = new JScrollPane(tableProduct);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        scroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        scrollWrapper.add(scroll, BorderLayout.CENTER);
        tableCard.add(scrollWrapper, BorderLayout.CENTER);
        
        // Action buttons at bottom of table - centered
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // Changed to CENTER
        buttonPanel.setOpaque(false);
        
        btnSave = createActionButton("Add New", UIStyle.PRIMARY, e -> saveProduct()); // PRESERVED ACTION
        JButton btnImport = createActionButton("Import", UIStyle.PRIMARY, null);
        JButton btnExport = createActionButton("Export", UIStyle.PRIMARY, null);
        JButton btnRefresh = createActionButton("Refresh", UIStyle.PRIMARY, e -> {
            loadProducts();
            loadCategories();
        });
        btnDelete = createActionButton("Delete", UIStyle.ERROR, e -> deleteProduct()); // Red button
        btnUpdate = createActionButton("Update", UIStyle.PRIMARY, e -> updateProduct()); // PRESERVED ACTION
        btnUpdate.setEnabled(false);
        btnUpdate.setForeground(Color.WHITE); // Ensure white text on update button
        btnReset = createActionButton("Reset", UIStyle.PRIMARY, e -> resetForm()); // PRESERVED ACTION
        btnClose = createActionButton("Close", UIStyle.PRIMARY, e -> dispose()); // Changed to dispose
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnImport);
        buttonPanel.add(btnExport);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnClose);
        
        tableCard.add(buttonPanel, BorderLayout.SOUTH);
        
        parent.add(tableCard, BorderLayout.CENTER);
    }
    
    /**
     * Creates the form panel on the right (30%).
     */
    private void createFormPanel(JPanel parent) {
        RoundedPanel formCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        formCard.setBackground(UIStyle.CARD_BG);
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        formCard.setPreferredSize(new Dimension(400, 0));
        
        JLabel formTitle = new JLabel("Product Details");
        formTitle.setFont(UIStyle.FONT_SUBHEADING);
        formTitle.setForeground(UIStyle.TEXT_DARK);
        formTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        formCard.add(formTitle, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        
        // Product Name - removed asterisk
        addFormField(formPanel, "Product Name", txtName = new JTextField());
        
        // Category - removed asterisk
        JLabel lblCategory = new JLabel("Category");
        lblCategory.setFont(UIStyle.FONT_BODY_BOLD);
        lblCategory.setForeground(UIStyle.TEXT_DARK);
        lblCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblCategory);
        formPanel.add(Box.createVerticalStrut(8));
        
        ComboBoxCategory = new JComboBox<>(); // PRESERVED FIELD NAME
        ComboBoxCategory.setFont(UIStyle.FONT_BODY);
        ComboBoxCategory.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        ComboBoxCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        ComboBoxCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(ComboBoxCategory);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Price and Quantity in a row - increased width
        JPanel priceQtyPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        priceQtyPanel.setOpaque(false);
        priceQtyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        priceQtyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
        pricePanel.setOpaque(false);
        JLabel lblPrice = new JLabel("Price"); // Removed asterisk
        lblPrice.setFont(UIStyle.FONT_BODY_BOLD);
        lblPrice.setForeground(UIStyle.TEXT_DARK);
        pricePanel.add(lblPrice);
        pricePanel.add(Box.createVerticalStrut(5));
        txtPrice = new JTextField(); // PRESERVED FIELD NAME
        txtPrice.setFont(UIStyle.FONT_BODY);
        txtPrice.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        pricePanel.add(txtPrice);
        
        JPanel qtyPanel = new JPanel();
        qtyPanel.setLayout(new BoxLayout(qtyPanel, BoxLayout.Y_AXIS));
        qtyPanel.setOpaque(false);
        JLabel lblQty = new JLabel("Stock"); // Removed asterisk
        lblQty.setFont(UIStyle.FONT_BODY_BOLD);
        lblQty.setForeground(UIStyle.TEXT_DARK);
        qtyPanel.add(lblQty);
        qtyPanel.add(Box.createVerticalStrut(5));
        txtQuantity = new JTextField(); // PRESERVED FIELD NAME
        txtQuantity.setFont(UIStyle.FONT_BODY);
        txtQuantity.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtQuantity.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        qtyPanel.add(txtQuantity);
        
        priceQtyPanel.add(pricePanel);
        priceQtyPanel.add(qtyPanel);
        formPanel.add(priceQtyPanel);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Description - removed asterisk
        JLabel lblDesc = new JLabel("Description");
        lblDesc.setFont(UIStyle.FONT_BODY_BOLD);
        lblDesc.setForeground(UIStyle.TEXT_DARK);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblDesc);
        formPanel.add(Box.createVerticalStrut(8));
        
        txtDescription = new JTextField(); // PRESERVED FIELD NAME
        txtDescription.setFont(UIStyle.FONT_BODY);
        txtDescription.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtDescription.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        txtDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(txtDescription);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Location fields - shelf (which implies warehouse) + supplier, via FK dropdowns
        JLabel lblLocation = new JLabel("Location & Supplier");
        lblLocation.setFont(UIStyle.FONT_BODY_BOLD);
        lblLocation.setForeground(UIStyle.TEXT_DARK);
        lblLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblLocation);
        formPanel.add(Box.createVerticalStrut(8));

        JLabel lblShelf = new JLabel("Shelf (Warehouse)");
        lblShelf.setFont(UIStyle.FONT_BODY_BOLD);
        lblShelf.setForeground(UIStyle.TEXT_DARK);
        lblShelf.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblShelf);
        formPanel.add(Box.createVerticalStrut(8));

        ComboBoxShelf = new JComboBox<>();
        ComboBoxShelf.setFont(UIStyle.FONT_BODY);
        ComboBoxShelf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        ComboBoxShelf.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        ComboBoxShelf.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(ComboBoxShelf);
        formPanel.add(Box.createVerticalStrut(15));

        JLabel lblSupplier = new JLabel("Supplier");
        lblSupplier.setFont(UIStyle.FONT_BODY_BOLD);
        lblSupplier.setForeground(UIStyle.TEXT_DARK);
        lblSupplier.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblSupplier);
        formPanel.add(Box.createVerticalStrut(8));

        ComboBoxSupplier = new JComboBox<>();
        ComboBoxSupplier.setFont(UIStyle.FONT_BODY);
        ComboBoxSupplier.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        ComboBoxSupplier.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        ComboBoxSupplier.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(ComboBoxSupplier);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Image upload button (placeholder) - removed emoji
        JButton btnUploadImage = new JButton("Upload Image");
        btnUploadImage.setFont(UIStyle.FONT_BODY);
        btnUploadImage.setForeground(UIStyle.TEXT_DARK);
        btnUploadImage.setBackground(UIStyle.SEPARATOR);
        btnUploadImage.setBorderPainted(false);
        btnUploadImage.setFocusPainted(false);
        btnUploadImage.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUploadImage.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.BUTTON_HEIGHT));
        btnUploadImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(btnUploadImage);
        
        // Wrap form panel in scroll pane with gap - using colored border for visibility
        JPanel formScrollWrapper = new JPanel(new BorderLayout());
        formScrollWrapper.setOpaque(true);
        formScrollWrapper.setBackground(UIStyle.CARD_BG);
        formScrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        formScroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        formScrollWrapper.add(formScroll, BorderLayout.CENTER);
        formCard.add(formScrollWrapper, BorderLayout.CENTER);
        
        // Form action buttons
        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        formButtonPanel.setOpaque(false);
        formButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        btnFormSave = createActionButton("Save", UIStyle.PRIMARY, e -> saveProduct());
        btnFormUpdate = createActionButton("Update", UIStyle.PRIMARY, e -> updateProduct());
        btnFormUpdate.setEnabled(false);
        btnFormUpdate.setForeground(Color.WHITE); // Ensure white text on update button
        JButton btnFormClear = createActionButton("Clear", UIStyle.PRIMARY, e -> clearForm());
        
        formButtonPanel.add(btnFormSave);
        formButtonPanel.add(btnFormUpdate);
        formButtonPanel.add(btnFormClear);
        
        formCard.add(formButtonPanel, BorderLayout.SOUTH);
        
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
        // All buttons except delete have white text on very dark blue background
        if (bgColor == UIStyle.ERROR) {
            btn.setForeground(Color.WHITE); // White text on dark red
            btn.setBackground(UIStyle.ERROR); // Very dark red for delete
        } else {
            btn.setForeground(Color.WHITE);
            btn.setBackground(UIStyle.PRIMARY); // Very dark blue for all others
        }
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, UIStyle.BUTTON_HEIGHT));
        if (listener != null) {
            btn.addActionListener(listener);
        }
        return btn;
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL query.
     */
private void loadCategories() {
    try {
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading categories");
                return;
            }
        Statement st = con.createStatement();
            // PRESERVED SQL QUERY
        ResultSet rs = st.executeQuery("SELECT * FROM category");
        ComboBoxCategory.removeAllItems();
            while(rs.next()) {
                ComboBoxCategory.addItem(rs.getInt("category_pk")+" - "+rs.getString("name"));
            }
        } catch(Exception e) {
            ErrorHandler.showError(e, "loading categories");
        }
    }

    /**
     * Loads shelf options as "shelf_pk - code (Warehouse name)" so a product's
     * location is picked via FK instead of typed as free text.
     */
    private void loadShelves() {
        try {
            Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading shelves");
                return;
            }
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT shelf.shelf_pk, shelf.code, warehouse.name AS warehouseName " +
                "FROM shelf INNER JOIN warehouse ON shelf.warehouse_fk = warehouse.warehouse_pk " +
                "ORDER BY warehouse.name, shelf.code");
            ComboBoxShelf.removeAllItems();
            while (rs.next()) {
                ComboBoxShelf.addItem(rs.getInt("shelf_pk") + " - " + rs.getString("code") +
                        " (" + rs.getString("warehouseName") + ")");
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading shelves");
        }
    }

    /**
     * Loads supplier options as "supplier_pk - name".
     */
    private void loadSuppliers() {
        try {
            Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading suppliers");
                return;
            }
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT supplier_pk, name FROM supplier ORDER BY name");
            ComboBoxSupplier.removeAllItems();
            while (rs.next()) {
                ComboBoxSupplier.addItem(rs.getInt("supplier_pk") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading suppliers");
        }
    }

    /**
     * Selects the combo box item whose "id - ..." prefix matches the given id.
     * Returns without changing selection if not found (e.g. id is 0/unset).
     */
    private void selectComboItemById(JComboBox<String> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.startsWith(id + " - ")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Note: Table columns simplified for UI, but full data still loaded internally.
     * Query now joins shelf/warehouse/supplier so location info can be shown too.
     */
private void loadProducts() {
    try {
        DefaultTableModel model = (DefaultTableModel) tableProduct.getModel();
        model.setRowCount(0);
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading products");
                return;
            }
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT product.*, category.name AS categoryName, " +
            "shelf.code AS shelfCode, warehouse.name AS warehouseName, supplier.name AS supplierName " +
            "FROM product " +
            "INNER JOIN category ON product.category_fk = category.category_pk " +
            "LEFT JOIN shelf ON product.shelf_fk = shelf.shelf_pk " +
            "LEFT JOIN warehouse ON shelf.warehouse_fk = warehouse.warehouse_pk " +
            "LEFT JOIN supplier ON product.supplier_fk = supplier.supplier_pk");
            int rowNum = 1;
        while(rs.next()) {
            model.addRow(new Object[]{
                    rowNum++,
                    rs.getString("name"),
                    "SKU-" + rs.getInt("product_pk"), // Simplified SKU
                    rs.getString("categoryName"),
                    "$" + rs.getInt("price"),
                    rs.getInt("quantity"),
                    rs.getString("warehouseName"),
                    rs.getString("shelfCode"),
                    rs.getString("supplierName")
            });
        }
        btnUpdate.setEnabled(false);
            btnSave.setEnabled(true);
            btnFormUpdate.setEnabled(false);
            btnFormSave.setEnabled(true);
        } catch(Exception e) {
            ErrorHandler.showError(e, "loading products");
        }
}

    /**
     * PRESERVED METHOD - Do not modify validation logic.
     */
private boolean validateFields(String type) {
    if(type.equals("new")) {
            return txtName.getText().trim().isEmpty() || txtPrice.getText().trim().isEmpty() ||
                   txtDescription.getText().trim().isEmpty() || txtQuantity.getText().trim().isEmpty() ||
                   ComboBoxShelf.getSelectedItem() == null || ComboBoxSupplier.getSelectedItem() == null;
    } else {
            return txtName.getText().trim().isEmpty() || txtPrice.getText().trim().isEmpty() ||
                   txtDescription.getText().trim().isEmpty();
    }
}

    /**
     * PRESERVED METHOD - Do not modify selection logic.
     * Note: This method needs to work with the original table structure internally.
     */
private void selectProduct() {
    int index = tableProduct.getSelectedRow();
        if (index < 0) return;
        
        // Need to reload full data to get all fields
        try {
            Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "selecting product");
                return;
            }
            Statement st = con.createStatement();
            // Get product name from table
            String productName = tableProduct.getValueAt(index, 1).toString();
            PreparedStatement ps = con.prepareStatement("SELECT product.*, category.name AS categoryName FROM product " +
                "INNER JOIN category ON product.category_fk = category.category_pk WHERE product.name = ?");
            ps.setString(1, productName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                productPk = rs.getInt("product_pk");
                txtName.setText(rs.getString("name"));
                txtQuantity.setText(String.valueOf(rs.getInt("quantity")));
                txtPrice.setText(String.valueOf(rs.getInt("price")));
                txtDescription.setText(rs.getString("description"));

                loadShelves();
                selectComboItemById(ComboBoxShelf, rs.getInt("shelf_fk"));
                loadSuppliers();
                selectComboItemById(ComboBoxSupplier, rs.getInt("supplier_fk"));

    ComboBoxCategory.removeAllItems();
                ComboBoxCategory.addItem(rs.getInt("category_fk")+" - "+rs.getString("categoryName"));
                loadCategoriesExceptCurrent(rs.getInt("category_fk"));

    btnSave.setEnabled(false);
    btnUpdate.setEnabled(true);
                btnFormSave.setEnabled(false);
                btnFormUpdate.setEnabled(true);
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "selecting product");
        }
}

    /**
     * PRESERVED METHOD - Do not modify SQL query.
     */
private void loadCategoriesExceptCurrent(int currentId) {
    try {
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading categories");
                return;
            }
        Statement st = con.createStatement();
            // PRESERVED SQL QUERY
        ResultSet rs = st.executeQuery("SELECT * FROM category");
        while(rs.next()) {
            if(rs.getInt("category_pk") != currentId)
                ComboBoxCategory.addItem(rs.getInt("category_pk")+" - "+rs.getString("name"));
        }
        } catch(Exception e) {
            ErrorHandler.showError(e, "loading categories");
        }
}

    /**
     * PRESERVED METHOD - Do not modify SQL INSERT statement.
     */
private void saveProduct() {
        if(validateFields("new")) {
            JOptionPane.showMessageDialog(null, "All required fields must be filled", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    try {
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "saving product");
                return;
            }
        PreparedStatement ps = con.prepareStatement("INSERT INTO product(name,quantity,price,description,category_fk,shelf_fk,supplier_fk) VALUES(?,?,?,?,?,?,?)");
            ps.setString(1, txtName.getText().trim());
            ps.setInt(2, Integer.parseInt(txtQuantity.getText().trim()));
            ps.setInt(3, Integer.parseInt(txtPrice.getText().trim()));
            ps.setString(4, txtDescription.getText().trim());
            ps.setInt(5, Integer.parseInt(((String)ComboBoxCategory.getSelectedItem()).split(" - ")[0]));
            ps.setInt(6, Integer.parseInt(((String)ComboBoxShelf.getSelectedItem()).split(" - ")[0]));
            ps.setInt(7, Integer.parseInt(((String)ComboBoxSupplier.getSelectedItem()).split(" - ")[0]));
        ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Product added successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch(Exception e) {
            ErrorHandler.showError(e, "saving product");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL UPDATE statement.
     */
private void updateProduct() {
        if(validateFields("edit")) {
            JOptionPane.showMessageDialog(null, "All required fields must be filled", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    try {
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "updating product");
                return;
            }
        PreparedStatement ps = con.prepareStatement("UPDATE product SET name=?, quantity=?, price=?, description=?, category_fk=?, shelf_fk=?, supplier_fk=? WHERE product_pk=?");
            ps.setString(1, txtName.getText().trim());
            ps.setInt(2, Integer.parseInt(txtQuantity.getText().trim()));
            ps.setInt(3, Integer.parseInt(txtPrice.getText().trim()));
            ps.setString(4, txtDescription.getText().trim());
            ps.setInt(5, Integer.parseInt(((String)ComboBoxCategory.getSelectedItem()).split(" - ")[0]));
            ps.setInt(6, Integer.parseInt(((String)ComboBoxShelf.getSelectedItem()).split(" - ")[0]));
            ps.setInt(7, Integer.parseInt(((String)ComboBoxSupplier.getSelectedItem()).split(" - ")[0]));
        ps.setInt(8, productPk);
        ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Product updated successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch(Exception e) {
            ErrorHandler.showError(e, "updating product");
        }
    }
    
    /**
     * Clears the form.
     */
    private void clearForm() {
        txtName.setText("");
        txtQuantity.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        ComboBoxCategory.setSelectedIndex(0);
        if (ComboBoxShelf.getItemCount() > 0) ComboBoxShelf.setSelectedIndex(0);
        if (ComboBoxSupplier.getItemCount() > 0) ComboBoxSupplier.setSelectedIndex(0);
        productPk = 0;
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnFormSave.setEnabled(true);
        btnFormUpdate.setEnabled(false);
        tableProduct.clearSelection();
    }

    /**
     * Deletes the currently selected product after confirmation. Previously
     * there was no way to remove a product from the app at all -- it had to
     * be deleted directly in the database.
     */
    private void deleteProduct() {
        if (productPk == 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.", "No Product Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + txtName.getText() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "deleting product");
                return;
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM product WHERE product_pk=?");
            ps.setInt(1, productPk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Product deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "deleting product");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify reset logic.
     */
    private void resetForm() {
        setVisible(false);
        dispose();
        new Product().setVisible(true);
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(Product::new);
}
}
