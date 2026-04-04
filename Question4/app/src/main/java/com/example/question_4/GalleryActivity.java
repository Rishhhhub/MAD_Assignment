package com.example.question_4;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvFolderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recyclerView);
        tvFolderName = findViewById(R.id.tvFolderName);

        // Get folder path from Intent extra
        String folderPath = getIntent().getStringExtra("folder_path");
        if (folderPath == null) {
            Toast.makeText(this, "No folder path provided.", Toast.LENGTH_SHORT).show();
            finish(); // close this activity
            return;
        }
        File folder = new File(folderPath);
        tvFolderName.setText("📁 " + folder.getName());

        // Load all image files from the folder
        List<File> images = getImagesFromFolder(folder);

        if (images.isEmpty()) {
            Toast.makeText(this, "No images found in this folder.",
                    Toast.LENGTH_SHORT).show();
        }
        // GridLayoutManager: 3 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Set adapter
        ImageAdapter adapter = new ImageAdapter(this, images);
        recyclerView.setAdapter(adapter);
    }

    // Filter only image files (jpg, jpeg, png, webp, gif)
    private List<File> getImagesFromFolder(File folder) {
        FilenameFilter imageFilter = (dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                    || lower.endsWith(".png") || lower.endsWith(".webp")
                    || lower.endsWith(".gif");
        };

        File[] files = folder.listFiles(imageFilter);
        if (files == null) {
            return new ArrayList<>();
        }

        // Sort by last modified (newest first)
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return new ArrayList<>(Arrays.asList(files));
    }

    // onResume(): reload images when returning from ImageDetailActivity
    // (in case the user deleted an image)
    @Override
    protected void onResume() {
        super.onResume();
        // Re-get folder path and refresh adapter
        String folderPath = getIntent().getStringExtra("folder_path");
        if (folderPath != null) {
            File folder = new File(folderPath);
            List<File> images = getImagesFromFolder(folder);
            ImageAdapter adapter = new ImageAdapter(this, images);
            recyclerView.setAdapter(adapter);
        }
    }
}
