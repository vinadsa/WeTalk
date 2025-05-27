public class DatabaseTest {
    public static void main(String[] args) {
        try {
            // Test database connection
            System.out.println("Testing database connection...");
            if (DatabaseHandler.testConnection()) {
                System.out.println("Database connection successful!");
                
                // Try to register a test user
                String testUsername = "testuser";
                String testPassword = "password123";
                
                System.out.println("\nAttempting to register test user...");
                if (DatabaseHandler.registerUser(testUsername, testPassword)) {
                    System.out.println("Test user registered successfully!");
                    
                    // Try to login with the test user
                    System.out.println("\nAttempting to login with test user...");
                    if (DatabaseHandler.validateLogin(testUsername, testPassword)) {
                        System.out.println("Login successful!");
                    } else {
                        System.out.println("Login failed!");
                    }
                } else {
                    System.out.println("Failed to register test user!");
                }
            } else {
                System.out.println("Database connection failed!");
            }
        } catch (Exception e) {
            System.out.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseHandler.close();
        }
    }
}
