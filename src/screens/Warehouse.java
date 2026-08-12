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
 * Manage Warehouses window.
 *
 * A warehouse is a new top-level entity (normalized out of the old free-text
 * Warehouse/City columns on `product`). Each warehouse can have several
 * shelves (1:N) -- shelves are managed from the panel on the right once a
 * warehouse row is selected.
 */
public class Warehouse extends JFrame {

    private JTextField txtName, txtCity, txtAddress;
    private JButton btnSave, btnUpdate, btnReset, btnClose, btnDelete;
    private JTable tableWarehouse;
    private int warehousePk = 0;

    private JList<String> listShelves;
    private DefaultListModel<String> shelfListModel;
    private JTextField txtShelfCode;
    private JButton btnAddShelf, btnRemoveShelf;

    public Warehouse() {
        super("Manage Warehouses");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1300, 780);
        setMinimumSize(new Dimension(1050, 620));
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

        JLabel title = new JLabel("Manage Warehouses");
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

        JLabel tableTitle = new JLabel("Warehouses");
        tableTitle.setFont(UIStyle.FONT_SUBHEADING);
        tableTitle.setForeground(UIStyle.TEXT_DARK);
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        tableWarehouse = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "City", "Address"}, 0));
        tableWarehouse.setFont(UIStyle.FONT_BODY);
        tableWarehouse.setRowHeight(UIStyle.TABLE_ROW_HEIGHT);
        tableWarehouse.setShowGrid(false);
        tableWarehouse.setIntercellSpacing(new Dimension(0, 0));
        tableWarehouse.setDefaultRenderer(Object.class, new ModernTableRenderer());
        tableWarehouse.getTableHeader().setFont(UIStyle.FONT_BODY_BOLD);
        tableWarehouse.getTableHeader().setBackground(UIStyle.PRIMARY);
        tableWarehouse.getTableHeader().setForeground(Color.WHITE);
        tableWarehouse.getTableHeader().setPreferredSize(new Dimension(0, 40));

        tableWarehouse.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectWarehouse();
            }
        });

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(true);
        scrollWrapper.setBackground(UIStyle.CARD_BG);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        JScrollPane scroll = new JScrollPane(tableWarehouse);
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

        btnSave = createActionButton("Add", UIStyle.PRIMARY, e -> saveWarehouse());
        JButton btnRefresh = createActionButton("Refresh", UIStyle.PRIMARY, e -> loadTable());
        btnUpdate = createActionButton("Update", UIStyle.PRIMARY, e -> updateWarehouse());
        btnUpdate.setEnabled(false);
        btnDelete = createActionButton("Delete", UIStyle.ERROR, e -> deleteWarehouse());
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
        // the full side-panel height, so the Shelves list can never be
        // clipped off the bottom of the window again, regardless of size.
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIStyle.FONT_BODY_BOLD);
        tabs.setBackground(UIStyle.CARD_BG);
        tabs.setPreferredSize(new Dimension(380, 0));

        tabs.addTab("Warehouse Details", wrapInScroll(createFormCard()));
        tabs.addTab("Shelves", createShelfCard());

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
        formCard.setMaximumSize(new Dimension(380, 320));

        JLabel formTitle = new JLabel("Warehouse Details");
        formTitle.setFont(UIStyle.FONT_SUBHEADING);
        formTitle.setForeground(UIStyle.TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(15));

        addFormField(formCard, "Name", txtName = new JTextField());
        addFormField(formCard, "City", txtCity = new JTextField());
        addFormField(formCard, "Address", txtAddress = new JTextField());

        JPanel formButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        formButtonPanel.setOpaque(false);
        formButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnFormSave = createActionButton("Save", UIStyle.PRIMARY, e -> saveWarehouse());
        JButton btnFormClear = createActionButton("Clear", UIStyle.PRIMARY, e -> clearForm());
        formButtonPanel.add(btnFormSave);
        formButtonPanel.add(btnFormClear);
        formCard.add(formButtonPanel);

        return formCard;
    }

    private RoundedPanel createShelfCard() {
        RoundedPanel shelfCard = new RoundedPanel(UIStyle.RADIUS_MEDIUM, true);
        shelfCard.setBackground(UIStyle.CARD_BG);
        shelfCard.setLayout(new BorderLayout());
        shelfCard.setBorder(BorderFactory.createEmptyBorder(UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE, UIStyle.PADDING_LARGE));
        shelfCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Shelves (select a warehouse first)");
        title.setFont(UIStyle.FONT_SUBHEADING);
        title.setForeground(UIStyle.TEXT_DARK);
        shelfCard.add(title, BorderLayout.NORTH);

        shelfListModel = new DefaultListModel<>();
        listShelves = new JList<>(shelfListModel);
        listShelves.setFont(UIStyle.FONT_BODY);
        JScrollPane shelfScroll = new JScrollPane(listShelves);
        shelfScroll.setPreferredSize(new Dimension(0, 160));
        shelfCard.add(shelfScroll, BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new BorderLayout(8, 0));
        addPanel.setOpaque(false);
        addPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        txtShelfCode = new JTextField();
        txtShelfCode.setFont(UIStyle.FONT_BODY);
        txtShelfCode.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.SEPARATOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        addPanel.add(txtShelfCode, BorderLayout.CENTER);

        JPanel shelfButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        shelfButtons.setOpaque(false);
        btnAddShelf = createActionButton("Add Shelf", UIStyle.PRIMARY, e -> addShelf());
        btnRemoveShelf = createActionButton("Remove", UIStyle.ERROR, e -> removeShelf());
        shelfButtons.add(btnAddShelf);
        shelfButtons.add(btnRemoveShelf);

        JPanel shelfBottom = new JPanel();
        shelfBottom.setOpaque(false);
        shelfBottom.setLayout(new BoxLayout(shelfBottom, BoxLayout.Y_AXIS));
        shelfBottom.add(addPanel);
        shelfBottom.add(shelfButtons);
        shelfCard.add(shelfBottom, BorderLayout.SOUTH);

        return shelfCard;
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
        btn.setPreferredSize(new Dimension(100, UIStyle.BUTTON_HEIGHT));
        if (listener != null) btn.addActionListener(listener);
        return btn;
    }

    private boolean validateFields() {
        return txtName.getText().trim().isEmpty();
    }

    private void loadTable() {
        DefaultTableModel model = (DefaultTableModel) tableWarehouse.getModel();
        model.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "loading warehouses");
                return;
            }
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM warehouse ORDER BY name");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("warehouse_pk"), rs.getString("name"),
                        rs.getString("city"), rs.getString("address")
                });
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading warehouses");
        }
        btnUpdate.setEnabled(false);
        btnSave.setEnabled(true);
        btnDelete.setEnabled(false);
    }

    private void selectWarehouse() {
        int index = tableWarehouse.getSelectedRow();
        if (index < 0) return;

        warehousePk = (int) tableWarehouse.getValueAt(index, 0);
        txtName.setText(tableWarehouse.getValueAt(index, 1).toString());
        Object city = tableWarehouse.getValueAt(index, 2);
        Object address = tableWarehouse.getValueAt(index, 3);
        txtCity.setText(city == null ? "" : city.toString());
        txtAddress.setText(address == null ? "" : address.toString());

        btnSave.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);

        loadShelvesForWarehouse();
    }

    private void loadShelvesForWarehouse() {
        shelfListModel.clear();
        if (warehousePk == 0) return;
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) return;
            PreparedStatement ps = con.prepareStatement(
                    "SELECT shelf_pk, code FROM shelf WHERE warehouse_fk=? ORDER BY code");
            ps.setInt(1, warehousePk);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                shelfListModel.addElement(rs.getInt("shelf_pk") + " - " + rs.getString("code"));
            }
        } catch (Exception e) {
            ErrorHandler.showError(e, "loading shelves");
        }
    }

    private void addShelf() {
        if (warehousePk == 0) {
            JOptionPane.showMessageDialog(this, "Select a warehouse first.", "No Warehouse Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String code = txtShelfCode.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a shelf code (e.g. A1).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "adding shelf");
                return;
            }
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO shelf (code, warehouse_fk) VALUES (?, ?)");
            ps.setString(1, code);
            ps.setInt(2, warehousePk);
            ps.executeUpdate();
            txtShelfCode.setText("");
            loadShelvesForWarehouse();
        } catch (Exception e) {
            ErrorHandler.showError(e, "adding shelf");
        }
    }

    private void removeShelf() {
        String selected = listShelves.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a shelf to remove.", "No Shelf Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int shelfPk = Integer.parseInt(selected.split(" - ")[0]);
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "removing shelf");
                return;
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM shelf WHERE shelf_pk=?");
            ps.setInt(1, shelfPk);
            ps.executeUpdate();
            loadShelvesForWarehouse();
        } catch (Exception e) {
            ErrorHandler.showError(e, "removing shelf");
        }
    }

    private void saveWarehouse() {
        if (validateFields()) {
            JOptionPane.showMessageDialog(this, "Name field is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "saving warehouse");
                return;
            }
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO warehouse (name, city, address) VALUES (?, ?, ?)");
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtCity.getText().trim());
            ps.setString(3, txtAddress.getText().trim());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Warehouse added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "saving warehouse");
        }
    }

    private void updateWarehouse() {
        if (validateFields()) {
            JOptionPane.showMessageDialog(this, "Name field is required", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "updating warehouse");
                return;
            }
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE warehouse SET name=?, city=?, address=? WHERE warehouse_pk=?");
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtCity.getText().trim());
            ps.setString(3, txtAddress.getText().trim());
            ps.setInt(4, warehousePk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Warehouse updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "updating warehouse");
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtCity.setText("");
        txtAddress.setText("");
        warehousePk = 0;
        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        tableWarehouse.clearSelection();
        shelfListModel.clear();
    }

    /**
     * Deletes the currently selected warehouse after confirmation. This
     * cascades (ON DELETE CASCADE) to remove its shelves and any
     * supplier_warehouse links; products that were on those shelves survive
     * with shelf_fk cleared to NULL (ON DELETE SET NULL), i.e. "unassigned".
     */
    private void deleteWarehouse() {
        if (warehousePk == 0) {
            JOptionPane.showMessageDialog(this, "Select a warehouse first.", "No Warehouse Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + txtName.getText() + "\"? Its shelves and supplier links will be removed too, " +
                "and any products stored there will become unassigned. This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = ConnectionProvider.getCon()) {
            if (con == null) {
                ErrorHandler.showError(new Exception("Database connection failed"), "deleting warehouse");
                return;
            }
            PreparedStatement ps = con.prepareStatement("DELETE FROM warehouse WHERE warehouse_pk=?");
            ps.setInt(1, warehousePk);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Warehouse deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (Exception e) {
            ErrorHandler.showError(e, "deleting warehouse");
        }
    }

    private void resetForm() {
        clearForm();
        loadTable();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Warehouse::new);
    }
}
