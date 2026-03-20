# Kodi Updater (Android TV, Java)

This Android TV app checks a GitHub-hosted manifest for updates, downloads updated files, and writes them into a user-selected Kodi folder.

## Project structure

- `app/src/main/java/com/example/kodiupdater/MainActivity.java` - launch screen and update orchestration
- `app/src/main/java/com/example/kodiupdater/UpdateManager.java` - manifest fetch, version check, download, checksum, and file replacement logic
- `app/src/main/java/com/example/kodiupdater/VersionUtils.java` - semantic-ish version comparison
- `app/src/main/java/com/example/kodiupdater/ChecksumUtils.java` - SHA-256 helper
- `app/src/main/assets/sample-kodi-update-manifest.json` - manifest template
- `app/src/test/java/com/example/kodiupdater/*` - unit tests

## What it does

1. On launch it shows **"Checking for updates."**
2. If no Kodi folder is configured, it prompts for folder selection.
3. It fetches `kodi-update-manifest.json` from your GitHub repository.
4. If `manifest.version` is newer than the last applied version, it downloads listed files.
5. It verifies SHA-256 checksums (if supplied), replaces files in the chosen Kodi directory, and displays **"Update successful..."**.

## Important limitation

Android does not allow arbitrary file-system writes to another app's private storage without elevated privileges.
This project uses Storage Access Framework (folder picker), so the user must explicitly grant folder access.

## Configure GitHub repo

Edit BuildConfig fields in `app/build.gradle`:

- `GITHUB_OWNER`
- `GITHUB_REPO`
- `GITHUB_BRANCH`
- `GITHUB_MANIFEST_PATH`

The app will show a configuration error until you replace placeholder values.

## Manifest format

Use `app/src/main/assets/sample-kodi-update-manifest.json` as template.

## Build/run

```bash
gradle test
gradle assembleDebug
```

Then install on Android TV device/emulator from Android Studio.
