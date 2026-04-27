package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
// Task 4.5 & 4.6: Customer CRUD and inactive customers
public class CustomersTab extends JPanel {

    private JTable activeCustomerTable;
    private DefaultTableModel activeTableModel;

    private JTable inactiveCustomerTable;
    private DefaultTableModel inactiveTableModel;
    private JTextField inactiveSearchField;

    private JTextField searchField;
    private JButton searchButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;

    public CustomersTab() {
        setLayout(new BorderLayout());

        JTabbedPane innerTabPane = new JTabbedPane();

        JPanel activePanel = createActiveCustomersPanel();
        innerTabPane.addTab("Active Customers", activePanel);

        JPanel inactivePanel = createInactiveCustomersPanel();
        innerTabPane.addTab("Inactive Customers", inactivePanel);

        add(innerTabPane, BorderLayout.CENTER);
    }

    private JPanel createActiveCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        searchButton = new JButton("Search");
        searchPanel.add(searchButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("➕ Add Customer");
        editButton = new JButton("✏️ Edit Customer");
        deleteButton = new JButton("🗑️ Delete Customer");
        refreshButton = new JButton("🔄 Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Customer ID", "First Name", "Last Name", "Email", "Phone", "Country", "City", "Address"};
        activeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activeCustomerTable = new JTable(activeTableModel);
        activeCustomerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(activeCustomerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadActiveCustomers();

        // Button Actions
        addButton.addActionListener(e -> openAddDialog());
        editButton.addActionListener(e -> openEditDialog());
        deleteButton.addActionListener(e -> deleteCustomer());
        refreshButton.addActionListener(e -> loadActiveCustomers());
        searchButton.addActionListener(e -> searchActiveCustomers());

        // Double-click to edit
        activeCustomerTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog();
                }
            }
        });

        return panel;
    }

    private JPanel createInactiveCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search Inactive Customers:"));
        inactiveSearchField = new JTextField(20);
        topPanel.add(inactiveSearchField);

        JButton inactiveSearchButton = new JButton("Search");
        inactiveSearchButton.addActionListener(e -> loadInactiveCustomers());
        topPanel.add(inactiveSearchButton);

        JButton inactiveRefreshButton = new JButton("Refresh");
        inactiveRefreshButton.addActionListener(e -> loadInactiveCustomers());
        topPanel.add(inactiveRefreshButton);

        panel.add(topPanel, BorderLayout.NORTH);

        JLabel infoLabel = new JLabel("Customers with no invoices OR no purchases in last 2 years");
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(infoLabel);
        panel.add(infoPanel, BorderLayout.SOUTH);

        String[] columns = {"Customer ID", "First Name", "Last Name", "Email", "Phone", "Country", "Last Invoice Date", "Status"};
        inactiveTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inactiveCustomerTable = new JTable(inactiveTableModel);
        JScrollPane scrollPane = new JScrollPane(inactiveCustomerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadInactiveCustomers();

        return panel;
    }

    private void loadActiveCustomers() {
        loadActiveCustomersWithFilter(null);
    }

    private void searchActiveCustomers() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadActiveCustomers();
        } else {
            loadActiveCustomersWithFilter(searchTerm);
        }
    }

    private void loadActiveCustomersWithFilter(String searchTerm) {
        activeTableModel.setRowCount(0);

        String sql;
        if (searchTerm == null || searchTerm.isEmpty()) {
            sql = "SELECT CustomerId, FirstName, LastName, Email, Phone, Country, City, Address " +
                    "FROM Customer ORDER BY CustomerId";
        } else {
            sql = "SELECT CustomerId, FirstName, LastName, Email, Phone, Country, City, Address " +
                    "FROM Customer WHERE FirstName LIKE '%" + searchTerm + "%' " +
                    "OR LastName LIKE '%" + searchTerm + "%' " +
                    "OR Email LIKE '%" + searchTerm + "%' " +
                    "OR Country LIKE '%" + searchTerm + "%' " +
                    "ORDER BY CustomerId";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("CustomerId"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("Email"));
                row.add(rs.getString("Phone"));
                row.add(rs.getString("Country"));
                row.add(rs.getString("City"));
                row.add(rs.getString("Address"));
                activeTableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }

    private void loadInactiveCustomers() {
        inactiveTableModel.setRowCount(0);

        String searchTerm = inactiveSearchField.getText().trim();

        String sql;

        if (searchTerm.isEmpty()) {
            sql =
                    "SELECT " +
                            "   c.CustomerId, " +
                            "   c.FirstName, " +
                            "   c.LastName, " +
                            "   c.Email, " +
                            "   c.Phone, " +
                            "   c.Country, " +
                            "   MAX(i.InvoiceDate) AS LastInvoiceDate, " +
                            "   CASE " +
                            "       WHEN MAX(i.InvoiceDate) IS NULL THEN 'No purchases ever' " +
                            "       WHEN MAX(i.InvoiceDate) < DATE_SUB(CURDATE(), INTERVAL 2 YEAR) THEN 'Inactive (>2 years)' " +
                            "       ELSE 'Active' " +
                            "   END AS Status " +
                            "FROM Customer c " +
                            "LEFT JOIN Invoice i ON c.CustomerId = i.CustomerId " +
                            "GROUP BY c.CustomerId, c.FirstName, c.LastName, c.Email, c.Phone, c.Country " +
                            "HAVING MAX(i.InvoiceDate) IS NULL OR MAX(i.InvoiceDate) < DATE_SUB(CURDATE(), INTERVAL 2 YEAR) " +
                            "ORDER BY c.CustomerId";
        } else {
            sql =
                    "SELECT " +
                            "   c.CustomerId, " +
                            "   c.FirstName, " +
                            "   c.LastName, " +
                            "   c.Email, " +
                            "   c.Phone, " +
                            "   c.Country, " +
                            "   MAX(i.InvoiceDate) AS LastInvoiceDate, " +
                            "   CASE " +
                            "       WHEN MAX(i.InvoiceDate) IS NULL THEN 'No purchases ever' " +
                            "       WHEN MAX(i.InvoiceDate) < DATE_SUB(CURDATE(), INTERVAL 2 YEAR) THEN 'Inactive (>2 years)' " +
                            "       ELSE 'Active' " +
                            "   END AS Status " +
                            "FROM Customer c " +
                            "LEFT JOIN Invoice i ON c.CustomerId = i.CustomerId " +
                            "WHERE c.FirstName LIKE '%" + searchTerm + "%' " +
                            "   OR c.LastName LIKE '%" + searchTerm + "%' " +
                            "   OR c.Email LIKE '%" + searchTerm + "%' " +
                            "   OR c.Country LIKE '%" + searchTerm + "%' " +
                            "GROUP BY c.CustomerId, c.FirstName, c.LastName, c.Email, c.Phone, c.Country " +
                            "HAVING MAX(i.InvoiceDate) IS NULL OR MAX(i.InvoiceDate) < DATE_SUB(CURDATE(), INTERVAL 2 YEAR) " +
                            "ORDER BY c.CustomerId";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("CustomerId"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("Email"));
                row.add(rs.getString("Phone"));
                row.add(rs.getString("Country"));

                Date lastInvoiceDate = rs.getDate("LastInvoiceDate");
                String lastInvoiceStr = (lastInvoiceDate == null) ? "Never" : lastInvoiceDate.toString();
                row.add(lastInvoiceStr);

                row.add(rs.getString("Status"));
                inactiveTableModel.addRow(row);
            }

            if (inactiveTableModel.getRowCount() == 0) {
                Vector<Object> row = new Vector<>();
                row.add("");
                row.add("No");
                row.add("inactive");
                row.add("customers");
                row.add("found");
                row.add("");
                row.add("");
                row.add("");
                inactiveTableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inactive customers: " + e.getMessage());
        }
    }



    private void openAddDialog() {
        CustomerDialog dialog = new CustomerDialog(this, null);
        dialog.setVisible(true);
        loadActiveCustomers();
        loadInactiveCustomers();
    }

    private void openEditDialog() {
        int selectedRow = activeCustomerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.");
            return;
        }

        int customerId = (int) activeTableModel.getValueAt(selectedRow, 0);
        String firstName = (String) activeTableModel.getValueAt(selectedRow, 1);
        String lastName = (String) activeTableModel.getValueAt(selectedRow, 2);
        String email = (String) activeTableModel.getValueAt(selectedRow, 3);
        String phone = (String) activeTableModel.getValueAt(selectedRow, 4);
        String country = (String) activeTableModel.getValueAt(selectedRow, 5);
        String city = (String) activeTableModel.getValueAt(selectedRow, 6);
        String address = (String) activeTableModel.getValueAt(selectedRow, 7);

        Customer existingCustomer = new Customer(customerId, firstName, lastName, email, phone, country, city, address);
        CustomerDialog dialog = new CustomerDialog(this, existingCustomer);
        dialog.setVisible(true);
        loadActiveCustomers();
        loadInactiveCustomers();
    }

    private void deleteCustomer() {
        int selectedRow = activeCustomerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.");
            return;
        }

        int customerId = (int) activeTableModel.getValueAt(selectedRow, 0);
        String customerName = activeTableModel.getValueAt(selectedRow, 1) + " " + activeTableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete customer: " + customerName + "?\n" +
                        "This will also delete their invoices and invoice lines!",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Customer WHERE CustomerId = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, customerId);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                    loadActiveCustomers();
                    loadInactiveCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete customer.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage());
            }
        }
    }

    // Inner class to hold customer data
    class Customer {
        int id;
        String firstName;
        String lastName;
        String email;
        String phone;
        String country;
        String city;
        String address;

        Customer(int id, String firstName, String lastName, String email,
                 String phone, String country, String city, String address) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.country = country;
            this.city = city;
            this.address = address;
        }
    }
}