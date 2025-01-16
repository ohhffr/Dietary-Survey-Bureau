package com.example.quantacup.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantacup.R;
import com.example.quantacup.bean.Comment;
import com.example.quantacup.bean.SubComment;
import com.example.quantacup.help.CircleCrop;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SubDetailCommentAdapter extends RecyclerView.Adapter<SubDetailCommentAdapter.SubDetailCommentViewHolder> {
    private List<SubComment> subComments;
    private Context context;
    public SubDetailCommentAdapter(Context context, List<SubComment> subComments) {
        this.context = context; // 保存上下文
        this.subComments = subComments;
    }

    @NonNull
    @Override
    public SubDetailCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_comment_detail_item, parent, false);
        return new SubDetailCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubDetailCommentViewHolder holder, int position) {
        SubComment subComment = subComments.get(position);
        holder.usernameTextView.setText(subComment.getUserName());
        holder.contentTextView.setText(subComment.getContent());
        holder.createdAtTextView.setText(subComment.getCreatedAt());


        // 使用 Glide 加载用户头像
        Glide.with(context)
                .load(subComment.getUserAvatarURL()) // 使用头像 URL
                .transform(new CircleCrop())
                .placeholder(R.drawable.default_avatar) // 占位图
                .error(R.drawable.pic_head) // 错误时的图
                .into(holder.userAvatar);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            showCommentOptions(subComment);
        });
    }

    @Override
    public int getItemCount() {
        return subComments.size();
    }

    public static class SubDetailCommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView userAvatar;
        public TextView usernameTextView, contentTextView, createdAtTextView;

        public SubDetailCommentViewHolder(View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.sub_comment_avatar);
            usernameTextView = itemView.findViewById(R.id.sub_comment_user_name);
            contentTextView = itemView.findViewById(R.id.sub_comment_content);
            createdAtTextView = itemView.findViewById(R.id.sub_comment_created_at);
        }
    }

    @SuppressLint("SetTextI18n")
    private void showCommentOptions(SubComment subComment) {
        // 创建一个Dialog
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 不显示标题
        dialog.setContentView(R.layout.dialog_comment_options); // 设置自定义布局

        // 获取Dialog的窗口
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // 设置宽度和高度
            window.setGravity(Gravity.BOTTOM); // 设置位置为底部
        }
        TextView tvContent = dialog.findViewById(R.id.tvContent);
        tvContent.setText(subComment.getUserName() + ":" + subComment.getContent());
        // 按钮初始化
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnReply = dialog.findViewById(R.id.btnReply);//暂时不实现
        Button btnCopy = dialog.findViewById(R.id.btnCopy);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // 如果当前用户是评论者，显示删除按钮
        if (subComment.getUserId().equals(getCurrentUserId())) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                showDeleteConfirmationDialog(subComment.getSubCommentId(),subComment);
                dialog.dismiss();
            });
        } else {
            btnDelete.setVisibility(View.GONE); // 隐藏删除按钮
        }


        // 复制按钮的逻辑
        btnCopy.setOnClickListener(v -> {
            copyComment(subComment.getContent()); // 复制评论的逻辑
            dialog.dismiss();
        });

        // 取消按钮的逻辑
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show(); // 显示Dialog
    }

    private void showDeleteConfirmationDialog(int subCommentId,SubComment subComment) {
        // 获取 SharedPreferences 中的 token
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 UserLoginToken，默认值为 null

        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // 创建 DELETE 请求的 URL
        String url = "http://8.134.144.65:8082/cofood/community/subComments?subCommentId=" + subCommentId;

        // 创建请求对象，并添加请求头
        assert token != null;
        Request request = new Request.Builder()
                .url(url)
                .delete() // 指定为 DELETE 请求
                .addHeader("token", token) // 添加 token 到请求头
                .build();
        // 异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 处理请求失败
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 处理响应
                if (response.isSuccessful()) {
                    // 删除成功，更新 UI
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 你可以在这里更新评论列表
                        int position = subComments.indexOf(subComment);
                        if (position != -1) {
                            subComments.remove(position);
                            notifyItemRemoved(position);
                        }
                        Toast.makeText(context, "评论已删除", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // 如果请求失败
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "删除失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserId", MODE_PRIVATE);
        return sharedPreferences.getString("user_id", null); // -1 表示未找到
    }

    private void copyComment(String content) {
        // 获取 ClipboardManager 实例
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // 创建 ClipData 对象，设置需要复制的文本
        ClipData clip = ClipData.newPlainText("Comment", content);

        // 将 ClipData 设置到剪贴板
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "评论已复制到剪贴板", Toast.LENGTH_SHORT).show(); // 提示用户已复制
        } else {
            Toast.makeText(context, "复制失败", Toast.LENGTH_SHORT).show(); // 提示复制失败
        }
    }
}
