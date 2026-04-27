package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
// Task 4.1 & 4.2: Employee table with filter
public class EmployeesTab extends JPanel {

    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField filterTextField;

    public EmployeesTab() {
        setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterTextField = new JTextField(20);
        filterPanel.add(filterTextField);

        JButton filterButton = new JButton("Search");
        filterButton.addActionListener(e -> loadEmployees());
        filterPanel.add(filterButton);

        add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"First Name", "Last Name", "Title", "City", "Country", "Phone", "Supervisor", "Active"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        add(scrollPane, BorderLayout.CENTER);

        loadEmployees();
    }

    private void loadEmployees() {
        tableModel.setRowCount(0);

        String filter = filterTextField.getText().trim();
        String sql;

        if (filter.isEmpty()) {
            sql = "SELECT e.FirstName, e.LastName, e.Title, e.City, e.Country, e.Phone, " +
                    "CONCAT(m.FirstName, ' ', m.LastName) AS Supervisor, " +
                    "CASE WHEN e.EmployeeId IN (SELECT DISTINCT ReportsTo FROM Employee WHERE ReportsTo IS NOT NULL) " +
                    "THEN 'Yes' ELSE 'No' END AS Active " +
                    "FROM Employee e " +
                    "LEFT JOIN Employee m ON e.ReportsTo = m.EmployeeId";
        } else {
            sql = "SELECT e.FirstName, e.LastName, e.Title, e.City, e.Country, e.Phone, " +
                    "CONCAT(m.FirstName, ' ', m.LastName) AS Supervisor, " +
                    "CASE WHEN e.EmployeeId IN (SELECT DISTINCT ReportsTo FROM Employee WHERE ReportsTo IS NOT NULL) " +
                    "THEN 'Yes' ELSE 'No' END AS Active " +
                    "FROM Employee e " +
                    "LEFT JOIN Employee m ON e.ReportsTo = m.EmployeeId " +
                    "WHERE e.FirstName LIKE '%" + filter + "%' OR " +
                    "e.LastName LIKE '%" + filter + "%' OR " +
                    "e.City LIKE '%" + filter + "%'";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("Title"));
                row.add(rs.getString("City"));
                row.add(rs.getString("Country"));
                row.add(rs.getString("Phone"));
                row.add(rs.getString("Supervisor") == null ? "None" : rs.getString("Supervisor"));
                row.add(rs.getString("Active"));
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading employees: " + e.getMessage());
        }
    }
}
