package com.example.question_4;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import com.bumptech.glide.Glide;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    private ImageView ivFullImage;
    private TextView tvImageName, tvImagePath, tvImageSize, tvImageDate;
    private Button btnDelete;
    private Uri imageUri;
    private DocumentFile imageFile;

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

        // Get URI passed from ImageAdapter
        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString == null) { finish(); return; }

        imageUri  = Uri.parse(uriString);
        imageFile = DocumentFile.fromSingleUri(this, imageUri);

        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(this, "Image not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load full image
        Glide.with(this).load(imageUri).into(ivFullImage);

        // Fill in details
        tvImageName.setText(imageFile.getName());
        tvImagePath.setText(imageUri.toString());
        tvImageSize.setText(formatFileSize(imageFile.length()));

        // Date from last modified timestamp
        long lastModified = imageFile.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a", Locale.getDefault());
        tvImageDate.setText(
                lastModified > 0
                        ? sdf.format(new Date(lastModified))
                        : "Not available");

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete \""
                        + imageFile.getName() + "\"?\nThis cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage() {
        if (imageFile.delete()) {
            Toast.makeText(this, "Image deleted.", Toast.LENGTH_SHORT).show();
            finish(); // returns to gallery
        } else {
            Toast.makeText(this,
                    "Failed to delete. Folder may not allow deletions.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ── UTILITY ──────────────────────────────────────────────────────────────

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "Unknown";
        if (bytes < 1024) return bytes + " B";
        DecimalFormat df = new DecimalFormat("0.00");
        if (bytes < 1024 * 1024)
            return df.format(bytes / 1024.0) + " KB";
        return df.format(bytes / (1024.0 * 1024)) + " MB";
    }
}