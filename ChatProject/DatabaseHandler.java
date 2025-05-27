import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {
    // Update these connection details for your MySQL database
    private static final String DB_URL = "jdbc:mysql://localhost:3306/we_talk";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "wirapati123"; // Replace with your actual MySQL root password
    
    private static Connection connection;
    
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver registered.");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    public static void connect() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Successfully connected to we_talk database!");
            }
        } catch (SQLException e) {
            System.err.println("Connection failed! Check your database settings.");
            throw e;
        }
    }
    
    public static boolean testConnection() {
        try {
            connect();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean validateLogin(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND pswd = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            System.out.println("Attempting to validate user: " + username); // Debug log
            
            ResultSet rs = pstmt.executeQuery();
            boolean isValid = rs.next();
            
            System.out.println("Login validation result: " + isValid); // Debug log
            
            return isValid;
        }
    }
    
    public static boolean registerUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, pswd) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            System.out.println("User registered successfully: " + username); // Debug log
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

