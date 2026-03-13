package com.barangay.system.main;

import com.barangay.system.database.DatabaseConnection;
import com.barangay.system.model.User;
import com.barangay.system.repository.DocumentRequestRepository;
import com.barangay.system.repository.UserRepository;
import com.barangay.system.service.DocumentRequestService;
import com.barangay.system.service.UserService;
import com.barangay.system.ui.AdminMenu;
import com.barangay.system.ui.AuthMenu;
import com.barangay.system.ui.UserMenu;

import java.sql.SQLException;

/*
 * Entry point for the Barangay Document Request System.
 *
 * Flow:
 *   1. Verify the database connection on startup.
 *   2. Wire repositories -> services -> menus.
 *   3. Show the auth screen and route the logged-in user by role.
 *   4. Return to the auth screen after each logout.
 */
public class Main {

    public static void main(String[] args) {

        // ----------------------------------------------------------
        // 1. Verify DB connectivity before doing anything else
        // ----------------------------------------------------------
        System.out.println("Connecting to database...");

        try {
            DatabaseConnection.getInstance();
            System.out.println("Database connection established successfully.\n");
        } catch (SQLException e) {
            System.out.println("FATAL: Could not connect to the database.");
            System.out.println("Reason : " + e.getMessage());
            System.out.println("Ensure XAMPP MySQL is running and barangay_db exists.");
            return;
        }

        // ----------------------------------------------------------
        // 2. Build the dependency graph: Repositories -> Services -> Menus
        // ----------------------------------------------------------
        UserRepository            userRepo    = new UserRepository();
        DocumentRequestRepository requestRepo = new DocumentRequestRepository();

        UserService            userService    = new UserService(userRepo);
        DocumentRequestService requestService = new DocumentRequestService(requestRepo);

        AuthMenu authMenu = new AuthMenu(userService);

        // ----------------------------------------------------------
        // 3. Application loop - returns to auth screen after each logout
        // ----------------------------------------------------------
        boolean continueRunning = true;

        while (continueRunning) {
            User loggedInUser = authMenu.show();

            if (loggedInUser == null) {
                // User chose Exit from the auth menu
                continueRunning = false;
            } else {
                // Route to the correct menu based on role
                if (User.ROLE_ADMIN.equals(loggedInUser.getRole())) {
                    new AdminMenu(userService, requestService, loggedInUser).show();
                } else {
                    new UserMenu(userService, requestService, loggedInUser).show();
                }
                // Loop back to auth screen after logout
            }
        }

        System.out.println("\nSystem shutdown. Goodbye!");
    }
}