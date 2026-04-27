package com.chinook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class TracksTab extends JPanel {

    private JTable tracksTable;
    private DefaultTableModel tableModel;
    private JButton addButton;

    public TracksTab() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("+ Add New Track");
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.NORTH);

        String[] columns = {"Track ID", "Name", "Album", "Genre", "Media Type", "Composer", "Unit Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tracksTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tracksTable);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> openAddTrackDialog());

        loadTracks();
    }

    private void loadTracks() {
        tableModel.setRowCount(0);

        String sql = "SELECT t.TrackId, t.Name, a.Title AS Album, g.Name AS Genre, " +
                "mt.Name AS MediaType, t.Composer, t.UnitPrice " +
                "FROM Track t " +
                "JOIN Album a ON t.AlbumId = a.AlbumId " +
                "JOIN Genre g ON t.GenreId = g.GenreId " +
                "JOIN MediaType mt ON t.MediaTypeId = mt.MediaTypeId " +
                "ORDER BY t.TrackId DESC LIMIT 100";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("TrackId"));
                row.add(rs.getString("Name"));
                row.add(rs.getString("Album"));
                row.add(rs.getString("Genre"));
                row.add(rs.getString("MediaType"));
                row.add(rs.getString("Composer"));
                row.add(rs.getDouble("UnitPrice"));
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading tracks: " + e.getMessage());
        }
    }

    private void openAddTrackDialog() {
        AddTrackDialog dialog = new AddTrackDialog(ChinookApp.getInstance());
        dialog.setVisible(true);
        loadTracks();
    }
}
