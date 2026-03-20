package com.example.kodiupdater;

public final class VersionUtils {
    private VersionUtils() {
    }

    public static boolean isNewer(String remote, String local) {
        if (remote == null || remote.trim().isEmpty()) {
            return false;
        }
        if (local == null || local.trim().isEmpty()) {
            return true;
        }

        String[] remoteParts = remote.split("\\.");
        String[] localParts = local.split("\\.");
        int max = Math.max(remoteParts.length, localParts.length);

        for (int i = 0; i < max; i++) {
            int r = i < remoteParts.length ? parsePart(remoteParts[i]) : 0;
            int l = i < localParts.length ? parsePart(localParts[i]) : 0;
            if (r > l) {
                return true;
            }
            if (r < l) {
                return false;
            }
        }
        return false;
    }

    private static int parsePart(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
