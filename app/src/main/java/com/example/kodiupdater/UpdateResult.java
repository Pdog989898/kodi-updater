package com.example.kodiupdater;

public class UpdateResult {
    public final boolean success;
    public final String message;

    private UpdateResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static UpdateResult success(String message) {
        return new UpdateResult(true, message);
    }

    public static UpdateResult error(String message) {
        return new UpdateResult(false, message);
    }
}
