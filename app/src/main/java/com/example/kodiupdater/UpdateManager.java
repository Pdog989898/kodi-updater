package com.example.kodiupdater;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateManager {
    private static final String PREFS = "kodi_updater_prefs";
    private static final String KEY_LAST_VERSION = "last_applied_version";

    private final Context context;
    private final OkHttpClient client;
    private final Gson gson;

    public UpdateManager(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public UpdateResult run(Uri kodiFolderTreeUri) {
        if (kodiFolderTreeUri == null) {
            return UpdateResult.error("Kodi folder not configured. Select folder to continue.");
        }

        if (isPlaceholderConfig()) {
            return UpdateResult.error("Configure GitHub owner/repo in app/build.gradle before running updates.");
        }

        try {
            UpdateManifest manifest = fetchManifest();
            if (manifest == null || manifest.files == null) {
                return UpdateResult.error("No valid manifest found in repository.");
            }

            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String localVersion = prefs.getString(KEY_LAST_VERSION, "");

            if (!VersionUtils.isNewer(manifest.version, localVersion)) {
                return UpdateResult.success("No update found. Current version: " + localVersion);
            }

            DocumentFile root = DocumentFile.fromTreeUri(context, kodiFolderTreeUri);
            if (root == null || !root.isDirectory()) {
                return UpdateResult.error("Kodi folder is inaccessible.");
            }

            for (UpdateManifest.ManifestFileEntry entry : manifest.files) {
                if (entry == null || entry.url == null || entry.targetRelativePath == null) {
                    return UpdateResult.error("Manifest contains an invalid file entry.");
                }

                byte[] content = downloadBytes(entry.url);
                if (content == null) {
                    return UpdateResult.error("Failed downloading " + entry.url);
                }

                if (entry.sha256 != null && !entry.sha256.isEmpty()) {
                    String actual = ChecksumUtils.sha256Hex(content);
                    if (!entry.sha256.equalsIgnoreCase(actual)) {
                        return UpdateResult.error("Checksum mismatch for " + entry.targetRelativePath);
                    }
                }

                boolean written = writeFileToTree(root, entry.targetRelativePath, content);
                if (!written) {
                    return UpdateResult.error("Unable to write " + entry.targetRelativePath);
                }
            }

            prefs.edit().putString(KEY_LAST_VERSION, manifest.version).apply();
            return UpdateResult.success("Update successful. Installed version " + manifest.version);

        } catch (Exception e) {
            return UpdateResult.error("Update failed: " + e.getMessage());
        }
    }

    private boolean isPlaceholderConfig() {
        return "YOUR_GITHUB_USERNAME".equals(BuildConfig.GITHUB_OWNER)
                || "YOUR_REPO_NAME".equals(BuildConfig.GITHUB_REPO);
    }

    private UpdateManifest fetchManifest() throws IOException {
        String body = downloadText(getManifestUrl());
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        return gson.fromJson(body, UpdateManifest.class);
    }

    private String getManifestUrl() {
        return "https://raw.githubusercontent.com/"
                + BuildConfig.GITHUB_OWNER + "/"
                + BuildConfig.GITHUB_REPO + "/"
                + BuildConfig.GITHUB_BRANCH + "/"
                + BuildConfig.GITHUB_MANIFEST_PATH;
    }

    private String downloadText(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            return response.body().string();
        }
    }

    private byte[] downloadBytes(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            return response.body().bytes();
        }
    }

    private boolean writeFileToTree(DocumentFile root, String relativePath, byte[] content) {
        String[] parts = relativePath.split("/");
        if (parts.length == 0) {
            return false;
        }

        DocumentFile current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            DocumentFile next = current.findFile(parts[i]);
            if (next == null || !next.isDirectory()) {
                next = current.createDirectory(parts[i]);
            }
            if (next == null) {
                return false;
            }
            current = next;
        }

        String name = parts[parts.length - 1];
        if (name.isEmpty()) {
            return false;
        }

        DocumentFile target = current.findFile(name);
        if (target != null && target.exists()) {
            target.delete();
        }

        DocumentFile created = current.createFile("application/octet-stream", name);
        if (created == null || created.getUri() == null) {
            return false;
        }

        ContentResolver resolver = context.getContentResolver();
        try (OutputStream out = resolver.openOutputStream(created.getUri(), "w")) {
            if (out == null) {
                return false;
            }
            out.write(content);
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
