package screens;
import screens.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import dao.ConnectionProvider;
import common.OpenPdf;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ModernTableRenderer;
import ui.ErrorHandler;

/**
 * Modernized View Orders window.
 * 
 * PRESERVED METHODS (DO NOT MODIFY):
 * - loadCustomers() - SQL query preserved
 * - loadOrdersForCustomer() - SQL query preserved
 * - openOrderPdf() - PDF opening logic preserved
 * - tableCustomer and tableOrder mouse click handlers preserved
 */
public class ViewOrders extends JFrame {
    
    private JTable tableCustomer, tableOrder;
    private JButton btnClose;

    // Kept so callers can still pass a role/email (Home.java does), but the
    // customer list is no longer scoped by it -- see loadCustomers().
    private String currentUserRole;
    private String currentUserEmail;

    public ViewOrders() { this("Admin", ""); }

    public ViewOrders(String role, String email) {
        super("View Orders");
        this.currentUserRole = (role == null) ? "Admin" : role;
        this.currentUserEmail = (email == null) ? "" : email;
        setSize(1200, 700);
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
            public void componentShown(ComponentEvent e) {
                loadCustomers(); // PRESERVED METHOD CALL
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
        
        JLabel title = new JLabel("View Orders");
        title.setFont(UIStyle.FONT_HEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);
        
        add(topBar, BorderLayout.NORTH);
    }
    
    /**
     * Creates the main content area with two tables.
     */
    private void createMainContent() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // Equal width columns
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Left: Customer table
        createCustomerTablePanel(mainPanel);
        
        // Right: Order table
        createOrderTablePanel(mainPanel);
        
        // Bottom: Close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        btnClose = new JButton("Close");
        btnClose.setFont(UIStyle.FONT_BODY);
        btnClose.setForeground(UIStyle.TEXT_DARK);
        btnClose.setBackground(UIStyle.SEPARATOR);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(100, UIStyle.BUTTON_HEIGHT));
        btnClose.addActionListener(e -> setVisible(false));
        bottomPanel.add(btnClose);
        
        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the customer table panel.
     */
    private void createCustomerTablePanel(JPanel parent) {
        RoundedPanel customerCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        customerCard.setBackground(UIStyle.CARD_BG);
        customerCard.setLayout(new BorderLayout());
        customerCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        customerCard.setPreferredSize(new Dimension(0, Integer.MAX_VALUE)); // Fill height
        
        JLabel customerTitle = new JLabel("Customer List");
        customerTitle.setFont(UIStyle.FONT_SUBHEADING);
        customerTitle.setForeground(UIStyle.TEXT_DARK);
        customerTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        customerCard.add(customerTitle, BorderLayout.NORTH);
        
        tableCustomer = new JTable(new DefaultTableModel(
            new Object[]{"ID", "Name", "Mobile Number", "Email"}, 0
        ));
        tableCustomer.setFont(UIStyle.FONT_BODY);
        tableCustomer.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableCustomer.setShowGrid(false);
        tableCustomer.setIntercellSpacing(new Dimension(0, 0));
        tableCustomer.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableCustomer.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableCustomer.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableCustomer.getTableHeader().setForeground(Color.WHITE);
        tableCustomer.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // PRESERVED: Mouse click handler
        tableCustomer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                loadOrdersForCustomer(); // PRESERVED METHOD CALL
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
        
        parent.add(customerCard);
    }
    
    /**
     * Creates the order table panel.
     */
    private void createOrderTablePanel(JPanel parent) {
        RoundedPanel orderCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        orderCard.setBackground(UIStyle.CARD_BG);
        orderCard.setLayout(new BorderLayout());
        orderCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        orderCard.setPreferredSize(new Dimension(0, Integer.MAX_VALUE)); // Fill height
        
        JLabel orderTitle = new JLabel("Order List");
        orderTitle.setFont(UIStyle.FONT_SUBHEADING);
        orderTitle.setForeground(UIStyle.TEXT_DARK);
        orderTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        orderCard.add(orderTitle, BorderLayout.NORTH);
        
        tableOrder = new JTable(new DefaultTableModel(
            new Object[]{"Order ID", "Date", "Total Paid"}, 0
        ));
        tableOrder.setFont(UIStyle.FONT_BODY);
        tableOrder.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableOrder.setShowGrid(false);
        tableOrder.setIntercellSpacing(new Dimension(0, 0));
        tableOrder.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableOrder.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableOrder.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableOrder.getTableHeader().setForeground(Color.WHITE);
        tableOrder.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // PRESERVED: Mouse click handler
        tableOrder.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                openOrderPdf(); // PRESERVED METHOD CALL
            }
        });
        
        // Wrap scroll pane in panel with visible gap - using colored border for visibility
        JPanel orderScrollWrapper = new JPanel(new BorderLayout());
        orderScrollWrapper.setOpaque(true);
        orderScrollWrapper.setBackground(UIStyle.CARD_BG);
        orderScrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15)); // Right gap for scrollbar
        JScrollPane orderScroll = new JScrollPane(tableOrder);
        orderScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Gap between scrollbar and content
        orderScroll.setOpaque(false);
        orderScroll.getViewport().setOpaque(false);
        orderScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        orderScroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0)); // Thinner, prettier scrollbar
        orderScroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        orderScrollWrapper.add(orderScroll, BorderLayout.CENTER);
        orderCard.add(orderScrollWrapper, BorderLayout.CENTER);
        
        parent.add(orderCard);
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL query.
     */
    private void loadCustomers() {
        try {
            DefaultTableModel model = (DefaultTableModel) tableCustomer.getModel();
            model.setRowCount(0);
            Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading customers");
                return;
            }
            Statement st = con.createStatement();
            // PRESERVED SQL QUERY -- customers are a manually-managed contact
            // list, not linked to individual logins, so everyone who can
            // reach this screen sees the full customer list.
            ResultSet rs = st.executeQuery("SELECT * FROM customer");
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("customer_pk"),
                    rs.getString("name"),
                    rs.getString("mobileNumber"),
                    rs.getString("email")
                });
            }
        } catch(Exception e) {
            ErrorHandler.showError(e, "loading customers");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify SQL query.
     */
    private void loadOrdersForCustomer() {
        int index = tableCustomer.getSelectedRow();
        if(index < 0) return;
        
        TableModel model = tableCustomer.getModel();
        String customerId = model.getValueAt(index, 0).toString();
        
        DefaultTableModel orderModel = (DefaultTableModel) tableOrder.getModel();
        orderModel.setRowCount(0);
        
        try {
            Connection con = ConnectionProvider.getCon();
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading orders");
                return;
            }
            Statement st = con.createStatement();
            // PRESERVED SQL QUERY
            ResultSet rs = st.executeQuery("SELECT * FROM orderDetail WHERE customer_fk=" + customerId);
            while(rs.next()) {
                orderModel.addRow(new Object[]{
                    rs.getString("orderId"),
                    rs.getDate("orderDate"),
                    "$" + rs.getInt("totalPaid")
                });
            }
        } catch(Exception e) {
            ErrorHandler.showError(e, "loading orders for customer");
        }
    }
    
    /**
     * PRESERVED METHOD - Do not modify PDF opening logic.
     */
    private void openOrderPdf() {
        int index = tableOrder.getSelectedRow();
        if(index < 0) return;
        
        TableModel model = tableOrder.getModel();
        String orderId = model.getValueAt(index, 0).toString();
        OpenPdf.OpenById(orderId); // PRESERVED METHOD CALL
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ViewOrders::new);
    }
}
