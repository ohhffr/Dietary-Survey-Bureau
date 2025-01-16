package com.example.quantacup.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quantacup.PostDetailActivity;
import com.example.quantacup.bean.Post;
import com.example.quantacup.R;
import com.example.quantacup.help.CircleCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {
    private Context context;
    private List<Post> posts;

    public MyPostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

        Glide.with(context).load(post.getUserAvatarUrl()).transform(new CircleCrop()).into(holder.userAvatar);
        holder.username.setText(post.getUsername());
        holder.postTime.setText(post.getPostTime());
        holder.postContent.setText(post.getContent());


        checkIfPostIsFavorites(post.getNoteId(), post,holder); // 检查帖子是否已收藏

        // 获取本地用户 ID（假设通过 SharedPreferences 获取）
        String currentUserId = getCurrentUserId(); // 请实现此方法以获取本地用户 ID

        // 检查内容行数
        holder.postContent.post(() -> {
            if (holder.postContent.getLineCount() >= 5) {
                holder.postContentExpand.setVisibility(View.VISIBLE); // 显示“展开”按钮
            } else {
                holder.postContentExpand.setVisibility(View.GONE); // 隐藏“展开”按钮
            }
        });

        holder.postContentExpand.setOnClickListener(v -> {
            // 展开内容并更新文本
            holder.postContent.setMaxLines(Integer.MAX_VALUE); // 显示所有行
            holder.postContentExpand.setVisibility(View.GONE); // 隐藏展开按钮
            holder.postContentCollapse.setVisibility(View.VISIBLE); // 显示收起按钮

            holder.postContentCollapse.setOnClickListener(view1 -> {
                // 收起内容
                holder.postContent.setMaxLines(5);
                holder.postContentCollapse.setVisibility(View.GONE); // 隐藏收起按钮
                holder.postContentExpand.setVisibility(View.VISIBLE); // 显示展开按钮
            });
        });


        // 判断当前用户ID与帖子用户ID是否相同
        if (currentUserId != null && currentUserId.equals(String.valueOf(post.getUserId()))) {
            // 当前用户是帖子作者，显示删除按钮，隐藏关注按钮
            holder.postDelete.setVisibility(View.VISIBLE);
            holder.postFollowed.setVisibility(View.GONE);
        } else {
            // 当前用户不是帖子作者，显示关注按钮，隐藏删除按钮
            holder.postFollowed.setVisibility(View.VISIBLE);
            holder.postDelete.setVisibility(View.GONE);
        }


        // 显示图片 RecyclerView
        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            holder.postImages.setVisibility(View.VISIBLE);
            DisplayImageAdapter displayImageAdapter = new DisplayImageAdapter(context, post.getImageUrls());
            holder.postImages.setAdapter(displayImageAdapter);
            holder.postImages.setLayoutManager(new GridLayoutManager(context,3));
        } else {
            holder.postImages.setVisibility(View.GONE);
        }

        holder.postComment.setOnClickListener(v -> {
            // 显示 EditText 和提交按钮
            holder.editTextComment.setVisibility(View.VISIBLE);//评论编辑框
            holder.buttonComment.setVisibility(View.VISIBLE);//评论发送按钮
            holder.editTextComment.addTextChangedListener(new TextWatcher() { //监听编辑框文本变化事件
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // 检查编辑框内容
                    if (charSequence.length() > 0) {//编辑框不为空
                        holder.buttonComment.setBackgroundResource(R.drawable.input_button); // 设置按钮背景为输入框按钮
                        holder.buttonComment.setTextColor(Color.WHITE); // 设置按钮文本颜色为白色
                    } else {
                        // 如果编辑框为空，恢复之前的颜色和文本
                        holder.buttonComment.setBackgroundResource(R.drawable.input_not_button); // 恢复默认按钮背景
                        holder.buttonComment.setTextColor(Color.GRAY); // 恢复默认文本颜色
                    }

                }
                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            // 请求焦点并弹出键盘
            holder.editTextComment.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(holder.editTextComment, InputMethodManager.SHOW_IMPLICIT);

            // 提交按钮点击事件
            holder.buttonComment.setOnClickListener(view -> {
                String comment = holder.editTextComment.getText().toString();
                if (!comment.isEmpty()) {
                    submitComment(post.getNoteId(), comment); // 调用提交评论的方法
                    holder.editTextComment.setText(""); // 清空输入框
                    holder.editTextComment.setVisibility(View.GONE); // 隐藏输入框
                    holder.buttonComment.setVisibility(View.GONE); // 隐藏提交按钮
                } else {
                    Toast.makeText(context, "评论不能为空", Toast.LENGTH_SHORT).show();
                }
            });
        });
        // 获取评论数
        getCommentCount(post.getNoteId(), holder);
        // 获取收藏数
        getCollectCount(post.getNoteId(), holder);

        holder.postDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(position);//删除帖子弹窗
        });

        holder.postFollowed.setOnClickListener(v ->{
            showUnfollowDialog(post.getUserId(), holder);//取消关注弹窗
        });

        // 点击事件处理
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("NOTE_ID", post.getNoteId()); // 传递帖子的 ID
            context.startActivity(intent); // 启动详情页面
        });

        // 添加点击事件处理
        holder.postCollect.setOnClickListener(v -> {
            // 检查当前收藏状态，假设有一个方法来获取收藏状态
            if (post.isCollected()) { // 假设 post 有 isCollected 方法返回当前状态
                // 取消收藏
                holder.postCollect.setImageResource(R.drawable.baseline_star_border_24); // 设置为未收藏图标
                cancelCollect(post.getNoteId(), post);
            } else {
                // 收藏
                holder.postCollect.setImageResource(R.drawable.baseline_star_24); // 设置为已收藏图标
                collectPost(post.getNoteId(), post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar,postComment,postCollect;
        TextView username,postTime,postContent,commentNum,postContentExpand,postContentCollapse,postCollectNum;
        RecyclerView postImages; // 使用 RecyclerView 替代 GridView
        ImageView postDelete; // 删除按钮
        EditText editTextComment;
        Button postFollowed,buttonComment;

        public ViewHolder(View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.post_user_avatar);
            username = itemView.findViewById(R.id.post_username);
            postTime = itemView.findViewById(R.id.post_time);
            postContent = itemView.findViewById(R.id.post_content);
            postContentExpand = itemView.findViewById(R.id.post_content_expand);//展开内容
            postContentCollapse = itemView.findViewById(R.id.post_content_collapse);//收起内容
            postImages = itemView.findViewById(R.id.post_images); // RecyclerView
            postDelete = itemView.findViewById(R.id.post_delete); // 获取删除按钮引用
            postFollowed = itemView.findViewById(R.id.post_followed);
            postComment = itemView.findViewById(R.id.comment);//评论
            editTextComment = itemView.findViewById(R.id.edit_text_comment_my);//评论输入框
            buttonComment = itemView.findViewById(R.id.button_comment_my);//发表评论按钮
            commentNum = itemView.findViewById(R.id.comment_num);//评论数
            postCollect = itemView.findViewById(R.id.collection);//收藏
            postCollectNum = itemView.findViewById(R.id.collection_num);//收藏数
            postImages.setLayoutManager(new GridLayoutManager(itemView.getContext(), 3)); // 设置布局管理器
        }
    }

    private void collectPost(int noteId, Post post) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 Token

        if (token == null) {
            Toast.makeText(context, "请登录以收藏帖子", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // 创建请求体
        RequestBody body = new FormBody.Builder()
                .add("noteId", String.valueOf(noteId))
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url("http://8.134.144.65:8082/cofood/community/favourite")
                .post(body)
                .addHeader("token", token) // 设置请求头
                .build();

        // 发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "收藏失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "已成功收藏帖子", Toast.LENGTH_SHORT).show();
                        post.setCollected(true); // 更新帖子的收藏状态
                    });
                } else {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "收藏失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void cancelCollect(int noteId, Post post) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 Token

        if (token == null) {
            Toast.makeText(context, "请登录以取消收藏", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient();


        // 创建请求
        Request request = new Request.Builder()
                .url("http://8.134.144.65:8082/cofood/community/favourite?noteId=" + noteId) // 假设这里是取消收藏的 URL
                .delete()
                .addHeader("token", token) // 设置请求头
                .build();

        // 发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "取消收藏失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "已成功取消收藏帖子", Toast.LENGTH_SHORT).show();
                        post.setCollected(false); // 更新帖子的收藏状态
                    });
                } else {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "取消收藏失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void checkIfPostIsFavorites(int noteId, Post post, MyPostsAdapter.ViewHolder holder) {
        // 创建 OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // 创建请求
        Request request = new Request.Builder()
                .url("http://8.134.144.65:8082/cofood/community/favouriteByNoteId?noteId=" + noteId)
                .get()
                .build();

        // 发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "获取收藏状态失败，请重试", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray favouriteList = jsonResponse.getJSONObject("data").getJSONArray("favouriteList");
                        String currentUserId = getCurrentUserId(); // 获取当前用户ID

                        // 检查当前用户ID是否为空
                        if (currentUserId == null || currentUserId.isEmpty()) {
                            ((Activity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "用户ID无效，请重试", Toast.LENGTH_SHORT).show();
                            });
                            return; // 返回，停止执行
                        }

                        boolean isFavorited = false;

                        for (int i = 0; i < favouriteList.length(); i++) {
                            JSONObject favourite = favouriteList.getJSONObject(i);
                            if (favourite.getInt("userId") == Integer.parseInt(currentUserId)) {
                                isFavorited = true;
                                break;
                            }
                        }

                        // 更新 UI 以显示收藏状态
                        boolean finalIsFavorited = isFavorited;
                        ((Activity) context).runOnUiThread(() -> {
                            if (finalIsFavorited) {
                                holder.postCollect.setImageResource(R.drawable.baseline_star_24); // 设置为已收藏图标
                                post.setCollected(true); // 更新帖子的收藏状态
                            } else {
                                holder.postCollect.setImageResource(R.drawable.baseline_star_border_24); // 设置为未收藏图标
                                post.setCollected(false); // 更新帖子的收藏状态
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Activity) context).runOnUiThread(() -> {
                            Toast.makeText(context, "解析响应失败，请重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "获取收藏状态失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }


    private void getCollectCount(int noteId, MyPostsAdapter.ViewHolder holder) {
        String url = "http://8.134.144.65:8082/cofood/community/favouriteByNoteId?noteId=" + noteId;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 处理请求失败
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 获取响应体
                    assert response.body() != null;
                    String jsonResponse = response.body().string();

                    // 解析JSON
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONArray favouriteList = data.getJSONArray("favouriteList");

                        // 更新收藏数
                        int collectCount = favouriteList.length();

                        // 在主线程中更新UI
                        ((Activity) context).runOnUiThread(() -> {
                            holder.postCollectNum.setText(String.valueOf(collectCount)); // 更新收藏数
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //弹出提醒是否要删除该帖子
    private void showDeleteConfirmationDialog(int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NoteId",-1);
        int savedNoteId = sharedPreferences.getInt("noteId_", -1); // -1 是默认值，表示未找到

        new AlertDialog.Builder(context)
                .setTitle("确认删除")
                .setMessage("您确定要删除这条帖子吗？")
                .setPositiveButton("确认", (dialog, which) -> deletePostByNoteId(savedNoteId, position))
                .setNegativeButton("取消", null)
                .show();
    }

    // 删除帖子
    private void deletePostByNoteId(int noteId, int position) {
        OkHttpClient client = new OkHttpClient();
        Log.e("TAG", "deletePostByNoteId: " + noteId + " " + position );
        String url = "http://8.134.144.65:8082/cofood/community/Note?noteId=" + noteId; // 构建 URL

        Request request = new Request.Builder()
                .url(url)
                .delete() // 设置为 DELETE 请求
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("PostsAdapter", "Delete request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    Log.e("PostsAdapter", "Post deleted successfully: " + noteId);

                    // 在主线程更新 UI
                    ((Activity) context).runOnUiThread(() -> {
                        posts.remove(position); // 从列表中移除该帖子
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, posts.size());
                    });
                } else {
                    Log.e("PostsAdapter", "Delete request failed: " + response.code() + " " + response.message());
                }
            }
        });
    }

    //弹出提醒是否要取消关注该用户
    private void showUnfollowDialog(String followeeId, MyPostsAdapter.ViewHolder holder) {
        new AlertDialog.Builder(context)
                .setTitle("确认取消关注")
                .setMessage("您确定要取消关注此用户吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    unfollowUser(followeeId, holder);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    //取关
    private void unfollowUser(String followeeId, MyPostsAdapter.ViewHolder holder) {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserLoginToken", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 如果没有找到，返回 null

        // 修改 URL 为关注接口，使用 DELETE 方法
        final String url = "http://8.134.144.65:8082/cofood/community/follow";

        // 通过 URL 查询参数传递 followeeId
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder()
                .addQueryParameter("followeeId", followeeId)
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(httpUrl) // 使用构建的 URL
                .delete(); // 设置请求方法为 DELETE

        if (token != null) {
            requestBuilder.addHeader("token", token); // 将 token 添加到请求头
        }

        Request request = requestBuilder.build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", "取消关注请求失败: " + e.getMessage());
                // 在 UI 线程中更新按钮状态
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "取消关注失败，请重试！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Log.i("TAG", "取消关注成功: " + response.body().string());

                    // 在 UI 线程中更新按钮状态
                    ((Activity) context).runOnUiThread(() -> {
                        holder.postFollowed.setEnabled(true);
                        holder.postFollowed.setText("+关注");
                        holder.postFollowed.setBackgroundResource(R.drawable.followed_not);
                        Toast.makeText(context, "取消关注成功!", Toast.LENGTH_SHORT).show();

                        // 停止定时刷新

                    });
                } else {
                    Log.e("TAG", "取消关注失败: " + response.message());
                    // 在 UI 线程中更新按钮状态
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "取消关注失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void submitComment(int noteId, String comment) {
        // 确保此方法在非UI线程中调用
        OkHttpClient client = new OkHttpClient();

        // 从 SharedPreferences 获取 Token
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 替换为你的键名

        // 创建请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("noteId", String.valueOf(noteId))
                .add("content", comment)
                .build();


        // 创建请求并添加请求头
        Request.Builder requestBuilder = new Request.Builder()
                .url("http://8.134.144.65:8082/cofood/community/comments")
                .post(requestBody);

        // 如果 token 存在，则添加到请求头
        if (token != null) {
            requestBuilder.addHeader("token", token); // 或者根据你的 API 需要使用不同的头名称
        }

        Request request = requestBuilder.build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "评论提交失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "评论提交成功", Toast.LENGTH_SHORT).show();
                        try {
                            Log.e("TAG", "onResponse: " + response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        // 刷新评论或更新UI等
                    });
                } else {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "评论提交失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    //网络请求，该请求是返回该帖子的所有评论，通过list[]"comments"的长度加子评论subComments[]的长度即为评论数
    private void getCommentCount(int noteId, MyPostsAdapter.ViewHolder holder) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/community/comments?noteId=" + noteId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("AllPostsAdapter", "Failed to fetch comments: " + e.getMessage());
                // 处理请求失败的情况
                holder.commentNum.setText("0"); // 失败时显示 0
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        if (jsonObject.getInt("code") == 200) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            JSONArray comments = data.getJSONArray("comments");

                            int totalCommentCount = 0; // 用于统计总评论数

                            // 遍历父评论
                            for (int i = 0; i < comments.length(); i++) {
                                JSONObject comment = comments.getJSONObject(i);
                                // 统计父评论
                                totalCommentCount++;

                                // 统计子评论
                                if (comment.has("subComments")) {
                                    JSONArray subComments = comment.getJSONArray("subComments");
                                    totalCommentCount += subComments.length(); // 累加子评论数量
                                }
                            }

                            // 在主线程更新UI
                            int finalTotalCommentCount = totalCommentCount;
                            ((Activity) context).runOnUiThread(() -> holder.commentNum.setText(String.valueOf(finalTotalCommentCount)));
                        } else {
                            Log.e("AllPostsAdapter", "Error: " + jsonObject.getString("msg"));
                        }
                    } catch (Exception e) {
                        Log.e("AllPostsAdapter", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    Log.e("AllPostsAdapter", "Response not successful: " + response.message());
                }
            }
        });
    }

    //从本地获得userId
    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserId", MODE_PRIVATE);
        return sharedPreferences.getString("user_id", null); // -1 表示未找到
    }
}
