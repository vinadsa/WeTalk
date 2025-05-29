package com.chat.client.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles database authentication operations for the chat application.
 * This class manages database connections and user authentication.
 */
public class DatabaseAuthenticator {
    private static final Logger LOGGER = Logger.getLogger(DatabaseAuthenticator.class.getName());
    
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://129.150.37.67:3306/we_talk";
    private static final String DB_USER = "gilang";
    private static final String DB_PASSWORD = "Motorxsr.155";
    
    private static Connection connection;
    private static DatabaseAuthenticator instance;
    
    /**
     * Private constructor for singleton pattern
     */
    private DatabaseAuthenticator() {
        // Register the JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC Driver registered.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found!", e);
        }
    }
    
    /**
     * Get singleton instance of DatabaseAuthenticator
     * 
     * @return DatabaseAuthenticator instance
     */
    public static synchronized DatabaseAuthenticator getInstance() {
        if (instance == null) {
            instance = new DatabaseAuthenticator();
        }
        return instance;
    }
    
    /**
     * Establishes a connection to the database
     * 
     * @throws SQLException if a database access error occurs
     */
    public void connect() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                LOGGER.info("Successfully connected to we_talk database!");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Connection failed! Check your database settings.", e);
            throw e;
        }
    }
    
    /**
     * Validates user credentials against the database
     * 
     * @param username the username to check
     * @param password the password to validate
     * @return true if the credentials are valid, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND pswd = ?";
        
        // Ensure we have a connection
        if (connection == null || connection.isClosed()) {
            connect();
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            LOGGER.info("Attempting to validate user: " + username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean isValid = rs.next();
                LOGGER.info("Login validation result: " + isValid);
                return isValid;
            }
        }
    }
    
    /**
     * Registers a new user in the database
     * 
     * @param username the username for the new user
     * @param password the password for the new user
     * @return true if registration was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean registerUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, pswd) VALUES (?, ?)";
        
        // Ensure we have a connection
        if (connection == null || connection.isClosed()) {
            connect();
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            int rowsAffected = pstmt.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                LOGGER.info("User registered successfully: " + username);
            } else {
                LOGGER.warning("No rows affected when registering user: " + username);
            }
            
            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering user: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Retrieves a user from the database by username
     * 
     * @param username the username to search for
     * @return User object if found, null otherwise
     * @throws SQLException if a database access error occurs
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        // Ensure we have a connection
        if (connection == null || connection.isClosed()) {
            connect();
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("pswd")
                    );
                }
                return null;
            }
        }
    }
    
    /**
     * Checks if a username already exists in the database
     * 
     * @param username the username to check
     * @return true if the username exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        // Ensure we have a connection
        if (connection == null || connection.isClosed()) {
            connect();
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
    
    /**
     * Tests the database connection
     * 
     * @return true if the connection is successful, false otherwise
     */
    public boolean testConnection() {
        try {
            connect();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Closes the database connection
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing database connection: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Creates the users table if it doesn't exist
     * 
     * @throws SQLException if a database access error occurs
     */
    public void createUsersTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "username VARCHAR(50) UNIQUE NOT NULL, " +
                     "pswd VARCHAR(255) NOT NULL)";
        
        // Ensure we have a connection
        if (connection == null || connection.isClosed()) {
            connect();
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
            LOGGER.info("Users table created or already exists.");
        }
    }
}

