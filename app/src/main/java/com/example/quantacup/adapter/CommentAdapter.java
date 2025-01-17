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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantacup.PostDetailActivity;
import com.example.quantacup.R;
import com.example.quantacup.bean.Comment;
import com.example.quantacup.bean.SubComment;
import com.example.quantacup.help.CircleCrop;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> comments;
    private PostDetailActivity postDetailActivity; // 添加这一行

    private HashSet<String> displayedCommentIds; // 用于存储已显示评论的 ID

    // 修改构造函数
    public CommentAdapter(Context context, List<Comment> comments, PostDetailActivity postDetailActivity) {
        this.context = context;
        this.comments = comments;
        this.postDetailActivity = postDetailActivity; // 赋值

    }

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.usernameTextView.setText(comment.getUserName());
        Glide.with(holder.itemView.getContext())
                .load(comment.getUserAvatarUrl()) // 从 Comment 中获取头像 URL
                .placeholder(R.drawable.default_avatar) // 头像加载前的占位图
                .error(R.drawable.pic_head) // 加载失败时的占位图
                .transform(new CircleCrop())
                .into(holder.userAvatarImageView);
        holder.contentTextView.setText(comment.getContent());
        holder.createdAtTextView.setText(comment.getCreatedAt());


        if (comment.getSubComments() != null && !comment.getSubComments().isEmpty()) {

            holder.subCommentRecyclerView.setVisibility(View.VISIBLE);
            holder.subCommentRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            holder.subCommentRecyclerView.setAdapter(new SubCommentAdapter(comment.getSubComments()));


            // 为子评论适配器提供点击事件监听器
            holder.subCommentRecyclerView.setAdapter(new SubCommentAdapter(comment.getSubComments(), subComment -> {
                // 在这里调用 showSubCommentsDialog
                showSubCommentsDialog(comment.getSubComments(),comment);
            }, context));
        } else {
            holder.subCommentRecyclerView.setVisibility(View.GONE); // 如果没有子评论，隐藏 RecyclerView
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            showCommentOptions(comment);
        });


    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    // 添加新评论并更新 RecyclerView
    public void addComment(Comment comment) {
        comments.add(comment);
        notifyItemInserted(comments.size() - 1); // 通知 RecyclerView 有新项目插入
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView userAvatarImageView;
        public TextView usernameTextView,contentTextView,createdAtTextView;
        public RecyclerView subCommentRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            userAvatarImageView = itemView.findViewById(R.id.comment_avatar);
            usernameTextView = itemView.findViewById(R.id.comment_user_name);
            contentTextView = itemView.findViewById(R.id.comment_content);
            createdAtTextView = itemView.findViewById(R.id.comment_created_at);
            subCommentRecyclerView = itemView.findViewById(R.id.sub_comment_recycler_view);
        }
    }

    // 显示子评论的 BottomSheetDialog
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void showSubCommentsDialog(List<SubComment> subComments, Comment comment) {
        // 创建 BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        // 设置布局
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.dialog_sub_comments, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        ImageView subCommentRefresh = bottomSheetView.findViewById(R.id.sub_comment_refresh);
        TextView subCommentNum = bottomSheetView.findViewById(R.id.sub_comment_num);
        ImageView subCommentDown = bottomSheetView.findViewById(R.id.sub_comment_down);
        RelativeLayout parentComment = bottomSheetView.findViewById(R.id.parent_comment);
        ImageView parentAvatar = bottomSheetView.findViewById(R.id.comment_avatar_parent);
        TextView parentUsername = bottomSheetView.findViewById(R.id.comment_user_name_parent);
        TextView parentCommentTime = bottomSheetView.findViewById(R.id.comment_created_at_parent);
        TextView parentContent = bottomSheetView.findViewById(R.id.comment_content_parent);

        // 设置数据
        Glide.with(context)
                .load(comment.getUserAvatarUrl()) // 从 Comment 中获取头像 URL
                .placeholder(R.drawable.default_avatar) // 头像加载前的占位图
                .error(R.drawable.pic_head) // 加载失败时的占位图
                .transform(new CircleCrop())
                .into(parentAvatar);

        parentUsername.setText(comment.getUserName()); // 设置用户名
        parentContent.setText(comment.getContent()); // 设置评论内容
        parentCommentTime.setText(comment.getCreatedAt()); // 设置评论时间

        // 设置 RecyclerView
        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.sub_comments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SubDetailCommentAdapter(context,subComments));

        // 更新 subCommentNum 显示子评论的数量
        subCommentNum.setText(subComments.size() + "条回复");
        // 刷新子评论数据
        subCommentRefresh.setOnClickListener(v -> {
            int noteId = comment.getNoteId();
            comments.clear();
            notifyDataSetChanged(); // 通知适配器更新
            postDetailActivity.fetchComments(noteId); // 调用 PostDetailActivity 的方法
        });

        parentComment.setOnClickListener(v->{
            showCommentOptions(comment);
        });
        // 关闭弹窗
        subCommentDown.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });
        // 显示对话框
        bottomSheetDialog.show();
    }
    // 显示评论选项的弹窗
    @SuppressLint("SetTextI18n")
    private void showCommentOptions(Comment comment) {
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
        tvContent.setText(comment.getUserName() + ":" + comment.getContent());
        // 按钮初始化
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnReply = dialog.findViewById(R.id.btnReply);
        Button btnCopy = dialog.findViewById(R.id.btnCopy);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // 如果当前用户是评论者，显示删除按钮
        if (comment.getUserId().equals(getCurrentUserId())) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                showDeleteConfirmationDialog(comment.getCommentId(),comment);
                dialog.dismiss();
            });
        } else {
            btnDelete.setVisibility(View.GONE); // 隐藏删除按钮
        }

        // 回复按钮的逻辑
        btnReply.setOnClickListener(v -> {
            showReplyInputDialog(comment);
            dialog.dismiss();
        });

        // 复制按钮的逻辑
        btnCopy.setOnClickListener(v -> {
            copyComment(comment.getContent()); // 复制评论的逻辑
            dialog.dismiss();
        });

        // 取消按钮的逻辑
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show(); // 显示Dialog
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
    private void showDeleteConfirmationDialog(int commentId,Comment comment) {
        // 创建 AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确认删除");
        builder.setMessage("删除评论后，评论下的所有回复都会被删除。");

        // 设置确定按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 用户点击了确定，执行删除操作
            deleteComment(commentId,comment);
        });

        // 设置取消按钮
        builder.setNegativeButton("取消", (dialog, which) -> {
            dialog.dismiss(); // 关闭对话框
        });

        // 创建并显示对话框
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void deleteComment(int commentId,Comment comment) {
        // 获取 SharedPreferences 中的 token
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 UserLoginToken，默认值为 null

        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // 创建 DELETE 请求的 URL
        String url = "http://8.134.144.65:8082/cofood/community/comments?commentId=" + commentId;

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
                         int position = comments.indexOf(comment);
                         if (position != -1) {
                           comments.remove(position);
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
    private void showReplyInputDialog(Comment comment) {
        // 创建一个Dialog
        Dialog replyDialog = new Dialog(context);
        replyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        replyDialog.setContentView(R.layout.dialog_reply_input); // 使用自定义布局

        // 获取控件
        EditText etReply = replyDialog.findViewById(R.id.etReply);
        Button btnSend = replyDialog.findViewById(R.id.btnSend);

        // 设置输入框的提示文字
        etReply.setHint("回复@" + comment.getUserName() + ":");

        // 隐藏发送按钮，直到有文本输入
        btnSend.setVisibility(View.GONE);

        // 监听输入变化
        etReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    // 修改 EditText 的宽度为 280dp
                    int widthInDp = 280;
                    float scale = context.getResources().getDisplayMetrics().density; // 获取屏幕密度
                    int widthInPx = (int) (widthInDp * scale + 0.5f); // dp 转换为 px
                    ViewGroup.LayoutParams params = etReply.getLayoutParams();
                    params.width = widthInPx; // 设置 EditText 宽度
                    etReply.setLayoutParams(params);

                    // 显示发送按钮
                    btnSend.setVisibility(View.VISIBLE);
                } else {
                    // 恢复 EditText 宽度为 MATCH_PARENT
                    ViewGroup.LayoutParams params = etReply.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    etReply.setLayoutParams(params);

                    // 隐藏发送按钮
                    btnSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> {
            String replyContent = etReply.getText().toString();
            sendReply(comment.getCommentId(), replyContent);
            replyDialog.dismiss();
        });

        // 设置Dialog的窗口属性
        Window window = replyDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); // 使布局适应键盘
            window.setGravity(Gravity.BOTTOM); // 设置Dialog位置为底部
        }

        replyDialog.show(); // 显示回复对话框

        // 弹出键盘并将焦点放在输入框上
        etReply.requestFocus(); // 请求 EditText 获取焦点
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etReply, InputMethodManager.SHOW_IMPLICIT); // 显示键盘
        }
    }
    private void sendReply(int commentId, String replyContent) {
        OkHttpClient client = new OkHttpClient();
        // 创建 POST 请求的 URL
        String url = "http://8.134.144.65:8082/cofood/community/subComments";

        // 从 SharedPreferences 获取 token
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 UserLoginToken，默认值为 null

        RequestBody formBody = new FormBody.Builder()
                .add("commentId", String.valueOf(commentId))
                .add("content", replyContent)
                .build();

        // 创建请求对象并添加请求头
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(formBody);

        // 如果 token 不为空，添加到请求头
        if (token != null) {
            requestBuilder.addHeader("token", token); // 使用 Bearer 作为前缀
        }

        Request request = requestBuilder.build();

        // 异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 处理请求失败
                e.printStackTrace();
                // 可以在主线程中显示 Toast
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "回复失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // 处理响应
                if (response.isSuccessful()) {
                    // 如果请求成功
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "回复成功", Toast.LENGTH_SHORT).show());
                } else {
                    // 如果请求失败
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "回复失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserId", MODE_PRIVATE);
        return sharedPreferences.getString("user_id", null); // -1 表示未找到
    }

}
