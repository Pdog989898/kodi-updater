package com.example.kodiupdater;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "kodi_updater_prefs";
    private static final String KEY_KODI_FOLDER_URI = "kodi_folder_uri";

    private TextView statusText;
    private Button selectFolderButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        ContentResolver resolver = getContentResolver();
                        resolver.takePersistableUriPermission(uri, flags);

                        getPrefs().edit().putString(KEY_KODI_FOLDER_URI, uri.toString()).apply();
                        runUpdateCheck();
                    }
                } else {
                    setStatus("Kodi folder selection cancelled.");
                    selectFolderButton.setVisibility(android.view.View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        selectFolderButton = findViewById(R.id.selectFolderButton);
        selectFolderButton.setOnClickListener(v -> launchFolderPicker());

        runUpdateCheck();
    }

    private void runUpdateCheck() {
        setStatus(getString(R.string.checking_for_updates));
        selectFolderButton.setVisibility(android.view.View.GONE);

        Uri kodiFolderUri = getStoredKodiUri();
        if (kodiFolderUri == null) {
            setStatus("Kodi folder required. Choose your Kodi data directory.");
            selectFolderButton.setVisibility(android.view.View.VISIBLE);
            return;
        }

        executor.execute(() -> {
            UpdateManager manager = new UpdateManager(MainActivity.this);
            UpdateResult result = manager.run(kodiFolderUri);
            runOnUiThread(() -> {
                setStatus(result.message);
                if (!result.success) {
                    selectFolderButton.setVisibility(android.view.View.VISIBLE);
                }
            });
        });
    }

    private void launchFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        folderPickerLauncher.launch(intent);
    }

    private Uri getStoredKodiUri() {
        String uriText = getPrefs().getString(KEY_KODI_FOLDER_URI, null);
        if (uriText == null || uriText.isEmpty()) {
            return null;
        }
        return Uri.parse(uriText);
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private void setStatus(String value) {
        statusText.setText(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
