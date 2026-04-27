package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
// Task 4.7: Customer recommendations engine
public class RecommendationsTab extends JPanel {

    private JComboBox<String> customerCombo;
    private JLabel totalSpentLabel;
    private JLabel totalPurchasesLabel;
    private JLabel lastPurchaseLabel;
    private JLabel favouriteGenreLabel;
    private JTable recommendationsTable;
    private DefaultTableModel tableModel;

    // Store customer IDs with their display names
    private Vector<CustomerItem> customerList;

    public RecommendationsTab() {
        setLayout(new BorderLayout());

        // Top Panel - Customer Selection
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Select Customer"));

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Customer:"));
        customerCombo = new JComboBox<>();
        customerCombo.setPreferredSize(new Dimension(300, 25));
        selectionPanel.add(customerCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadRecommendations());
        selectionPanel.add(refreshButton);

        topPanel.add(selectionPanel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);

        // Middle Panel - Spending Summary & Favourite Genre
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Customer Spending Summary"));
        summaryPanel.setPreferredSize(new Dimension(0, 80));

        totalSpentLabel = new JLabel("Total Spent: --");
        totalSpentLabel.setFont(new Font("Arial", Font.BOLD, 12));
        totalPurchasesLabel = new JLabel("Total Purchases: --");
        lastPurchaseLabel = new JLabel("Last Purchase: --");
        favouriteGenreLabel = new JLabel("Favourite Genre: --");
        favouriteGenreLabel.setFont(new Font("Arial", Font.BOLD, 12));
        favouriteGenreLabel.setForeground(new Color(0, 100, 0));

        summaryPanel.add(totalSpentLabel);
        summaryPanel.add(totalPurchasesLabel);
        summaryPanel.add(lastPurchaseLabel);
        summaryPanel.add(favouriteGenreLabel);

        add(summaryPanel, BorderLayout.CENTER);

        // Bottom Panel - Recommendations Table
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Recommended Tracks"));

        String[] columns = {"Track Name", "Album", "Artist", "Genre", "Unit Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recommendationsTable = new JTable(tableModel);
        recommendationsTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        recommendationsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        recommendationsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        JScrollPane scrollPane = new JScrollPane(recommendationsTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load customers into dropdown
        loadCustomers();

        // Add listener for customer selection
        customerCombo.addActionListener(e -> loadRecommendations());
    }

    private void loadCustomers() {
        customerList = new Vector<>();
        customerCombo.removeAllItems();

        String sql = "SELECT CustomerId, FirstName, LastName, Email FROM Customer ORDER BY CustomerId";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("CustomerId");
                String name = rs.getString("FirstName") + " " + rs.getString("LastName");
                CustomerItem item = new CustomerItem(id, name);
                customerList.add(item);
                customerCombo.addItem(name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }

    private void loadRecommendations() {
        int selectedIndex = customerCombo.getSelectedIndex();
        if (selectedIndex == -1 || customerList.isEmpty()) {
            return;
        }

        int customerId = customerList.get(selectedIndex).id;

        // Load spending summary
        loadSpendingSummary(customerId);

        // Load favourite genre
        String favouriteGenre = loadFavouriteGenre(customerId);

        // Load recommendations
        loadRecommendations(customerId, favouriteGenre);
    }

    private void loadSpendingSummary(int customerId) {
        String sql =
                "SELECT " +
                        "   COUNT(i.InvoiceId) AS TotalPurchases, " +
                        "   SUM(i.Total) AS TotalSpent, " +
                        "   MAX(i.InvoiceDate) AS LastPurchaseDate " +
                        "FROM Invoice i " +
                        "WHERE i.CustomerId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int purchaseCount = rs.getInt("TotalPurchases");
                double totalSpent = rs.getDouble("TotalSpent");
                Date lastPurchase = rs.getDate("LastPurchaseDate");

                totalPurchasesLabel.setText("Total Purchases: " + purchaseCount);
                totalSpentLabel.setText(String.format("Total Spent: $%.2f", totalSpent));
                lastPurchaseLabel.setText("Last Purchase: " + (lastPurchase == null ? "Never" : lastPurchase.toString()));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String loadFavouriteGenre(int customerId) {
        String sql =
                "SELECT " +
                        "   g.Name AS GenreName, " +
                        "   COUNT(il.InvoiceLineId) AS PurchaseCount " +
                        "FROM Invoice i " +
                        "JOIN InvoiceLine il ON i.InvoiceId = il.InvoiceId " +
                        "JOIN Track t ON il.TrackId = t.TrackId " +
                        "JOIN Genre g ON t.GenreId = g.GenreId " +
                        "WHERE i.CustomerId = ? " +
                        "GROUP BY g.GenreId, g.Name " +
                        "ORDER BY PurchaseCount DESC " +
                        "LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String genre = rs.getString("GenreName");
                favouriteGenreLabel.setText("Favourite Genre: " + genre);
                return genre;
            } else {
                favouriteGenreLabel.setText("Favourite Genre: No purchases yet");
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            favouriteGenreLabel.setText("Favourite Genre: Error");
            return null;
        }
    }

    private void loadRecommendations(int customerId, String favouriteGenre) {
        tableModel.setRowCount(0);

        if (favouriteGenre == null || favouriteGenre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "This customer has no purchase history. Cannot generate recommendations.",
                    "No History",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // SQL to find tracks that:
        // 1. Are in the customer's favourite genre
        // 2. The customer has NOT already purchased
        // 3. Exclude tracks already in customer's playlist (if any)
        String sql =
                "SELECT " +
                        "   t.Name AS TrackName, " +
                        "   a.Title AS AlbumTitle, " +
                        "   ar.Name AS ArtistName, " +
                        "   g.Name AS GenreName, " +
                        "   t.UnitPrice " +
                        "FROM Track t " +
                        "JOIN Album a ON t.AlbumId = a.AlbumId " +
                        "JOIN Artist ar ON a.ArtistId = ar.ArtistId " +
                        "JOIN Genre g ON t.GenreId = g.GenreId " +
                        "WHERE g.Name = ? " +
                        "AND t.TrackId NOT IN ( " +
                        "    SELECT il.TrackId " +
                        "    FROM Invoice i " +
                        "    JOIN InvoiceLine il ON i.InvoiceId = il.InvoiceId " +
                        "    WHERE i.CustomerId = ? " +
                        ") " +
                        "ORDER BY RAND() " +
                        "LIMIT 15";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, favouriteGenre);
            pstmt.setInt(2, customerId);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("TrackName"));
                row.add(rs.getString("AlbumTitle"));
                row.add(rs.getString("ArtistName"));
                row.add(rs.getString("GenreName"));
                row.add(String.format("$%.2f", rs.getDouble("UnitPrice")));
                tableModel.addRow(row);
                count++;
            }

            if (count == 0) {
                Vector<Object> row = new Vector<>();
                row.add("");
                row.add("No");
                row.add("inactive");
                row.add("customers");
                row.add("found");
                row.add("");
                row.add("");
                row.add("");
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading recommendations: " + e.getMessage());
        }
    }

    // Helper class to store customer combo box items
    class CustomerItem {
        int id;
        String name;

        CustomerItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}