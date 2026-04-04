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

                    // Convert the content URI to a real file path
                    String path = getRealPathFromUri(treeUri);

                    if (path != null) {
                        // Check how many images are in this folder
                        File folder = new File(path);
                        File[] images = folder.listFiles((dir, name) -> {
                            String lower = name.toLowerCase();
                            return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                                    || lower.endsWith(".png") || lower.endsWith(".webp");
                        });

                        int count = (images == null) ? 0 : images.length;
                        Toast.makeText(this,
                                folder.getName() + ": " + count + " images found",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, GalleryActivity.class);
                        intent.putExtra("folder_path", path);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this,
                                "Could not read this folder. Try a different one.",
                                Toast.LENGTH_LONG).show();
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
        // Build actual paths and check which ones exist
        File baseDir = Environment.getExternalStorageDirectory();

        // Define all candidate folders with their real paths
        String[] folderNames = {
                "DCIM/Camera",
                "Pictures",
                "Downloads",
                "DCIM",
                "Documents"
        };

        // Only show folders that actually exist on this device
        List<String> existingNames = new ArrayList<>();
        List<File> existingPaths = new ArrayList<>();

        for (String name : folderNames) {
            File f = new File(baseDir, name);
            if (f.exists() && f.isDirectory()) {
                existingNames.add(name);
                existingPaths.add(f);
            }
        }

        // Also check standard Android paths directly
        File[] standardDirs = {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        };
        String[] standardNames = {"DCIM", "Pictures", "Downloads", "Documents"};

        for (int i = 0; i < standardDirs.length; i++) {
            // Avoid duplicates
            if (standardDirs[i].exists() && !existingPaths.contains(standardDirs[i])) {
                existingNames.add(standardNames[i]);
                existingPaths.add(standardDirs[i]);
            }
        }

        if (existingNames.isEmpty()) {
            Toast.makeText(this, "No accessible folders found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nameArray = existingNames.toArray(new String[0]);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Choose a Folder")
                .setItems(nameArray, (dialog, which) -> {
                    File chosenFolder = existingPaths.get(which);

                    // Check if folder has images before opening
                    File[] images = chosenFolder.listFiles((dir, name) -> {
                        String lower = name.toLowerCase();
                        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                                || lower.endsWith(".png") || lower.endsWith(".webp");
                    });

                    int imageCount = (images == null) ? 0 : images.length;
                    Toast.makeText(this,
                            "Opening " + chosenFolder.getName() +
                                    " (" + imageCount + " images found)",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, GalleryActivity.class);
                    intent.putExtra("folder_path", chosenFolder.getAbsolutePath());
                    startActivity(intent);
                })
                .show();
    }
}
