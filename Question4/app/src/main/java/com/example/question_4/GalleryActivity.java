package com.example.question_4;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvFolderName;

    // Store URI for refreshing on resume
    private String folderUriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recyclerView);
        tvFolderName = findViewById(R.id.tvFolderName);

        folderUriString = getIntent().getStringExtra("folder_uri");

        if (folderUriString == null) {
            Toast.makeText(this, "No folder selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadImages();
    }

    private void loadImages() {
        Uri treeUri = Uri.parse(folderUriString);

        // DocumentFile lets us read folder contents from a URI
        DocumentFile folder = DocumentFile.fromTreeUri(this, treeUri);

        if (folder == null || !folder.exists()) {
            Toast.makeText(this, "Cannot access folder.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvFolderName.setText("📁 " + folder.getName());

        // Get all image files from the folder
        List<Uri> imageUris = new ArrayList<>();
        DocumentFile[] files = folder.listFiles();

        if (files != null) {
            for (DocumentFile file : files) {
                if (file.isFile() && isImageFile(file.getName())) {
                    imageUris.add(file.getUri());
                }
            }
        }

        if (imageUris.isEmpty()) {
            Toast.makeText(this,
                    "No images found in " + folder.getName(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    imageUris.size() + " images found",
                    Toast.LENGTH_SHORT).show();
        }

        // 3 column grid
        // 3 column grid
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        ImageAdapter adapter = new ImageAdapter(this, imageUris);
        recyclerView.setAdapter(adapter);
    }

    // Check if file is an image by extension
    private boolean isImageFile(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".webp")
                || lower.endsWith(".gif");
    }

    // Refresh when returning from ImageDetailActivity
    @Override
    protected void onResume() {
        super.onResume();
        if (folderUriString != null) {
            loadImages();
        }
    }
}