package com.barangay.system.ui;

import com.barangay.system.model.User;
import com.barangay.system.service.UserService;

/*
 * Displays the application entry screen - Sign Up and Log In.
 * Returns the authenticated User to Main, which routes to the correct menu.
 * Returns null if the user selects Exit.
 *
 * Uses ConsoleHelper.readTitleCase() for Full Name and Address so that
 * names are always stored in Title Case regardless of how the user typed them.
 */
public class AuthMenu {

    private final UserService userService;

    public AuthMenu(UserService userService) {
        this.userService = userService;
    }

    // ----------------------------------------------------------
    // Main auth loop - runs until login succeeds or user exits
    // ----------------------------------------------------------
    public User show() {
        while (true) {
            printWelcome();
            int choice = ConsoleHelper.readInt("  Enter choice: ");

            switch (choice) {
                case 1:
                    User loggedIn = showLogin();
                    if (loggedIn != null) {
                        return loggedIn;
                    }
                    break;
                case 2:
                    showSignUp();
                    break;
                case 3:
                    System.out.println(
                            "\n  Thank you for using the Barangay Document Request System. Goodbye!");
                    return null;
                default:
                    System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // ----------------------------------------------------------
    // Welcome / main-entry menu
    // ----------------------------------------------------------
    private void printWelcome() {
        System.out.println();
        ConsoleHelper.printDivider();
        System.out.println("         BARANGAY DOCUMENT REQUEST SYSTEM");
        ConsoleHelper.printDivider();
        System.out.println("  [1] Log In");
        System.out.println("  [2] Sign Up");
        System.out.println("  [3] Exit");
        ConsoleHelper.printSeparator();
    }

    // ----------------------------------------------------------
    // Login screen
    // ----------------------------------------------------------
    private User showLogin() {
        ConsoleHelper.printHeader("LOG IN");

        String username = ConsoleHelper.readLine("  Username : ");
        String password = ConsoleHelper.readLine("  Password : ");

        User user = userService.login(username, password);

        if (user != null) {
            System.out.println("\n  Welcome back, " + user.getFullName()
                    + "!  (" + user.getRole() + ")");
            ConsoleHelper.pressEnterToContinue();
            return user;
        } else {
            ConsoleHelper.printResult("ERROR: Invalid username or password.");
            ConsoleHelper.pressEnterToContinue();
            return null;
        }
    }

    // ----------------------------------------------------------
    // Sign-up screen
    // Full Name and Address use readTitleCase() so they are always
    // stored as e.g. "Juan Dela Cruz" and "Block 4 Lot 12 Sampaguita St."
    // ----------------------------------------------------------
    private void showSignUp() {
        ConsoleHelper.printHeader("SIGN UP - NEW RESIDENT ACCOUNT");

        String username = ConsoleHelper.readLine("  Choose Username : ");
        String password = ConsoleHelper.readLine("  Choose Password : ");
        String fullName = ConsoleHelper.readTitleCase("  Full Name       : ");
        String address = ConsoleHelper.readTitleCase("  Home Address    : ");

        String result = userService.registerUser(username, password, fullName, address);
        ConsoleHelper.printResult(result);
        ConsoleHelper.pressEnterToContinue();
    }
}
