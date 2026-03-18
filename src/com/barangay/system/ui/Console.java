package com.barangay.system.ui;

import com.barangay.system.util.StringUtil;

import java.sql.Timestamp;
import java.util.Scanner;

// All shared display and input utilities. Static only — never instantiate.
public class Console {

    // System title constants — no database name shown to users.
    public static final String TITLE     = "BARANGAY DOCUMENT REQUEST AND MANAGEMENT SYSTEM";
    public static final String SUBTITLE  = "Document Services  |  Barangay Hall";

    public static final int    W      = 80;
    public static final String INDENT = "     ";   // 5-space indent for raw prompts only

    // Table column format strings — 2-space leading indent applied in row().
    public static final String FMT_REQUEST_RESIDENT = "%-8s  %-26s  %-10s  %-10s  %-14s";
    public static final String FMT_REQUEST_ADMIN    = "%-8s  %-20s  %-22s  %-10s";
    public static final String FMT_RESIDENT         = "%-5s  %-16s  %-22s  %-15s";
    public static final String FMT_DOCTYPE          = "%-5s  %-32s  %-10s  %-8s";
    public static final String FMT_REGISTRATION     = "%-5s  %-14s  %-20s  %-13s  %-14s";
    public static final String FMT_HISTORY          = "%-5s  %-16s  %-20s  %-8s  %-14s";

    private static final Scanner SCAN = new Scanner(System.in);

    // ── Layout ─────────────────────────────────────────────────────────────

    public static void divider()   { System.out.println(rep('=', W)); }
    public static void separator() { System.out.println(rep('-', W)); }

    public static void header(String title) {
        System.out.println();
        divider();
        centered(title);
        divider();
    }

    public static void subHeader(String title) {
        separator();
        centered(title);
        separator();
    }

    // Prints text horizontally centered within W columns.
    public static void centered(String text) {
        if (text == null || text.isEmpty()) return;
        int pad = Math.max(0, (W - text.length()) / 2);
        System.out.println(rep(' ', pad) + text);
    }

    // Prints a table row with a 2-space leading indent.
    public static void row(String fmt, Object... vals) {
        System.out.printf("  " + fmt + "%n", vals);
    }

    /*
     * Prints a block of menu items all starting at the same horizontal
     * position, calculated so the widest item is centered on the terminal.
     * Every menu in the system calls this method — ensures 100% consistent layout.
     *
     * Example output (W=80, widest item = "[ 5 ]  Cancel Request" = 22 chars):
     *     pad = (80 - 22) / 2 = 29 spaces
     *                             [ 1 ]  View Requests
     *                             [ 2 ]  Find Requests
     *                             ...
     *                             [ 0 ]  Back
     */
    public static void menu(String[] items) {
        int maxLen = 0;
        for (String item : items) {
            if (item.length() > maxLen) maxLen = item.length();
        }
        int leftPad = Math.max(2, (W - maxLen) / 2);
        String pad = rep(' ', leftPad);
        System.out.println();
        for (String item : items) {
            System.out.println(pad + item);
        }
        System.out.println();
        separator();
    }

    public static void result(String msg) {
        System.out.println();
        String prefix = msg.startsWith("SUCCESS") ? "[OK]  "
                      : msg.startsWith("ERROR")   ? "[!!]  "
                      : "[i]   ";
        centered(prefix + msg);
        System.out.println();
    }

    public static void noResults(String label) {
        centered("[i]   No " + label + " found.");
    }

    public static void count(int n, String label) {
        centered("[i]   Showing " + n + " " + label + (n == 1 ? "" : "s"));
    }

    public static void pressEnter() {
        System.out.println();
        centered("Press [Enter] to continue...");
        SCAN.nextLine();
    }

    // ── Input ──────────────────────────────────────────────────────────────

    public static String readLine(String prompt) {
        System.out.print(rep(' ', (W - prompt.length()) / 2) + prompt);
        return SCAN.nextLine().trim();
    }

    // Password input: uses System.console() for masking; falls back to Scanner in IDE.
    public static String readPassword(String prompt) {
        String pad = rep(' ', Math.max(0, (W - prompt.length()) / 2));
        System.out.print(pad + prompt);
        java.io.Console c = System.console();
        if (c != null) {
            char[] pw = c.readPassword();
            return pw != null ? new String(pw) : "";
        }
        return SCAN.nextLine().trim();
    }

    public static String readTitleCase(String prompt) {
        return StringUtil.toTitleCase(readLine(prompt));
    }

    public static String readSentence(String prompt) {
        return StringUtil.toSentenceCase(readLine(prompt));
    }

    // Loops until a valid integer is entered — never crashes on bad input.
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(rep(' ', Math.max(0, (W - prompt.length()) / 2)) + prompt);
            try { return Integer.parseInt(SCAN.nextLine().trim()); }
            catch (NumberFormatException e) { centered("[!!]  Please enter a valid number."); }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(rep(' ', Math.max(0, (W - prompt.length()) / 2)) + prompt);
            try { return Double.parseDouble(SCAN.nextLine().trim()); }
            catch (NumberFormatException e) { centered("[!!]  Enter a valid decimal (e.g. 50.00)."); }
        }
    }

    /*
     * Y/N confirmation prompt. Loops until y or n is entered.
     * Returns true for y, false for n (case-insensitive).
     * Replaces all previous (yes/no) prompts in the system.
     */
    public static boolean confirm(String prompt) {
        while (true) {
            String fullPrompt = prompt + " (y/n): ";
            System.out.print(rep(' ', Math.max(0, (W - fullPrompt.length()) / 2)) + fullPrompt);
            String input = SCAN.nextLine().trim().toLowerCase();
            if (input.equals("y")) return true;
            if (input.equals("n")) return false;
            centered("[!!]  Please enter y or n.");
        }
    }

    // ── Formatting ─────────────────────────────────────────────────────────

    // Truncates string to maxLen chars, appending "..." if cut.
    public static String cut(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }

    public static String fmtDate(Timestamp ts) {
        return ts != null ? ts.toString().substring(0, 16) : "-";
    }

    public static String fmtFee(double fee) {
        return fee == 0 ? "FREE" : String.format("PHP %.2f", fee);
    }

    private static String rep(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }
}