package com.barangay.system.ui;

import java.util.Scanner;

/*
 * Utility class with shared console I/O helpers for all menu classes.
 *
 * Changes in this version:
 *   - Separators and dividers are 80 characters wide for full-screen display.
 *   - toTitleCase()    : capitalizes the first letter of every word.
 *                        Used for Full Name and Address inputs.
 *   - toSentenceCase() : capitalizes only the very first letter of the string.
 *                        Used for Purpose / Reason inputs.
 *   - readTitleCase()  : reads a line and auto-applies toTitleCase().
 *   - readSentenceCase(): reads a line and auto-applies toSentenceCase().
 */
public class ConsoleHelper {

    // Single shared scanner - never close System.in
    private static final Scanner SCANNER = new Scanner(System.in);

    // Width of separator lines (matches a standard full-screen terminal)
    private static final int WIDTH = 150;

    // ----------------------------------------------------------
    // Basic input - reads raw trimmed input
    // ----------------------------------------------------------
    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    // ----------------------------------------------------------
    // Title-case input - first letter of every word is capitalized.
    // Example: "juan dela cruz" -> "Juan Dela Cruz"
    // Use for: Full Name, Address
    // ----------------------------------------------------------
    public static String readTitleCase(String prompt) {
        String raw = readLine(prompt);
        return toTitleCase(raw);
    }

    // ----------------------------------------------------------
    // Sentence-case input - only the very first letter is capitalized,
    // the rest of the string is left as typed.
    // Example: "for employment purposes" -> "For employment purposes"
    // Use for: Purpose / Reason fields
    // ----------------------------------------------------------
    public static String readSentenceCase(String prompt) {
        String raw = readLine(prompt);
        return toSentenceCase(raw);
    }

    // ----------------------------------------------------------
    // Reads an integer, looping until a valid number is entered
    // ----------------------------------------------------------
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }

    // ----------------------------------------------------------
    // Converts a string to Title Case.
    // Each word's first letter is uppercased; the rest are lowercased.
    // Handles null and empty strings safely.
    // ----------------------------------------------------------
    public static String toTitleCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String[] words = input.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() == 0) {
                continue;
            }

            // Uppercase the first character, lowercase the rest
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1).toLowerCase());
            }

            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    // ----------------------------------------------------------
    // Converts a string to Sentence Case.
    // Only the very first character is uppercased.
    // The rest of the string is left exactly as typed.
    // Handles null and empty strings safely.
    // ----------------------------------------------------------
    public static String toSentenceCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        String trimmed = input.trim();
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase();
        }
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    // ----------------------------------------------------------
    // Print an 80-character divider line ( = signs )
    // ----------------------------------------------------------
    public static void printDivider() {
        System.out.println(repeat('=', WIDTH));
    }

    // ----------------------------------------------------------
    // Print an 80-character separator line ( - signs )
    // ----------------------------------------------------------
    public static void printSeparator() {
        System.out.println(repeat('-', WIDTH));
    }

    // ----------------------------------------------------------
    // Print a title block between two dividers, padded with spaces
    // ----------------------------------------------------------
    public static void printHeader(String title) {
        printDivider();
        System.out.println("  " + title);
        printDivider();
    }

    // ----------------------------------------------------------
    // Print a service result message with a >> prefix
    // ----------------------------------------------------------
    public static void printResult(String message) {
        System.out.println();
        System.out.println("  >> " + message);
        System.out.println();
    }

    // ----------------------------------------------------------
    // Pause execution until the user presses Enter
    // ----------------------------------------------------------
    public static void pressEnterToContinue() {
        System.out.print("\n  Press [Enter] to continue...");
        SCANNER.nextLine();
    }

    // ----------------------------------------------------------
    // Internal helper - builds a string of n repeated characters
    // ----------------------------------------------------------
    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
