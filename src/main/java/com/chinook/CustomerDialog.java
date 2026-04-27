package com.chinook;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CustomerDialog extends JDialog {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField countryField;
    private JTextField cityField;
    private JTextField addressField;
    private JButton saveButton;
    private JButton cancelButton;

    private CustomersTab parent;
    private CustomersTab.Customer existingCustomer;
    private boolean isEditMode;

    public CustomerDialog(CustomersTab parent, CustomersTab.Customer customer) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), "Customer", true);
        this.parent = parent;
        this.existingCustomer = customer;
        this.isEditMode = (customer != null);

        setTitle(isEditMode ? "Edit Customer" : "Add New Customer");
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("First Name:*"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(25);
        formPanel.add(firstNameField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Last Name:*"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(25);
        formPanel.add(lastNameField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(25);
        formPanel.add(emailField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(25);
        formPanel.add(phoneField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Country:*"), gbc);
        gbc.gridx = 1;
        countryField = new JTextField(25);
        formPanel.add(countryField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("City:"), gbc);
        gbc.gridx = 1;
        cityField = new JTextField(25);
        formPanel.add(cityField, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressField = new JTextField(25);
        formPanel.add(addressField, gbc);
        row++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (isEditMode) {
            firstNameField.setText(existingCustomer.firstName);
            lastNameField.setText(existingCustomer.lastName);
            emailField.setText(existingCustomer.email);
            phoneField.setText(existingCustomer.phone);
            countryField.setText(existingCustomer.country);
            cityField.setText(existingCustomer.city);
            addressField.setText(existingCustomer.address);
        }

        saveButton.addActionListener(e -> saveCustomer());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveCustomer() {
        // Validate required fields
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String country = countryField.getText().trim();

        if (firstName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name is required!");
            return;
        }
        if (lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Last Name is required!");
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required!");
            return;
        }
        if (country.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Country is required!");
            return;
        }

        String phone = phoneField.getText().trim();
        String city = cityField.getText().trim();
        String address = addressField.getText().trim();

        String sql;
        if (isEditMode) {
            sql = "UPDATE Customer SET FirstName=?, LastName=?, Email=?, Phone=?, " +
                    "Country=?, City=?, Address=? WHERE CustomerId=?";
        } else {
            sql = "INSERT INTO Customer (CustomerId, FirstName, LastName, Email, Phone, Country, City, Address) " +
                    "VALUES ((SELECT MAX(CustomerId) + 1 FROM Customer), ?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (isEditMode) {
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, email);
                pstmt.setString(4, phone.isEmpty() ? null : phone);
                pstmt.setString(5, country);
                pstmt.setString(6, city.isEmpty() ? null : city);
                pstmt.setString(7, address.isEmpty() ? null : address);
                pstmt.setInt(8, existingCustomer.id);
            } else {
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                pstmt.setString(3, email);
                pstmt.setString(4, phone.isEmpty() ? null : phone);
                pstmt.setString(5, country);
                pstmt.setString(6, city.isEmpty() ? null : city);
                pstmt.setString(7, address.isEmpty() ? null : address);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, isEditMode ? "Customer updated successfully!" :
                        "Customer added successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Operation failed!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}