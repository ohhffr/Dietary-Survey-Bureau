package com.example.quantacup.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantacup.R;

public class TabAdapter extends RecyclerView.Adapter<TabAdapter.TabViewHolder> {
    private String[] tabTitles;
    public int selectedPosition = -1; // 用于跟踪选中的位置
    private OnTabClickListener listener;

    public TabAdapter(String[] tabTitles, OnTabClickListener listener) {
        this.tabTitles = tabTitles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_item, parent, false);
        return new TabViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.bind(tabTitles[position], position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            listener.onTabClick(position);
            selectedPosition = position;
            notifyDataSetChanged(); // 更新整个列表
        });
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }

    static class TabViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        TabViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tab_item_text); // 确保你有这个 TextView
        }

        void bind(String title, boolean isSelected) {
            titleTextView.setText(title);
            if (isSelected) {
                titleTextView.setTextColor(Color.GREEN); // 设置选中状态的颜色
                titleTextView.setTextSize(20);
            } else {
                titleTextView.setTextColor(Color.BLACK); // 设置未选中状态的颜色
                titleTextView.setTextSize(15);
            }
        }
    }

    public interface OnTabClickListener {
        void onTabClick(int position);
    }
}
