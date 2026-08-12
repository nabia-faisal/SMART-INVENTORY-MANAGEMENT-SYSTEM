package screens;
import screens.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

// iText imports for PDF generation
import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;

import dao.ConnectionProvider;
import dao.InventoryUtils;
import common.OpenPdf;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ModernTableRenderer;
import ui.ErrorHandler;

/**
 * Modernized Order management window with POS-style layout.
 * 
 * PRESERVED METHODS (DO NOT MODIFY):
 * - loadCustomerAndProductTables() - SQL queries preserved
 * - selectCustomer() - Selection logic preserved
 * - selectProduct() - Selection logic preserved
 * - addToCart() - Cart logic and SQL preserved
 * - removeFromCart() - Cart logic preserved
 * - saveOrder() - SQL INSERT/UPDATE and PDF generation preserved
 * - clearProductFields() - Field clearing preserved
 * - All field names preserved
 * - customerPk, productPk, finalTotalPrice, orderId variable names preserved
 */
public class Order extends JFrame {

private int customerPk = 0;
private int productPk = 0;
private int finalTotalPrice = 0;
private String orderId = "";

// Kept so callers can still pass a role/email (Home.java does), but the
// customer list is no longer scoped by it -- customers are managed
// manually by SuperAdmin, not linked to individual logins.
private String currentUserRole;
private String currentUserEmail;

private JTextField txtCustomerName, txtCustomerMobileNumber, txtCustomerEmail;
private JTextField txtProductName, txtProductPrice, txtProductDescription, txtOrderQuantity;
private JLabel lblFinalTotalPrice;
private JTable tableCustomer, tableProduct, tableCart;

public Order() { this("Admin", ""); }

public Order(String role, String email) {
    super("Manage Orders");
        this.currentUserRole = (role == null) ? "Admin" : role;
        this.currentUserEmail = (email == null) ? "" : email;
        setSize(1400, 750);
    setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UIStyle.BACKGROUND);
        
        // Top bar
        createTopBar();
        
        // Main content
        createMainContent();
        
