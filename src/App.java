import model.User;
import service.AppointmentService;
import ui.LoginMenu;
import ui.MainMenu;
import dao.DBConnection;

import java.sql.SQLException;

public class App {
    // volatile for thread visibility
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nShutting down Hospital Management System...");
            cleanup();
            System.out.println("Goodbye!");
        }));

        // Welcome message
        printWelcomeBanner();

        try {
            // Test database connection
            testDatabaseConnection();

            // Main application loop
            runApplication();

        } catch (Exception e) {
            System.err.println("\n❌ Fatal Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    /**
     * Main application loop
     */
    private static void runApplication() {
        LoginMenu loginMenu = new LoginMenu();

        while (isRunning) {
            try {
                // Show login menu and get authenticated user
                User user = loginMenu.showLoginMenu();

                if (user != null) {
                    // Show main menu based on user role
                    MainMenu mainMenu = new MainMenu(user);
                    mainMenu.show();
                }

            } catch (Exception e) {
                System.err.println("\n❌ An error occurred: " + e.getMessage());
                System.out.println("Please try again.\n");

                // Optional: Log the exception for debugging
                if (isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Print welcome banner
     */
    private static void printWelcomeBanner() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                            ║");
        System.out.println("║          HOSPITAL MANAGEMENT SYSTEM                        ║");
        System.out.println("║                                                            ║");
        System.out.println("║          Comprehensive Healthcare Solution                 ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\n          Powered by Java OOP Principles");
        System.out.println("          Version 1.0.0\n");
    }

    /**
     * Test database connection
     */
    private static void testDatabaseConnection() {
        System.out.print("Connecting to database... ");
        try {
            DBConnection.getConnection();
            System.out.println("✓ Connected successfully!\n");
        } catch (SQLException e) {
            System.err.println("\n\n❌ Database Connection Failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nPlease check:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Database 'hospital_management' exists");
            System.err.println("3. Username and password in DBConnection.java are correct");
            System.err.println("4. MySQL JDBC driver is in classpath\n");
            System.exit(1);
        }
    }

    /**
     * Cleanup resources before exit
     */
    private static void cleanup() {
        try {
            // Shutdown appointment service thread pool
            AppointmentService.shutdown();

            // Close database connection
            DBConnection.closeConnection();

            System.out.println("✓ Resources cleaned up successfully");

        } catch (Exception e) {
            System.err.println("Warning: Error during cleanup - " + e.getMessage());
        }
    }

    /**
     * Check if debug mode is enabled
     * Can be enabled via system property: -Ddebug=true
     */
    private static boolean isDebugMode() {
        return Boolean.getBoolean("debug");
    }

    /**
     * Stop the application
     */
    public static void stop() {
        isRunning = false;
    }
}
