package com.chinook;

import javax.swing.*;
import java.awt.*;

public class ChinookApp extends JFrame {

    private JTabbedPane tabbedPane;
    private static ChinookApp instance;

    public ChinookApp() {
        instance = this;
        setTitle("Chinook Music Store Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Employees", new EmployeesTab());
        tabbedPane.addTab("Tracks", new TracksTab());
        tabbedPane.addTab("Revenue Report", new ReportTab());
        tabbedPane.addTab("Customers", new CustomersTab());
        tabbedPane.addTab("Recommendations", new RecommendationsTab());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChinookApp app = new ChinookApp();
            app.setVisible(true);
        });
    }

    public static ChinookApp getInstance(){
        return instance;
    }

}
// Version 1.1
