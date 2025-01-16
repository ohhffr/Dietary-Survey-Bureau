package com.example.quantacup.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantacup.R;

import java.util.ArrayList;
import java.util.List;

public class PublishImageAdapter extends RecyclerView.Adapter<PublishImageAdapter.ViewHolder> {

    private List<Uri> imageUris;

    public PublishImageAdapter(List<Uri> imageUris) {
        this.imageUris = imageUris != null ? imageUris : new ArrayList<>(); // 处理空列表
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_publish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("PublishImageAdapter", "Binding position: " + position + ", Item count: " + getItemCount());
        Uri imageUri = imageUris.get(position);

        // 使用 Glide 加载图片
        Glide.with(holder.imageView.getContext())
                .load(imageUri)
                .into(holder.imageView);

        // 点击事件示例
        holder.imageView.setOnClickListener(v -> {
            //可以实现查看图片的逻辑
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_publish);
        }
    }
}
