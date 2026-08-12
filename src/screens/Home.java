package screens;
import screens.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import dao.ConnectionProvider;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ModernTableRenderer;
import ui.ErrorHandler;

public class Home extends JFrame {

    private String currentUserRole;
    private String currentUserEmail;
    private String currentUserName = "";

    private JPanel sidebar;
    private JPanel mainContent;
    private CardLayout cardLayout;

    private JLabel lblTotalProducts, lblOrdersToday, lblActiveCustomers, lblInventoryValue, lblLowStock;
    private JTable tableTopProducts, tableLowStock;
    private JLabel lblAlertBanner;

    // Stock alert threshold: items at or below this are flagged.
    // "Approaching" the threshold (within +10) gets a softer warning.
    private static final int LOW_STOCK_THRESHOLD = 20;
    private static final int APPROACHING_WINDOW = 10;

    public Home() {
        this("User", "user@example.com");
    }

    public Home(String role, String email) {
        super("SISMS - Secure Inventory and Supply Chain Management System");

        this.currentUserRole = role;
        this.currentUserEmail = email;

        loadUserInfo();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createTopBar();
        createSidebar(role);
        createMainContent();
        createDashboardPage();

        setVisible(true);
    }

    /** Load user name/email from DB */
    private void loadUserInfo() {
        try {
            Connection con = ConnectionProvider.getCon();
            if (con == null) return;

            PreparedStatement ps = con.prepareStatement(
                    "SELECT name, email FROM appuser WHERE email=? LIMIT 1");
            ps.setString(1, currentUserEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUserName = rs.getString("name");
                currentUserEmail = rs.getString("email");
            } else {
                currentUserName = "User";
            }

        } catch (Exception e) {
            currentUserName = "User";
            currentUserEmail = "user@example.com";
        }
    }

