package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ReportTab extends JPanel {

    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JLabel lastUpdatedLabel;

    public ReportTab() {
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Genre Revenue Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        String[] columns = {"Genre", "Total Revenue ($)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 14));
        reportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        reportTable.setRowHeight(25);

        reportTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lastUpdatedLabel = new JLabel();
        bottomPanel.add(lastUpdatedLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        loadReport();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        loadReport();
    }

    private void loadReport() {
        tableModel.setRowCount(0); // Clear existing rows

        String sql =
                "SELECT " +
                        "   g.Name AS Genre, " +
                        "   ROUND(SUM(il.UnitPrice * il.Quantity), 2) AS TotalRevenue " +
                        "FROM Genre g " +
                        "JOIN Track t ON g.GenreId = t.GenreId " +
                        "JOIN InvoiceLine il ON t.TrackId = il.TrackId " +
                        "JOIN Invoice i ON il.InvoiceId = i.InvoiceId " +
                        "GROUP BY g.GenreId, g.Name " +
                        "ORDER BY TotalRevenue DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            double totalAllGenres = 0;

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                String genre = rs.getString("Genre");
                double revenue = rs.getDouble("TotalRevenue");

                row.add(genre);
                row.add(String.format("$%.2f", revenue));
                tableModel.addRow(row);

                totalAllGenres += revenue;
            }

            Vector<Object> summaryRow = new Vector<>();
            summaryRow.add("TOTAL");
            summaryRow.add(String.format("$%.2f", totalAllGenres));
            tableModel.addRow(summaryRow);

            lastUpdatedLabel.setText("Last updated: " + new java.util.Date().toString());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading revenue report: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
