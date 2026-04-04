package com.example.question_4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    private ImageView ivFullImage;
    private TextView tvImageName, tvImagePath, tvImageSize, tvImageDate;
    private Button btnDelete;

    // Holds reference to the current image file
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Connect views
        ivFullImage  = findViewById(R.id.ivFullImage);
        tvImageName  = findViewById(R.id.tvImageName);
        tvImagePath  = findViewById(R.id.tvImagePath);
        tvImageSize  = findViewById(R.id.tvImageSize);
        tvImageDate  = findViewById(R.id.tvImageDate);
        btnDelete    = findViewById(R.id.btnDelete);

        // Get the file path passed from ImageAdapter
        String imagePath = getIntent().getStringExtra("image_path");
        if (imagePath == null) { finish(); return; }

        imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Toast.makeText(this, "File not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Load and display image using Glide
        Glide.with(this).load(imageFile).into(ivFullImage);

        // ── Image Details ──
        // Name
        tvImageName.setText(imageFile.getName());

        // Full path
        tvImagePath.setText(imageFile.getAbsolutePath());

        // Size — convert bytes to readable format
        tvImageSize.setText(formatFileSize(imageFile.length()));
        // Date taken — from file last modified timestamp
        long lastModified = imageFile.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a", Locale.getDefault());
        tvImageDate.setText(sdf.format(new Date(lastModified)));

        // Delete button
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }
    // ── DELETE WITH CONFIRMATION DIALOG ─────────────────────────────────

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete \"" + imageFile.getName() + "\"?\nThis cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null) // null = dismiss dialog, do nothing
                .show();
    }
    private void deleteImage() {
        if (imageFile.delete()) {
            // Tell the Media Scanner that a file was deleted
            // Without this, the Gallery app may still show the deleted image
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(imageFile)));

            Toast.makeText(this, "Image deleted.", Toast.LENGTH_SHORT).show();

            // Go back to the gallery (finish closes this activity)
            finish();
        } else {
            Toast.makeText(this, "Failed to delete image. Check permissions.",
                    Toast.LENGTH_LONG).show();
        }
    }
    // ── UTILITY ──────────────────────────────────────────────────────────

    // Converts bytes to KB, MB, GB with 2 decimal places
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        DecimalFormat df = new DecimalFormat("0.00");
        if (bytes < 1024 * 1024)
            return df.format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024)
            return df.format(bytes / (1024.0 * 1024)) + " MB";
        return df.format(bytes / (1024.0 * 1024 * 1024)) + " GB";
    }
}