    /** TOP BAR */
    private void createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyle.BACKGROUND);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.SEPARATOR));
        topBar.setPreferredSize(new Dimension(0, 60));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        JLabel lblBrand = new JLabel("SISMS");
        lblBrand.setFont(UIStyle.FONT_HEADING);
        lblBrand.setForeground(UIStyle.PRIMARY);

        leftPanel.add(lblBrand);
        topBar.add(leftPanel, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);
    }

    /** SIDEBAR (fixed alignment, no top spacing issue) */
    private void createSidebar(String role) {

        sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(UIStyle.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(UIStyle.SIDEBAR_WIDTH, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIStyle.SIDEBAR_BG);

        /* ---- USER CARD ---- */
        RoundedPanel userPanel = new RoundedPanel(15, false);
        userPanel.setBackground(UIStyle.USER_CARD_BG);
        userPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        userPanel.setMaximumSize(new Dimension(UIStyle.SIDEBAR_WIDTH - 20, 70));
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));

        JLabel lblUser = new JLabel(currentUserName, SwingConstants.CENTER);
        lblUser.setFont(UIStyle.FONT_BODY_BOLD);
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblEmail = new JLabel(currentUserEmail, SwingConstants.CENTER);
        lblEmail.setFont(UIStyle.FONT_SMALL);
        lblEmail.setForeground(UIStyle.TEXT_LIGHT);
        lblEmail.setAlignmentX(Component.CENTER_ALIGNMENT);

        userPanel.add(lblUser);
        userPanel.add(lblEmail);

        content.add(Box.createVerticalStrut(10));
        content.add(userPanel);

        /* ---- CREATE NEW ---- */
        JButton btnCreateNew = new JButton("+ Create New");
        btnCreateNew.setFont(UIStyle.getFont(Font.BOLD, 13));
        btnCreateNew.setBackground(UIStyle.USER_CARD_BG);
        btnCreateNew.setForeground(UIStyle.TEXT_DARK);
        btnCreateNew.setBorderPainted(false);
        btnCreateNew.setFocusPainted(false);
        btnCreateNew.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreateNew.setMaximumSize(new Dimension(UIStyle.SIDEBAR_WIDTH - 20, 40));
        btnCreateNew.setAlignmentX(Component.CENTER_ALIGNMENT);

        if ("Customer".equalsIgnoreCase(role)) {
            btnCreateNew.setVisible(false);
        }

        content.add(Box.createVerticalStrut(10));
        content.add(btnCreateNew);
        content.add(Box.createVerticalStrut(10));

        /* ---- NAVIGATION MENU ---- */
        JPanel navPanel = new JPanel();
        navPanel.setBackground(UIStyle.SIDEBAR_BG);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] navItems = {
                "Dashboard", "Products", "Categories",
                "Suppliers", "Warehouses",
                "Orders", "Customers", "Users",
                "View Orders", "Logout"
        };

        for (String item : navItems) {

            JButton btn = new JButton(item);
            styleNavButton(btn);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Role-based visibility
            if ("Admin".equalsIgnoreCase(role) && (item.equals("Users") || item.equals("Customers"))) {
                btn.setVisible(false);
            }
            if ("Customer".equalsIgnoreCase(role)) {
                if (item.equals("Products") || item.equals("Categories") ||
                        item.equals("Suppliers") || item.equals("Warehouses") ||
                        item.equals("Customers") || item.equals("Users")) {
                    btn.setVisible(false);
                }
            }

            btn.addActionListener(e -> navigateToPage(item));

            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(12));
        }

        // Add nav panel in center (fixes spacing)
        content.add(navPanel);

        sidebar.add(content, BorderLayout.NORTH);

        add(sidebar, BorderLayout.WEST);
    }

    /** Navigation button styling */
    private void styleNavButton(JButton btn) {
        btn.setFont(UIStyle.getFont(Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(UIStyle.SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(UIStyle.SIDEBAR_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setBackground(UIStyle.SIDEBAR_BG); }
        });
    }

    /** MAIN CONTENT PANEL */
    private void createMainContent() {
        mainContent = new JPanel();
        cardLayout = new CardLayout();
        mainContent.setLayout(cardLayout);
        add(mainContent, BorderLayout.CENTER);
    }

    /** DASHBOARD PAGE */
    private void createDashboardPage() {

        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(UIStyle.BACKGROUND);

        /* ---- TOP: banner + KPI cards ---- */
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setOpaque(false);

        lblAlertBanner = new JLabel(" ");
        lblAlertBanner.setOpaque(true);
        lblAlertBanner.setFont(UIStyle.FONT_BODY_BOLD);
        lblAlertBanner.setForeground(Color.WHITE);
        lblAlertBanner.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        lblAlertBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAlertBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        lblAlertBanner.setVisible(false);
        topStack.add(lblAlertBanner);
        topStack.add(Box.createVerticalStrut(15));

        JPanel kpiPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTotalProducts = createKPICard("Total Products", "0");
        lblOrdersToday = createKPICard("Orders Today", "0");
        lblActiveCustomers = createKPICard("Active Customers", "0");
        lblInventoryValue = createKPICard("Inventory Value", "$0");
        lblLowStock = createKPICard("Low Stock Items", "0");

        kpiPanel.add(lblTotalProducts.getParent());
        kpiPanel.add(lblOrdersToday.getParent());
        kpiPanel.add(lblActiveCustomers.getParent());
        kpiPanel.add(lblInventoryValue.getParent());
        kpiPanel.add(lblLowStock.getParent());

        topStack.add(kpiPanel);
        topStack.add(Box.createVerticalStrut(15));

        dashboard.add(topStack, BorderLayout.NORTH);

        /* ---- CENTER: Top Selling Products + Stock Alerts side by side ---- */
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setOpaque(false);

        /* ---- TABLE AREA ---- */
        RoundedPanel tableCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        tableCard.setBackground(UIStyle.CARD_BG);
        tableCard.setLayout(new BorderLayout());

        JLabel tableTitle = new JLabel("Top Selling Products");
        tableTitle.setFont(UIStyle.FONT_SUBHEADING);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        tableTopProducts = new JTable(new DefaultTableModel(
                new Object[]{"Products", "Category", "Units Sold", "Price", "Revenue"}, 0
        ));
        tableTopProducts.setRowHeight(30);
        tableTopProducts.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableTopProducts.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableTopProducts.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableTopProducts.getTableHeader().setForeground(Color.WHITE);

        tableCard.add(new JScrollPane(tableTopProducts), BorderLayout.CENTER);

        centerPanel.add(tableCard);
        centerPanel.add(createStockAlertCard());

        dashboard.add(centerPanel, BorderLayout.CENTER);

        mainContent.add(dashboard, "Dashboard");

        loadDashboardData();
    }

    /** Stock Alerts card: any product at/under the low-stock threshold, or close to it. */
    private RoundedPanel createStockAlertCard() {
        RoundedPanel card = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        card.setBackground(UIStyle.CARD_BG);
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Stock Alerts (threshold: " + LOW_STOCK_THRESHOLD + " units)");
        title.setFont(UIStyle.FONT_SUBHEADING);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        card.add(title, BorderLayout.NORTH);

        tableLowStock = new JTable(new DefaultTableModel(
                new Object[]{"Product", "Quantity", "Status"}, 0
        ) {
            public boolean isCellEditable(int row, int col) { return false; }
        });
        tableLowStock.setRowHeight(30);
        tableLowStock.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableLowStock.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableLowStock.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableLowStock.getTableHeader().setForeground(Color.WHITE);

        // Color the Status column: red for at/under threshold, orange for approaching it.
        tableLowStock.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UIStyle.FONT_BODY_BOLD);
                if (!isSelected) {
                    setBackground(Color.WHITE);
                    if ("Critical".equals(value)) {
                        setForeground(UIStyle.ERROR);
                    } else if ("Approaching".equals(value)) {
                        setForeground(UIStyle.WARNING);
                    } else {
                        setForeground(UIStyle.TEXT_DARK);
                    }
                }
                return c;
            }
        });

        card.add(new JScrollPane(tableLowStock), BorderLayout.CENTER);
        return card;
    }

    private JLabel createKPICard(String title, String value) {
        RoundedPanel card = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        card.setBackground(UIStyle.CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(UIStyle.FONT_HEADING);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(UIStyle.FONT_SMALL);
        lblTitle.setForeground(UIStyle.TEXT_LIGHT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblValue);
        card.add(lblTitle);

        return lblValue;
    }

    /** Load counts for dashboard */
    private void loadDashboardData() {
        try {
            Connection con = ConnectionProvider.getCon();
            if (con == null) return;

            Statement st = con.createStatement();

            ResultSet rs;

            rs = st.executeQuery("SELECT COUNT(*) AS c FROM product");
            if (rs.next()) lblTotalProducts.setText(String.valueOf(rs.getInt("c")));

            rs = st.executeQuery("SELECT COUNT(*) AS c FROM orderDetail WHERE orderDate = CURDATE()");
            if (rs.next()) lblOrdersToday.setText(String.valueOf(rs.getInt("c")));

            rs = st.executeQuery("SELECT COUNT(*) AS c FROM customer");
            if (rs.next()) lblActiveCustomers.setText(String.valueOf(rs.getInt("c")));

            rs = st.executeQuery("SELECT SUM(price * quantity) AS total FROM product");
            if (rs.next()) lblInventoryValue.setText("$" + rs.getInt("total"));

            loadLowStockAlerts(con);

        } catch (Exception e) {
            ErrorHandler.showError(e, "loading dashboard");
        }
    }

    /**
     * Flags any product at/under LOW_STOCK_THRESHOLD ("Critical") or within
     * APPROACHING_WINDOW units above it ("Approaching"), fills the Stock
     * Alerts table, the Low Stock Items KPI, and the top banner.
     */
    private void loadLowStockAlerts(Connection con) {
        try {
            DefaultTableModel model = (DefaultTableModel) tableLowStock.getModel();
            model.setRowCount(0);

            int criticalCount = 0, approachingCount = 0;
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT name, quantity FROM product WHERE quantity < " +
                    (LOW_STOCK_THRESHOLD + APPROACHING_WINDOW) + " ORDER BY quantity ASC");
            while (rs.next()) {
                int qty = rs.getInt("quantity");
                String status = qty < LOW_STOCK_THRESHOLD ? "Critical" : "Approaching";
                if (qty < LOW_STOCK_THRESHOLD) criticalCount++; else approachingCount++;
                model.addRow(new Object[]{rs.getString("name"), qty, status});
            }

            lblLowStock.setText(String.valueOf(criticalCount));
            lblLowStock.setForeground(criticalCount > 0 ? UIStyle.ERROR : UIStyle.TEXT_DARK);

            if (criticalCount > 0) {
                lblAlertBanner.setText("⚠  " + criticalCount + " product" + (criticalCount == 1 ? "" : "s") +
                        " at or below the " + LOW_STOCK_THRESHOLD + "-unit threshold need restocking" +
                        (approachingCount > 0 ? "  (+" + approachingCount + " approaching it)" : ""));
                lblAlertBanner.setBackground(UIStyle.ERROR);
                lblAlertBanner.setVisible(true);
            } else if (approachingCount > 0) {
                lblAlertBanner.setText("⚠  " + approachingCount + " product" + (approachingCount == 1 ? "" : "s") +
                        " approaching the " + LOW_STOCK_THRESHOLD + "-unit stock threshold");
                lblAlertBanner.setBackground(UIStyle.WARNING);
                lblAlertBanner.setVisible(true);
            } else {
                lblAlertBanner.setVisible(false);
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading stock alerts");
        }
    }

    /** Navigation */
    private void navigateToPage(String page) {
        switch (page) {
            case "Dashboard" -> {
                cardLayout.show(mainContent, "Dashboard");
                loadDashboardData();
            }
            case "Products" -> new Product().setVisible(true);
            case "Categories" -> new Category().setVisible(true);
            case "Suppliers" -> new Supplier().setVisible(true);
            case "Warehouses" -> new Warehouse().setVisible(true);
            case "Orders" -> new Order(currentUserRole, currentUserEmail).setVisible(true);
            case "Customers" -> new Customer(currentUserRole).setVisible(true);
            case "Users" -> new User(currentUserRole).setVisible(true);
            case "View Orders" -> new ViewOrders(currentUserRole, currentUserEmail).setVisible(true);
            case "Logout" -> {
                setVisible(false);
                dispose();
                new login().setVisible(true);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Home("User", "user@example.com"));
    }
}
