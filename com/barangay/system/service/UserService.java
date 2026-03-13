package com.barangay.system.service;

import com.barangay.system.model.User;
import com.barangay.system.repository.UserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * Business-logic layer for user-related operations.
 * Validates all inputs and enforces rules before calling the repository.
 * Returns plain-text result messages for the UI to display.
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ----------------------------------------------------------
    // SIGN UP - validate, check uniqueness, then persist.
    // Expects fullName and address to already be Title-Cased
    // by the UI layer (ConsoleHelper.toTitleCase).
    // ----------------------------------------------------------
    public String registerUser(String username, String password,
            String fullName, String address) {

        // Input validation
        if (username == null || username.trim().isEmpty()) {
            return "ERROR: Username cannot be empty.";
        }
        if (password == null || password.trim().isEmpty()) {
            return "ERROR: Password cannot be empty.";
        }
        if (password.length() < 6) {
            return "ERROR: Password must be at least 6 characters.";
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            return "ERROR: Full name cannot be empty.";
        }
        if (address == null || address.trim().isEmpty()) {
            return "ERROR: Address cannot be empty.";
        }

        try {
            // Username uniqueness check
            if (userRepository.usernameExists(username.trim())) {
                return "ERROR: Username '" + username + "' is already taken. "
                        + "Please choose a different username.";
            }

            // Build and persist the new user
            User newUser = new User(
                    username.trim(), password,
                    fullName.trim(), address.trim(), User.ROLE_USER
            );
            boolean saved = userRepository.save(newUser);

            return saved
                    ? "SUCCESS: Account created! You may now log in as '" + username + "'."
                    : "ERROR: Registration failed. Please try again.";

        } catch (SQLException e) {
            return "ERROR: Database error during registration - " + e.getMessage();
        }
    }

    // ----------------------------------------------------------
    // LOGIN - match username and password, return User or null
    // ----------------------------------------------------------
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        try {
            User user = userRepository.findByUsername(username.trim());
            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Database error during login - " + e.getMessage());
        }
        return null;
    }

    // ----------------------------------------------------------
    // UPDATE PROFILE - change full name, address, and password.
    // Expects fullName and address to already be Title-Cased.
    // ----------------------------------------------------------
    public String updateProfile(int userId, String newFullName,
            String newAddress, String newPassword) {

        if (newFullName == null || newFullName.trim().isEmpty()) {
            return "ERROR: Full name cannot be empty.";
        }
        if (newAddress == null || newAddress.trim().isEmpty()) {
            return "ERROR: Address cannot be empty.";
        }
        if (newPassword == null || newPassword.length() < 6) {
            return "ERROR: Password must be at least 6 characters.";
        }

        try {
            User user = userRepository.findById(userId);
            if (user == null) {
                return "ERROR: User not found.";
            }

            user.setFullName(newFullName.trim());
            user.setAddress(newAddress.trim());
            user.setPassword(newPassword);

            boolean updated = userRepository.update(user);
            return updated
                    ? "SUCCESS: Profile updated successfully."
                    : "ERROR: Update failed. Please try again.";

        } catch (SQLException e) {
            return "ERROR: Database error - " + e.getMessage();
        }
    }

    // ----------------------------------------------------------
    // GET ALL USERS - admin view
    // ----------------------------------------------------------
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (SQLException e) {
            System.out.println("ERROR: Could not fetch users - " + e.getMessage());
            return new ArrayList<User>();
        }
    }

    // ----------------------------------------------------------
    // GET USER BY ID
    // ----------------------------------------------------------
    public User getUserById(int id) {
        try {
            return userRepository.findById(id);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return null;
        }
    }
}
