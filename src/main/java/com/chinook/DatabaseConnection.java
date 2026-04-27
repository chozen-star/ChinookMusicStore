package com.chinook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Using environment variables for security
    private static final String PROTO = System.getenv("CHINOOK.DB.PROTO");
    private static final String HOST = System.getenv("CHINOOK.DB.HOST");
    private static final String PORT = System.getenv("CHINOOK.DB.PORT");
    private static final String DB_NAME = System.getenv("CHINOOK.DB.NAME");
    private static final String USERNAME = System.getenv("CHINOOK.DB.USERNAME");
    private static final String PASSWORD = System.getenv("CHINOOK.DB.PASSWORD");

    private static final String URL = PROTO + "://" + HOST + ":" + PORT + "/" + DB_NAME;

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");

            if (PROTO == null || HOST == null || PORT == null || DB_NAME == null || USERNAME == null || PASSWORD == null){
                throw  new SQLException("Missing database environment variables! "+"Please set CHINOOK.DB.PROTO, CHINOOK.DB.HOST, CHINOOK.DB.PORT, CHINOOK.DB.NAME, CHINOOK.DB.USERNAME, CHINOOK.DB.PASSWORD");
            }
            return DriverManager.getConnection(URL,USERNAME,PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("MariaDB JDBC Driver not found!");
        }
    }

    public static void main(String[] args) {
        try {
            Connection conn = getConnection();
            System.out.println("✅ Connected to database successfully!");
            System.out.println("URL: "+ URL);
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
    }
}