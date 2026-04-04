package com.example.question_4;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<Uri> imageUris;

    public ImageAdapter(Context context, List<Uri> imageUris) {
        this.context   = context;
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.grid_item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);

        // Glide loads directly from URI
        Glide.with(context)
                .load(imageUri)
                .thumbnail(0.1f)
                .centerCrop()
                .into(holder.ivThumbnail);

        // On click — open ImageDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageDetailActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;

        ImageViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}