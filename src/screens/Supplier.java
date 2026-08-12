package screens;
import screens.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import dao.ConnectionProvider;
import ui.UIStyle;
import ui.RoundedPanel;
import ui.ModernTableRenderer;
import ui.ErrorHandler;

/**
 * Manage Suppliers window.
 *
 * A supplier can deliver to several warehouses, and a warehouse can receive
 * from several suppliers -- a many-to-many relationship, resolved by the
 * supplier_warehouse junction table. The right-hand dual-list lets you move
 * warehouses between "Available" and "Linked" for the selected supplier.
 */
public class Supplier extends JFrame {

    private JTextField txtName, txtContactPerson, txtPhone, txtEmail, txtAddress;
    private JButton btnSave, btnUpdate, btnReset, btnClose, btnDelete;
    private JTable tableSupplier;
    private int supplierPk = 0;

    private DefaultListModel<String> availableModel, linkedModel;
    private JList<String> listAvailable, listLinked;

    public Supplier() {
        super("Manage Suppliers");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1400, 820);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BACKGROUND);

        createTopBar();
        createMainContent();

        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                loadTable();
            }
        });

        setVisible(true);
    }

    private void createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIStyle.BACKGROUND);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.SEPARATOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel title = new JLabel("Manage Suppliers");
        title.setFont(UIStyle.FONT_HEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);
    }

    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(UIStyle.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        createTablePanel(mainPanel);
        createSidePanel(mainPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createTablePanel(JPanel parent) {
        RoundedPanel tableCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        tableCard.setBackground(UIStyle.CARD_BG);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));

        JLabel tableTitle = new JLabel("Suppliers");
        tableTitle.setFont(UIStyle.FONT_SUBHEADING);
        tableTitle.setForeground(UIStyle.TEXT_DARK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        tableSupplier = new JTable(new DefaultTableModel(
            new Object[]{"ID", "Name", "Contact Person", "Phone", "Email", "Address"}, 0
        ));
        tableSupplier.setFont(UIStyle.FONT_BODY);
        tableSupplier.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableSupplier.setShowGrid(false);
        tableSupplier.setIntercellSpacing(new Dimension(0, 0));
        tableSupplier.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableSupplier.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableSupplier.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableSupplier.getTableHeader().setForeground(Color.WHITE);
        tableSupplier.getTableHeader().setPreferredSize(new Dimension(0, 40));

        tableSupplier.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectSupplier();
            }
        });

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(true);
        scrollWrapper.setBackground(UIStyle.CARD_BG);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        JScrollPane scroll = new JScrollPane(tableSupplier);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scroll.getVerticalScrollBar().setBackground(UIStyle.CARD_BG);
        scrollWrapper.add(scroll, BorderLayout.CENTER);
        tableCard.add(scrollWrapper, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        btnSave = createActionButton("Add", UIStyle.PRIMARY, e -> saveSupplier());
        JButton btnRefresh = createActionButton("Refresh", UIStyle.PRIMARY, e -> loadTable());
        btnUpdate = createActionButton("Update", UIStyle.PRIMARY, e -> updateSupplier());
        btnUpdate.setEnabled(false);
        btnDelete = createActionButton("Delete", UIStyle.ERROR, e -> deleteSupplier());
        btnDelete.setEnabled(false);
        btnReset = createActionButton("Reset", UIStyle.PRIMARY, e -> resetForm());
        btnClose = createActionButton("Close", UIStyle.PRIMARY, e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnClose);

        tableCard.add(buttonPanel, BorderLayout.SOUTH);

        parent.add(tableCard, BorderLayout.CENTER);
    }

    private void createSidePanel(JPanel parent) {
        // Two tabs instead of stacking both cards vertically -- each tab gets
        // the full side-panel height, so "Linked Warehouses" (and its >>/<<
        // buttons) can never be clipped off the bottom of the window again,
        // regardless of window size.
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIStyle.FONT_BODY_BOLD);
        tabs.setBackground(UIStyle.CARD_BG);
        tabs.setPreferredSize(new Dimension(420, 0));

        tabs.addTab("Supplier Details", wrapInScroll(createFormCard()));
        tabs.addTab("Linked Warehouses", createWarehouseLinkCard());

        parent.add(tabs, BorderLayout.EAST);
    }

    /** Pins a card to the top of a scroll pane so it never gets clipped even if the window is very short. */
    private JScrollPane wrapInScroll(JComponent comp) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(comp, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private RoundedPanel createFormCard() {
        RoundedPanel formCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        formCard.setBackground(UIStyle.CARD_BG);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel formTitle = new JLabel("Supplier Details");
        formTitle.setFont(UIStyle.FONT_SUBHEADING);
        formTitle.setForeground(UIStyle.TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(15));

        addFormField(formCard, "Name", txtName = new JTextField());
        addFormField(formCard, "Contact Person", txtContactPerson = new JTextField());
        addFormField(formCard, "Phone", txtPhone = new JTextField());
        addFormField(formCard, "Email", txtEmail = new JTextField());
        addFormField(formCard, "Address", txtAddress = new JTextField());

        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        formButtonPanel.setOpaque(false);
        formButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnFormSave = createActionButton("Save", UIStyle.PRIMARY, e -> saveSupplier());
        JButton btnFormClear = createActionButton("Clear", UIStyle.PRIMARY, e -> clearForm());
        formButtonPanel.add(btnFormSave);
        formButtonPanel.add(btnFormClear);
        formCard.add(formButtonPanel);

        return formCard;
    }

    private RoundedPanel createWarehouseLinkCard() {
        RoundedPanel card = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        card.setBackground(UIStyle.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));

        JLabel title = new JLabel("Linked Warehouses (select a supplier first)");
        title.setFont(UIStyle.FONT_SUBHEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel dualList = new JPanel(new GridLayout(1, 3, 8, 0));
        dualList.setOpaque(false);
        dualList.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        dualList.setPreferredSize(new Dimension(0, 200));

        availableModel = new DefaultListModel<>();
        listAvailable = new JList<>(availableModel);
        listAvailable.setFont(UIStyle.FONT_BODY);
        JPanel availPanel = new JPanel(new BorderLayout());
        availPanel.setOpaque(false);
        JLabel availLbl = new JLabel("Available", SwingConstants.CENTER);
        availLbl.setFont(UIStyle.FONT_SMALL);
        availPanel.add(availLbl, BorderLayout.NORTH);
        availPanel.add(new JScrollPane(listAvailable), BorderLayout.CENTER);

        JPanel arrowPanel = new JPanel();
        arrowPanel.setOpaque(false);
        arrowPanel.setLayout(new BoxLayout(arrowPanel, BoxLayout.Y_AXIS));
        arrowPanel.add(Box.createVerticalGlue());
        JButton btnLink = createActionButton(">>", UIStyle.PRIMARY, e -> linkSelectedWarehouse());
        JButton btnUnlink = createActionButton("<<", UIStyle.ERROR, e -> unlinkSelectedWarehouse());
        btnLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUnlink.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLink.setPreferredSize(new Dimension(60, UIStyle.BUTTON_HEIGHT));
        btnUnlink.setPreferredSize(new Dimension(60, UIStyle.BUTTON_HEIGHT));
        arrowPanel.add(btnLink);
        arrowPanel.add(Box.createVerticalStrut(10));
        arrowPanel.add(btnUnlink);
        arrowPanel.add(Box.createVerticalGlue());

        linkedModel = new DefaultListModel<>();
        listLinked = new JList<>(linkedModel);
        listLinked.setFont(UIStyle.FONT_BODY);
        JPanel linkedPanel = new JPanel(new BorderLayout());
        linkedPanel.setOpaque(false);
        JLabel linkedLbl = new JLabel("Linked", SwingConstants.CENTER);
        linkedLbl.setFont(UIStyle.FONT_SMALL);
        linkedPanel.add(linkedLbl, BorderLayout.NORTH);
        linkedPanel.add(new JScrollPane(listLinked), BorderLayout.CENTER);

        dualList.add(availPanel);
        dualList.add(arrowPanel);
        dualList.add(linkedPanel);

        card.add(dualList, BorderLayout.CENTER);

        return card;
    }

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
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIStyle.INPUT_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(field);
        parent.add(Box.createVerticalStrut(12));
    }

    private JButton createActionButton(String text, Color bgColor, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyle.getFont(Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, UIStyle.BUTTON_HEIGHT));
        if (listener != null) btn.addActionListener(listener);
        return btn;
    }

    private boolean validateFields() {
        return txtName.getText().trim().isEmpty();
    }

    private void loadTable() {
        DefaultTableModel model = (DefaultTableModel) tableSupplier.getModel();
        model.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading suppliers");
                return;
            }
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM supplier ORDER BY name");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("supplier_pk"), rs.getString("name"), rs.getString("contactPerson"),
                        rs.getString("phone"), rs.getString("email"), rs.getString("address")
                });
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading suppliers");
        }
        btnUpdate.setEnabled(false);
        btnSave.setEnabled(true);
        btnDelete.setEnabled(false);
    }

    private void selectSupplier() {
        int index = tableSupplier.getSelectedRow();
        if (index < 0) return;

        supplierPk = (int) tableSupplier.getValueAt(index, 0);
        txtName.setText(tableSupplier.getValueAt(index, 1).toString());
        txtContactPerson.setText(safe(tableSupplier.getValueAt(index, 2)));
        txtPhone.setText(safe(tableSupplier.getValueAt(index, 3)));
        txtEmail.setText(safe(tableSupplier.getValueAt(index, 4)));
        txtAddress.setText(safe(tableSupplier.getValueAt(index, 5)));

        btnSave.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);

        loadWarehouseLinks();
    }

    private String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    /** Loads Available (not yet linked) and Linked warehouse lists for the selected supplier. */
    private void loadWarehouseLinks() {
        availableModel.clear();
        linkedModel.clear();
        if (supplierPk == 0) return;

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) return;

            List<Integer> linkedIds = new ArrayList<>();
            PreparedStatement psLinked = con.prepareStatement(
                    "SELECT warehouse.warehouse_pk, warehouse.name FROM supplier_warehouse " +
                    "INNER JOIN warehouse ON supplier_warehouse.warehouse_fk = warehouse.warehouse_pk " +
                    "WHERE supplier_warehouse.supplier_fk=? ORDER BY warehouse.name");
            psLinked.setInt(1, supplierPk);
            ResultSet rsLinked = psLinked.executeQuery();
            while (rsLinked.next()) {
                linkedModel.addElement(rsLinked.getInt("warehouse_pk") + " - " + rsLinked.getString("name"));
                linkedIds.add(rsLinked.getInt("warehouse_pk"));
            }

            Statement st = con.createStatement();
            ResultSet rsAll = st.executeQuery("SELECT warehouse_pk, name FROM warehouse ORDER BY name");
            while (rsAll.next()) {
                int id = rsAll.getInt("warehouse_pk");
                if (!linkedIds.contains(id)) {
                    availableModel.addElement(id + " - " + rsAll.getString("name"));
                }
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading warehouse links");
        }
    }

    private void linkSelectedWarehouse() {
        if (supplierPk == 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier first.", "No Supplier Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String selected = listAvailable.getSelectedValue();
        if (selected == null) return;
        int warehousePk = Integer.parseInt(selected.split(" - ")[0]);

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "linking warehouse");
                return;
            }
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO supplier_warehouse (supplier_fk, warehouse_fk, sinceDate) VALUES (?, ?, CURDATE())");
            ps.setInt(1, supplierPk);
            ps.setInt(2, warehousePk);
            ps.executeUpdate();
            loadWarehouseLinks();
        } catch (Exception e) {
            ErrorHandler.showError(e, "linking warehouse");
        }
    }

    private void unlinkSelectedWarehouse() {
        String selected = listLinked.getSelectedValue();
        if (selected == null) return;
        int warehousePk = Integer.parseInt(selected.split(" - ")[0]);

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "unlinking warehouse");
                return;
            }
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM supplier_warehouse WHERE supplier_fk=? AND warehouse_fk=?");
            ps.setInt(1, supplierPk);
            ps.setInt(2, warehousePk);
            ps.executeUpdate();
            loadWarehouseLinks();
        } catch (Exception e) {
            ErrorHandler.showError(e, "unlinking warehouse");
        }
    }

    private void saveSupplier() {
        if (validateFields()) {
            JOptionPane.showMessageDialog(this, "Name field is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "saving supplier");
                return;
            }
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO supplier (name, contactPerson, phone, email, address) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtContactPerson.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtEmail.getText().trim());
            ps.setString(5, txtAddress.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "saving supplier");
        }
    }

    private void updateSupplier() {
        if (validateFields()) {
            JOptionPane.showMessageDialog(this, "Name field is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "updating supplier");
                return;
            }
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE supplier SET name=?, contactPerson=?, phone=?, email=?, address=? WHERE supplier_pk=?");
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtContactPerson.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtEmail.getText().trim());
            ps.setString(5, txtAddress.getText().trim());
            ps.setInt(6, supplierPk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "updating supplier");
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtContactPerson.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        supplierPk = 0;
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        tableSupplier.clearSelection();
        availableModel.clear();
        linkedModel.clear();
    }

    /**
     * Deletes the currently selected supplier after confirmation. This
     * cascades (ON DELETE CASCADE) to remove its supplier_warehouse links;
     * any product that had this as its primary supplier survives with
     * supplier_fk cleared to NULL (ON DELETE SET NULL), i.e. "no supplier".
     */
    private void deleteSupplier() {
        if (supplierPk == 0) {
            JOptionPane.showMessageDialog(this, "Select a supplier first.", "No Supplier Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + txtName.getText() + "\"? Its warehouse links will be removed too, " +
                "and any products supplied by them will show \"No Supplier\". This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "deleting supplier");
                return;
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM supplier WHERE supplier_pk=?");
            ps.setInt(1, supplierPk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "deleting supplier");
        }
    }

    private void resetForm() {
        clearForm();
        loadTable();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Supplier::new);
    }
}
