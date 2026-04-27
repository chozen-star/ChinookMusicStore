package com.chinook;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AddTrackDialog extends JDialog {

    private JComboBox<String> albumCombo;
    private JComboBox<String> genreCombo;
    private JComboBox<String> mediaTypeCombo;
    private JTextField nameField;
    private JTextField composerField;
    private JTextField priceField;
    private JButton saveButton;
    private JButton cancelButton;

    // Maps to store IDs for selected items
    private Map<String, Integer> albumMap = new HashMap<>();
    private Map<String, Integer> genreMap = new HashMap<>();
    private Map<String, Integer> mediaTypeMap = new HashMap<>();

    public AddTrackDialog(JFrame parent) {
        super(parent, "Add New Track", true);
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Track Name:*"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Album:*"), gbc);
        gbc.gridx = 1;
        albumCombo = new JComboBox<>();
        albumCombo.setPreferredSize(new Dimension(200, 25));
        formPanel.add(albumCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Genre:*"), gbc);
        gbc.gridx = 1;
        genreCombo = new JComboBox<>();
        genreCombo.setPreferredSize(new Dimension(200, 25));
        formPanel.add(genreCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Media Type:*"), gbc);
        gbc.gridx = 1;
        mediaTypeCombo = new JComboBox<>();
        mediaTypeCombo.setPreferredSize(new Dimension(200, 25));
        formPanel.add(mediaTypeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Composer:"), gbc);
        gbc.gridx = 1;
        composerField = new JTextField(20);
        formPanel.add(composerField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Unit Price:*"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(10);
        formPanel.add(priceField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadAlbums();
        loadGenres();
        loadMediaTypes();

        saveButton.addActionListener(e -> saveTrack());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadAlbums() {
        String sql = "SELECT AlbumId, Title FROM Album ORDER BY Title";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("Title");
                int id = rs.getInt("AlbumId");
                albumMap.put(title, id);
                albumCombo.addItem(title);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading albums: " + e.getMessage());
        }
    }

    private void loadGenres() {
        String sql = "SELECT GenreId, Name FROM Genre ORDER BY Name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("Name");
                int id = rs.getInt("GenreId");
                genreMap.put(name, id);
                genreCombo.addItem(name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading genres: " + e.getMessage());
        }
    }

    private void loadMediaTypes() {
        String sql = "SELECT MediaTypeId, Name FROM MediaType ORDER BY Name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("Name");
                int id = rs.getInt("MediaTypeId");
                mediaTypeMap.put(name, id);
                mediaTypeCombo.addItem(name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading media types: " + e.getMessage());
        }
    }
    private void saveTrack() {
        String trackName = nameField.getText().trim();
        if (trackName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Track Name is required!");
            return;
        }

        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unit Price is required!");
            return;
        }

        double unitPrice;
        try {
            unitPrice = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Unit Price must be a valid number!");
            return;
        }

        String selectedAlbum = (String) albumCombo.getSelectedItem();
        String selectedGenre = (String) genreCombo.getSelectedItem();
        String selectedMediaType = (String) mediaTypeCombo.getSelectedItem();

        if (selectedAlbum == null || selectedGenre == null || selectedMediaType == null) {
            JOptionPane.showMessageDialog(this, "Please select Album, Genre, and Media Type!");
            return;
        }

        int albumId = albumMap.get(selectedAlbum);
        int genreId = genreMap.get(selectedGenre);
        int mediaTypeId = mediaTypeMap.get(selectedMediaType);
        String composer = composerField.getText().trim();

        String getIdSql = "SELECT MAX(TrackId) + 1 as nextId FROM Track";
        int nextTrackId = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getIdSql)) {

            if (rs.next()) {
                nextTrackId = rs.getInt("nextId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error getting next Track ID: " + e.getMessage());
            return;
        }

        String sql = "INSERT INTO Track (TrackId, Name, AlbumId, MediaTypeId, GenreId, Composer, UnitPrice, Milliseconds, Bytes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nextTrackId);
            pstmt.setString(2, trackName);
            pstmt.setInt(3, albumId);
            pstmt.setInt(4, mediaTypeId);
            pstmt.setInt(5, genreId);
            pstmt.setString(6, composer.isEmpty() ? null : composer);
            pstmt.setDouble(7, unitPrice);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Track added successfully! Track ID: " + nextTrackId);
                dispose(); // Close dialog
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add track! No rows affected.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }


}