        // Load data when shown
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent evt) {
                loadCustomerAndProductTables(); // PRESERVED METHOD CALL
            }
        });
        
        setVisible(true);
    }
    
    /**
     * Creates the top bar.
     */
    private void createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyle.BACKGROUND);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.SEPARATOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

    JLabel title = new JLabel("Manage Orders");
        title.setFont(UIStyle.FONT_HEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);
        
        add(topBar, BorderLayout.NORTH);
    }
    
    /**
     * Creates the main content with POS-style layout.
     */
    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 0));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Left: Customer and Product selection
        createSelectionPanel(mainPanel);
        
        // Center: Cart
        createCartPanel(mainPanel);
        
        // Right: Summary and actions
        createSummaryPanel(mainPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the left panel with customer and product tables.
     */
    private void createSelectionPanel(JPanel parent) {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Customer table card
        RoundedPanel customerCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        customerCard.setBackground(UIStyle.CARD_BG);
        customerCard.setLayout(new BorderLayout());
        customerCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        JLabel customerTitle = new JLabel("Select Customer");
        customerTitle.setFont(UIStyle.FONT_SUBHEADING);
        customerTitle.setForeground(UIStyle.TEXT_DARK);
        customerTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        customerCard.add(customerTitle, BorderLayout.NORTH);
        
        tableCustomer = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Mobile", "Email"}, 0));
        tableCustomer.setFont(UIStyle.FONT_BODY);
        tableCustomer.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableCustomer.setShowGrid(false);
        tableCustomer.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableCustomer.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableCustomer.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableCustomer.getTableHeader().setForeground(Color.WHITE);
        tableCustomer.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // PRESERVED: Mouse click handler
        tableCustomer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectCustomer(); // PRESERVED METHOD CALL
            }
        });
        
        // Wrap scroll pane in panel with visible gap - using colored border for visibility
        JPanel customerScrollWrapper = new JPanel(new BorderLayout());
        customerScrollWrapper.setOpaque(true);
        customerScrollWrapper.setBackground(UIStyle.CARD_BG);
        customerScrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane customerScroll = new JScrollPane(tableCustomer);
        customerScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        customerScroll.setOpaque(false);
        customerScroll.getViewport().setOpaque(false);
        customerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        customerScroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        customerScroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        customerScrollWrapper.add(customerScroll, BorderLayout.CENTER);
        customerCard.add(customerScrollWrapper, BorderLayout.CENTER);
        
        // Customer details - hidden fields for data storage
        txtCustomerName = new JTextField();
        txtCustomerName.setVisible(false);
        txtCustomerMobileNumber = new JTextField();
        txtCustomerMobileNumber.setVisible(false);
        txtCustomerEmail = new JTextField();
        txtCustomerEmail.setVisible(false);
        
        leftPanel.add(customerCard);
        leftPanel.add(Box.createVerticalStrut(15));
        
        // Product table card
        RoundedPanel productCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        productCard.setBackground(UIStyle.CARD_BG);
        productCard.setLayout(new BorderLayout());
        productCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        JLabel productTitle = new JLabel("Select Product");
        productTitle.setFont(UIStyle.FONT_SUBHEADING);
        productTitle.setForeground(UIStyle.TEXT_DARK);
        productTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        productCard.add(productTitle, BorderLayout.NORTH);
        
        tableProduct = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Price", "Stock", "Category"}, 0));
        tableProduct.setFont(UIStyle.FONT_BODY);
        tableProduct.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableProduct.setShowGrid(false);
        tableProduct.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableProduct.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableProduct.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableProduct.getTableHeader().setForeground(Color.WHITE);
        tableProduct.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // PRESERVED: Mouse click handler
        tableProduct.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectProduct(); // PRESERVED METHOD CALL
            }
        });
        
        // Wrap scroll pane in panel with visible gap - using colored border for visibility
        JPanel productScrollWrapper = new JPanel(new BorderLayout());
        productScrollWrapper.setOpaque(true);
        productScrollWrapper.setBackground(UIStyle.CARD_BG);
        productScrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane productScroll = new JScrollPane(tableProduct);
        productScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        productScroll.setOpaque(false);
        productScroll.getViewport().setOpaque(false);
        productScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        productScroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        productScroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        productScrollWrapper.add(productScroll, BorderLayout.CENTER);
        productCard.add(productScrollWrapper, BorderLayout.CENTER);
        
        // Product details and quantity - simplified
        JPanel productDetails = new JPanel();
        productDetails.setLayout(new BoxLayout(productDetails, BoxLayout.Y_AXIS));
        productDetails.setOpaque(false);
        productDetails.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Hidden fields for data storage
        txtProductName = new JTextField();
        txtProductName.setVisible(false);
        txtProductPrice = new JTextField();
        txtProductPrice.setVisible(false);
        txtProductDescription = new JTextField();
        txtProductDescription.setVisible(false);
        
        JLabel lblQty = new JLabel("Quantity:");
        lblQty.setFont(UIStyle.FONT_BODY_BOLD);
        lblQty.setForeground(UIStyle.TEXT_DARK);
        lblQty.setAlignmentX(Component.LEFT_ALIGNMENT);
        productDetails.add(lblQty);
        productDetails.add(Box.createVerticalStrut(5));
        txtOrderQuantity = new JTextField();
        txtOrderQuantity.setFont(UIStyle.FONT_BODY);
        txtOrderQuantity.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        txtOrderQuantity.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        txtOrderQuantity.setAlignmentX(Component.LEFT_ALIGNMENT);
        productDetails.add(txtOrderQuantity);
        productDetails.add(Box.createVerticalStrut(10));
        
        JButton btnAddToCart = new JButton("Add To Cart");
        btnAddToCart.setFont(UIStyle.getFont(Font.BOLD, 13)); // Bold text
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setBackground(UIStyle.PRIMARY); // Very dark blue
        btnAddToCart.setBorderPainted(false);
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddToCart.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.BUTTON_HEIGHT));
        btnAddToCart.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAddToCart.addActionListener(e -> addToCart()); // PRESERVED ACTION
        productDetails.add(btnAddToCart);
        
        productCard.add(productDetails, BorderLayout.SOUTH);
        
        leftPanel.add(productCard);
        
        parent.add(leftPanel, BorderLayout.WEST);
    }
    
    /**
     * Creates the center cart panel.
     */
    private void createCartPanel(JPanel parent) {
        RoundedPanel cartCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        cartCard.setBackground(UIStyle.CARD_BG);
        cartCard.setLayout(new BorderLayout());
        cartCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        
        JLabel cartTitle = new JLabel("Shopping Cart");
        cartTitle.setFont(UIStyle.FONT_SUBHEADING);
        cartTitle.setForeground(UIStyle.TEXT_DARK);
        cartTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        cartCard.add(cartTitle, BorderLayout.NORTH);
        
        tableCart = new JTable(new DefaultTableModel(new Object[]{"Product ID", "Name", "Quantity", "Price", "Sub Total"}, 0));
        tableCart.setFont(UIStyle.FONT_BODY);
        tableCart.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableCart.setShowGrid(false);
        tableCart.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableCart.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableCart.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableCart.getTableHeader().setForeground(Color.WHITE);
        tableCart.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // PRESERVED: Mouse click handler
        tableCart.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                removeFromCart(); // PRESERVED METHOD CALL
            }
        });
        
        // Wrap scroll pane in panel with visible gap - using colored border for visibility
        JPanel cartScrollWrapper = new JPanel(new BorderLayout());
        cartScrollWrapper.setOpaque(true);
        cartScrollWrapper.setBackground(UIStyle.CARD_BG);
        cartScrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane cartScroll = new JScrollPane(tableCart);
        cartScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        cartScroll.setOpaque(false);
        cartScroll.getViewport().setOpaque(false);
        cartScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cartScroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        cartScroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        cartScrollWrapper.add(cartScroll, BorderLayout.CENTER);
        cartCard.add(cartScrollWrapper, BorderLayout.CENTER);
        
        parent.add(cartCard, BorderLayout.CENTER);
    }
    
    /**
     * Creates the right summary panel.
     */
    private void createSummaryPanel(JPanel parent) {
        RoundedPanel summaryCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        summaryCard.setBackground(UIStyle.CARD_BG);
        summaryCard.setLayout(new BorderLayout());
        summaryCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        summaryCard.setPreferredSize(new Dimension(350, 0));
        
        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(UIStyle.FONT_SUBHEADING);
        summaryTitle.setForeground(UIStyle.TEXT_DARK);
        summaryTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        summaryCard.add(summaryTitle, BorderLayout.NORTH);
        
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setOpaque(false);
        
        JLabel lblTotal = new JLabel("Total Amount:");
        lblTotal.setFont(UIStyle.FONT_BODY_BOLD);
        lblTotal.setForeground(UIStyle.TEXT_DARK);
        summaryPanel.add(lblTotal);
        summaryPanel.add(Box.createVerticalStrut(10));
        
        lblFinalTotalPrice = new JLabel("$0");
        lblFinalTotalPrice.setFont(UIStyle.getFont(Font.BOLD, 32));
        lblFinalTotalPrice.setForeground(UIStyle.PRIMARY);
        summaryPanel.add(lblFinalTotalPrice);
        summaryPanel.add(Box.createVerticalGlue());
        
        // Action buttons
        JButton btnSaveOrder = new JButton("Place Order");
        btnSaveOrder.setFont(UIStyle.getFont(Font.BOLD, 13)); // Bold text
        btnSaveOrder.setForeground(Color.WHITE);
        btnSaveOrder.setBackground(UIStyle.PRIMARY); // Very dark blue
        btnSaveOrder.setBorderPainted(false);
        btnSaveOrder.setFocusPainted(false);
        btnSaveOrder.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSaveOrder.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.BUTTON_HEIGHT + 10));
        btnSaveOrder.addActionListener(e -> saveOrder()); // PRESERVED ACTION
        summaryPanel.add(btnSaveOrder);
        summaryPanel.add(Box.createVerticalStrut(10));
        
        JButton btnReset = new JButton("Reset");
        btnReset.setFont(UIStyle.getFont(Font.BOLD, 13)); // Bold text
        btnReset.setForeground(Color.WHITE);
        btnReset.setBackground(UIStyle.PRIMARY); // Very dark blue
        btnReset.setBorderPainted(false);
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.BUTTON_HEIGHT));
        btnReset.addActionListener(e -> {
            setVisible(false);
            new Order(currentUserRole, currentUserEmail).setVisible(true);
        });
        summaryPanel.add(btnReset);
        summaryPanel.add(Box.createVerticalStrut(10));
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(UIStyle.getFont(Font.BOLD, 13)); // Bold text
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(UIStyle.PRIMARY); // Very dark blue
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.BUTTON_HEIGHT));
    btnClose.addActionListener(e -> setVisible(false));
        summaryPanel.add(btnClose);
        
        summaryCard.add(summaryPanel, BorderLayout.CENTER);
        
        parent.add(summaryCard, BorderLayout.EAST);
    }
    
    /**
     * Helper to add a detail field.
     */
    private void addDetailField(JPanel parent, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIStyle.FONT_BODY_BOLD);
        lbl.setForeground(UIStyle.TEXT_DARK);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(5));
        
        field.setFont(UIStyle.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL queries.
     */
private void loadCustomerAndProductTables() {
    try {
        DefaultTableModel customerModel = (DefaultTableModel) tableCustomer.getModel();
        DefaultTableModel productModel = (DefaultTableModel) tableProduct.getModel();
            customerModel.setRowCount(0);
            productModel.setRowCount(0);
            
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading data");
                return;
            }
        Statement st = con.createStatement();
            ResultSet rs;
            // PRESERVED SQL QUERY -- customers are a manually-managed contact
            // list (added by SuperAdmin), not linked to individual logins, so
            // everyone who can reach this screen sees the full customer list.
            rs = st.executeQuery("SELECT * FROM customer");
            while (rs.next()) {
                customerModel.addRow(new Object[]{
                    rs.getInt("customer_pk"),
                    rs.getString("name"),
                    rs.getString("mobileNumber"),
                    rs.getString("email")
                });
            }
            rs = st.executeQuery("SELECT product.*, category.name AS categoryName FROM product " +
                "INNER JOIN category ON product.category_fk = category.category_pk");
            while (rs.next()) {
                productModel.addRow(new Object[]{
                    rs.getInt("product_pk"), 
                    rs.getString("name"), 
                    rs.getInt("price"), 
                    rs.getInt("quantity"), 
                    rs.getString("categoryName")
                });
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading customer and product data");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify selection logic.
     */
private void selectCustomer() {
    int index = tableCustomer.getSelectedRow();
        if (index < 0) return;
    TableModel model = tableCustomer.getModel();
    customerPk = Integer.parseInt(model.getValueAt(index, 0).toString());
    txtCustomerName.setText(model.getValueAt(index, 1).toString());
    txtCustomerMobileNumber.setText(model.getValueAt(index, 2).toString());
    txtCustomerEmail.setText(model.getValueAt(index, 3).toString());
}

    /**
     * PRESERVED METHOD - Do not modify selection logic.
     */
private void selectProduct() {
    int index = tableProduct.getSelectedRow();
        if (index < 0) return;
    TableModel model = tableProduct.getModel();
    productPk = Integer.parseInt(model.getValueAt(index, 0).toString());
    txtProductName.setText(model.getValueAt(index, 1).toString());
    txtProductPrice.setText(model.getValueAt(index, 2).toString());
        // Get full description from database
        try {
            Connection con = ConnectionProvider.getCon();
            if (con != null) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT description FROM product WHERE product_pk=" + productPk);
                if (rs.next()) {
                    txtProductDescription.setText(rs.getString("description"));
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify cart logic or SQL.
     */
private void addToCart() {
        String qtyStr = txtOrderQuantity.getText().trim();
        if (qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Quantity required", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    int qty = Integer.parseInt(qtyStr);
        if (qty <= 0) {
            JOptionPane.showMessageDialog(null, "Quantity must be a positive number",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
    int price = Integer.parseInt(txtProductPrice.getText());
    int total = qty * price;

    try {
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "adding to cart");
                return;
            }
        Statement st = con.createStatement();
            // PRESERVED SQL QUERY
            ResultSet rs = st.executeQuery("SELECT quantity FROM product WHERE product_pk=" + productPk);
        if (rs.next() && rs.getInt("quantity") >= qty) {
            DefaultTableModel model = (DefaultTableModel) tableCart.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    if ((int)model.getValueAt(i, 0) == productPk) {
                        JOptionPane.showMessageDialog(null, "Product already in cart", 
                            "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                model.addRow(new Object[]{productPk, txtProductName.getText(), qty, price, total});
            finalTotalPrice += total;
                lblFinalTotalPrice.setText("$" + finalTotalPrice);
                clearProductFields(); // PRESERVED METHOD CALL
            } else {
                JOptionPane.showMessageDialog(null, "Insufficient stock", 
                    "Stock Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch(Exception e) {
            ErrorHandler.showError(e, "adding product to cart");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify cart logic.
     */
private void removeFromCart() {
    int index = tableCart.getSelectedRow();
        if (index >= 0 && JOptionPane.showConfirmDialog(null, "Remove this product from cart?", 
            "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        TableModel model = tableCart.getModel();
            finalTotalPrice -= Integer.parseInt(model.getValueAt(index, 4).toString());
            lblFinalTotalPrice.setText("$" + finalTotalPrice);
        ((DefaultTableModel) tableCart.getModel()).removeRow(index);
    }
}

    /**
     * PRESERVED METHOD - Do not modify SQL INSERT/UPDATE or PDF generation.
     */
private void saveOrder() {
        if (finalTotalPrice == 0 || txtCustomerName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Select customer and add products to cart", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        orderId = "Bill-" + System.nanoTime();

    try {
        Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "saving order");
                return;
            }
        DefaultTableModel dtm = (DefaultTableModel) tableCart.getModel();
            // PRESERVED SQL UPDATES
            for(int i = 0; i < dtm.getRowCount(); i++) {
                con.createStatement().executeUpdate("UPDATE product SET quantity=quantity-" + 
                    dtm.getValueAt(i, 2) + " WHERE product_pk=" + dtm.getValueAt(i, 0));
            }
            // PRESERVED SQL INSERT
        PreparedStatement ps = con.prepareStatement("INSERT INTO orderDetail(orderId,customer_fk,orderDate,totalPaid) VALUES(?,?,?,?)");
        ps.setString(1, orderId);
        ps.setInt(2, customerPk);
            ps.setDate(3, new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
        ps.setInt(4, finalTotalPrice);
        ps.executeUpdate();
        } catch(Exception e) {
            ErrorHandler.showError(e, "saving order");
            return;
        }

        // PRESERVED: PDF generation
    try {
        Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(InventoryUtils.billPath + orderId + ".pdf"));
        doc.open();
        doc.add(new com.itextpdf.text.Paragraph("Inventory Management System"));
        doc.add(new com.itextpdf.text.Paragraph("**************************************************************"));
            doc.add(new com.itextpdf.text.Paragraph("Order ID: " + orderId + "\nDate: " + 
                new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime()) + 
                "\nTotal Paid: " + finalTotalPrice));
        doc.add(new com.itextpdf.text.Paragraph("**************************************************************"));

        PdfPTable tb = new PdfPTable(5);
            String[] headers = {"Name", "Description", "Price Per Unit", "Quantity", "Sub Total"};
            for(String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(new BaseColor(255, 204, 51));
                tb.addCell(cell);
            }
            for(int i = 0; i < tableCart.getRowCount(); i++) {
                tb.addCell(tableCart.getValueAt(i, 1).toString());
                tb.addCell(txtProductDescription.getText());
                tb.addCell(tableCart.getValueAt(i, 3).toString());
                tb.addCell(tableCart.getValueAt(i, 2).toString());
                tb.addCell(tableCart.getValueAt(i, 4).toString());
        }
        doc.add(tb);
        doc.add(new com.itextpdf.text.Paragraph("**************************************************************"));
        doc.add(new com.itextpdf.text.Paragraph("Thank you! Please visit again."));
        doc.close();

        OpenPdf.OpenById(orderId);
        } catch(Exception e) {
            ErrorHandler.showError(e, "generating order PDF");
        }
        
        JOptionPane.showMessageDialog(null, "Order placed successfully!",
            "Success", JOptionPane.INFORMATION_MESSAGE);
        setVisible(false);
        new Order(currentUserRole, currentUserEmail).setVisible(true);
    }
    
    /**
     * PRESERVED METHOD - Do not modify field clearing logic.
     */
    private void clearProductFields() {
        txtProductName.setText("");
        txtProductPrice.setText("");
        txtProductDescription.setText("");
        txtOrderQuantity.setText("");
        productPk = 0;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Order::new);
    }
}
