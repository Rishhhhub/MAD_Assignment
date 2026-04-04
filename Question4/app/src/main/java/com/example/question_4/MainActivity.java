package com.example.question_4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import java.util.List;
import java.util.ArrayList;
import androidx.documentfile.provider.DocumentFile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnTakePhoto, btnBrowseFolder;

    // Stores the file path where the camera will save the photo
    private String currentPhotoPath;

    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri treeUri = result.getData().getData();

                    if (treeUri != null) {
                        // Pass the URI string directly — no path conversion needed
                        Intent intent = new Intent(this, GalleryActivity.class);
                        intent.putExtra("folder_uri", treeUri.toString());
                        startActivity(intent);
                    }
                }
            });

    // Launcher for taking a photo with the camera
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    Toast.makeText(this, "Photo saved to:\n" + currentPhotoPath,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Photo capture cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

    // Launcher for requesting multiple permissions at once
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                        boolean allGranted = !result.containsValue(false);
                        if (allGranted) {
                            launchCamera();
                        } else {
                            Toast.makeText(this,
                                    "Camera and storage permissions are required.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnBrowseFolder = findViewById(R.id.btnBrowseFolder);

        btnTakePhoto.setOnClickListener(v -> checkCameraPermission());
        btnBrowseFolder.setOnClickListener(v -> openFolderPicker());
    }

    // ── PERMISSIONS ────────────────────────────────────────────────────────

    private void checkCameraPermission() {
        List<String> needed = new ArrayList<>();

        // Always need CAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.CAMERA);
        }

        // Storage permission depends on Android version
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) { // Android 12 and below
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (needed.isEmpty()) {
            launchCamera();
        } else {
            permissionLauncher.launch(needed.toArray(new String[0]));
        }
    }

    // ── CAMERA ─────────────────────────────────────────────────────────────

    private void launchCamera() {
        try {
            // Create a unique file name using current timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            String imageFileName = "PHOTO_" + timeStamp + ".jpg";

            // Save to the public DCIM/Camera folder so it appears in the Gallery app
            File storageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "Camera");
            if (!storageDir.exists()) {
                storageDir.mkdirs(); // create folder if needed
            }

            File photoFile = new File(storageDir, imageFileName);
            currentPhotoPath = photoFile.getAbsolutePath();

            // FileProvider converts the file path to a content:// URI
            Uri photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);

            // Launch camera with the URI where it should save the photo
            cameraLauncher.launch(photoUri);

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── FOLDER PICKER ──────────────────────────────────────────────────────
    private void openFolderPicker() {
        // Use Android's built-in folder picker - works on all Android versions
        // No permission issues since the user picks the folder themselves
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        folderPickerLauncher.launch(intent);
    }

    private String getRealPathFromUri(Uri uri) {
        try {
            // Extract path from the document tree URI
            String docId = androidx.documentfile.provider.DocumentFile
                    .fromTreeUri(this, uri)
                    .getUri()
                    .getPath();

            // The URI path looks like: /tree/primary:DCIM/Camera
            // We need to convert it to: /storage/emulated/0/DCIM/Camera
            if (docId != null && docId.contains(":")) {
                String[] split = docId.split(":");
                // split[1] is the relative path e.g. "DCIM/Camera"
                if (split.length > 1) {
                    String relativePath = split[1];
                    return Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/" + relativePath;
                }
            }

            // Fallback: return root storage
            return Environment.getExternalStorageDirectory().getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
