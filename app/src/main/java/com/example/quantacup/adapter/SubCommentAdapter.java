package com.example.quantacup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quantacup.R;
import com.example.quantacup.bean.Comment;
import com.example.quantacup.bean.SubComment;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class SubCommentAdapter extends RecyclerView.Adapter<SubCommentAdapter.ViewHolder> {
    private List<SubComment> subComments;
    private OnSubCommentClickListener listener; // 添加监听器
    private Context context;

    public SubCommentAdapter(List<SubComment> subComments, OnSubCommentClickListener listener, Context context) {
        this.subComments = subComments;
        this.listener = listener;
        this.context = context;
    }

    public SubCommentAdapter(List<SubComment> subComments) {
        this.subComments = subComments;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SubComment subComment = subComments.get(position);
        holder.userNameTextView.setText(subComment.getUserName());
        holder.contentTextView.setText(subComment.getContent());

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubCommentClick(subComment); // 调用接口方法
            }
        });
    }

    @Override
    public int getItemCount() {
        return subComments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userNameTextView,contentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.sub_comment_username);
            contentTextView = itemView.findViewById(R.id.sub_comment_content);

        }
    }

}
