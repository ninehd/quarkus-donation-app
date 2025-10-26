package com.redhat.quarkus.donation.utils;

public class StringUtils {

    /**
     * Mask an email address for privacy
     * Example: john.doe@example.com -> jo******@example.com
     *
     * @param email The email to mask
     * @return The masked email, or original if invalid
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            // Invalid email, return as is
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        // Keep first 2 characters (or 1 if shorter), replace rest with asterisks
        int visibleChars = Math.min(2, localPart.length());
        String maskedLocalPart = localPart.substring(0, visibleChars) + "******";

        return maskedLocalPart + domain;
    }
}