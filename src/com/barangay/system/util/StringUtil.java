package com.barangay.system.util;

// Shared string helpers used across all service classes.
public class StringUtil {

    private StringUtil() {}

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String toTitleCase(String s) {
        if (isBlank(s)) return s;
        String[] words = s.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(words[i].charAt(0)));
            if (words[i].length() > 1)
                sb.append(words[i].substring(1).toLowerCase());
            if (i < words.length - 1) sb.append(' ');
        }
        return sb.toString();
    }

    public static String toSentenceCase(String s) {
        if (isBlank(s)) return s;
        String t = s.trim();
        return Character.toUpperCase(t.charAt(0)) + (t.length() > 1 ? t.substring(1) : "");
    }
}