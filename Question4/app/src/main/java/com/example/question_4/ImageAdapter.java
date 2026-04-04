package com.example.question_4;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

// RecyclerView.Adapter<VH> — VH is our ViewHolder type
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<File> imageFiles; // list of image files to display

    public ImageAdapter(Context context, List<File> imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    // onCreateViewHolder() — inflates one grid cell layout
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.grid_item_image, parent, false);
        return new ImageViewHolder(view);
    }

    // onBindViewHolder() — fills one grid cell with data
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);

        // Glide: load thumbnail into ImageView efficiently
        // .thumbnail(0.1f) loads a 10% quality preview first for speed
        Glide.with(context)
                .load(imageFile)
                .thumbnail(0.1f)
                .centerCrop()
                .into(holder.ivThumbnail);

        // On click: open ImageDetailActivity with the file path
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageDetailActivity.class);
            // Pass the absolute file path as a String extra
            intent.putExtra("image_path", imageFile.getAbsolutePath());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    // ViewHolder: caches the views inside one grid cell
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;

        ImageViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}
