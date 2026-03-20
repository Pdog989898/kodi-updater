package com.example.kodiupdater;

import java.util.List;

public class UpdateManifest {
    public String version;
    public List<ManifestFileEntry> files;

    public static class ManifestFileEntry {
        public String path;
        public String url;
        public String sha256;
        public String targetRelativePath;
    }
}
