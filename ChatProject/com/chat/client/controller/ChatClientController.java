package com.chat.client.controller;

import com.chat.client.model.ChatClientModel;
import com.chat.client.model.DatabaseAuthenticator;
import com.chat.client.model.User;
import com.chat.client.view.ChatClientView;
// import com.chat.client.model.ChatMessage; // Tidak digunakan secara langsung di sini

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ChatClientController {
    private ChatClientModel model;
    private ChatClientView view;
    private DatabaseAuthenticator dbAuth;
    private User currentUser;

    public ChatClientController(ChatClientModel model) {
        this.model = model;
        this.dbAuth = DatabaseAuthenticator.getInstance();
        
        // Add shutdown hook to ensure resources are cleaned up
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Application shutting down, cleaning up resources...");
            if (dbAuth != null) {
                dbAuth.close();
                System.out.println("Database connections closed.");
            }
        }));
    }
    public void setView(ChatClientView view) {
        this.view = view;
        this.model.addPropertyChangeListener(this.view); 
    }

    public void startApplication() {
        try {
            // Initialize database and create users table if needed
            initializeDatabase();
            
            // Show login/registration dialog
            boolean authenticated = showAuthDialog();
            
            if (authenticated && currentUser != null) {
                model.setUsername(currentUser.getUsername());
                if (view != null) { // Pastikan view sudah di-set
                    view.setTitle("Chat Klien - " + model.getUsername());
                    view.updateMessages(model.getMessages());
                    view.setInteractionEnabled(false);
                }
                model.connect();
            } else {
                // If authentication failed, close the application
                JOptionPane.showMessageDialog(view,
                    "Authentication failed or cancelled. Application will exit.",
                    "Authentication Error",
                    JOptionPane.ERROR_MESSAGE);
                cleanupAndExit(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                "Error starting application: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            cleanupAndExit(1);
        }
    }
    
    /**
     * Initialize database connection and setup
     */
    private void initializeDatabase() throws SQLException {
        try {
            // Test database connection
            if (!dbAuth.testConnection()) {
                throw new SQLException("Failed to connect to database");
            }
            
            // Create users table if it doesn't exist
            dbAuth.createUsersTableIfNotExists();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view,
                "Database error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }
    
    /**
     * Show authentication dialog with login and registration options
     * 
     * @return true if authentication succeeded, false otherwise
     */
    private boolean showAuthDialog() {
        // Create option buttons
        String[] options = {"Login", "Register", "Cancel"};
        
        int choice = JOptionPane.showOptionDialog(view,
            "Welcome to WeTalk Chat. Please select an option:",
            "WeTalk Authentication",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
            
        switch (choice) {
            case 0: // Login
                return showLoginDialog();
            case 1: // Register
                return showRegistrationDialog();
            default: // Cancel or closed
                return false;
        }
    }
    
    /**
     * Show login dialog and authenticate user
     * 
     * @return true if login succeeded, false otherwise
     */
    private boolean showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        
        int result = JOptionPane.showConfirmDialog(view, panel, "Login", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                    "Username and password cannot be empty",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
                return showLoginDialog(); // Show dialog again
            }
            
            try {
                if (dbAuth.validateUser(username, password)) {
                    // Login successful
                    currentUser = dbAuth.getUserByUsername(username);
                    return true;
                } else {
                    // Login failed
                    JOptionPane.showMessageDialog(view,
                        "Invalid username or password",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
                    return showLoginDialog(); // Show dialog again
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(view,
                    "Database error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return false; // Cancelled
    }
    
    /**
     * Show registration dialog and register new user
     * 
     * @return true if registration succeeded, false otherwise
     */
    private boolean showRegistrationDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);
        
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPasswordField);
        
        int result = JOptionPane.showConfirmDialog(view, panel, "Register", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            // Validate input
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(view,
                    "Username and password cannot be empty",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
                return showRegistrationDialog(); // Show dialog again
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(view,
                    "Passwords do not match",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
                return showRegistrationDialog(); // Show dialog again
            }
            
            try {
                // Check if username already exists
                if (dbAuth.usernameExists(username)) {
                    JOptionPane.showMessageDialog(view,
                        "Username already exists",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                    return showRegistrationDialog(); // Show dialog again
                }
                
                // Register new user
                if (dbAuth.registerUser(username, password)) {
                    // Registration successful
                    JOptionPane.showMessageDialog(view,
                        "Registration successful. Please login.",
                        "Registration Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    return showLoginDialog(); // Show login dialog
                } else {
                    // Registration failed
                    JOptionPane.showMessageDialog(view,
                        "Registration failed. Please try again.",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                    return showRegistrationDialog(); // Show dialog again
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(view,
                    "Database error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        return false; // Cancelled
    }

    public void sendMessageRequested(String messageContent) {
        if (model.isConnected()) {
            model.sendMessage(messageContent);
        } else {
            if (view != null) {
                JOptionPane.showMessageDialog(view, "Tidak terhubung ke server. Pesan tidak terkirim.", "Error Koneksi", JOptionPane.ERROR_MESSAGE);
            }
            // Untuk konsistensi, pesan error bisa juga dikirim melalui model agar view menampilkannya
            // model.addMessageInternal(new ChatMessage("SYSTEM", "Pesan tidak terkirim, tidak ada koneksi.", ChatMessage.MessageType.ERROR_MESSAGE));
        }
    }

    public void disconnectRequested() {
        model.disconnect();
        // Note: We don't close the database connection here as the user might want to reconnect
        // Database connections will be closed when the application exits via the shutdown hook
    }
    
    public String getClientUsername() {
        return model.getUsername();
    }
    
    /**
     * Get the current authenticated user
     * 
     * @return the current User object
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if user is authenticated
     * 
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    /**
     * Clean up resources and exit the application
     * 
     * @param exitCode the exit code to use
     */
    private void cleanupAndExit(int exitCode) {
        // Close database connections
        if (dbAuth != null) {
            dbAuth.close();
            System.out.println("Database connections closed before exit.");
        }
        
        // Disconnect from chat server if connected
        if (model != null && model.isConnected()) {
            model.disconnect();
            System.out.println("Disconnected from chat server before exit.");
        }
        
        // Exit the application
        System.exit(exitCode);
    }
}
