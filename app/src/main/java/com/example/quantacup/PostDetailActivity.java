package com.example.quantacup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quantacup.adapter.CommentAdapter;
import com.example.quantacup.adapter.DisplayImageAdapter;
import com.example.quantacup.bean.Comment;
import com.example.quantacup.bean.SubComment;
import com.example.quantacup.help.CircleCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostDetailActivity extends AppCompatActivity {
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private boolean isCollected = false; // Track if the post is collected
    private ImageView userAvatar,detailCommentIcon,detailFavorite,detailCollect;
    private TextView usernameTextView,postContent,postTime,postContentExpand,postContentCollapse,detailCommentNum,detailFavoriteNum,
                    detailCollectNum,detailCommentNumBig;
    private RecyclerView postImages,commentRecyclerView; // 使用 RecyclerView 替代 GridView
    private Button postFollowed,detailSendButton;
    private EditText detailComment;
    private OkHttpClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        initView();


        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this,commentList,this);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this));
        commentRecyclerView.setAdapter(commentAdapter);

        Intent intent = getIntent();
        int noteId = intent.getIntExtra("NOTE_ID", -1);
        if (noteId != -1) {
            loadPostDetails(noteId); // 根据 noteId 加载详细帖子信息
            getCommentCount(noteId);
            checkIfPostIsFavorites(noteId); // 检查帖子是否已被收藏
            fetchComments(noteId);
            getCollectCount(noteId);
        }

        detailComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    hideViews(); // 隐藏其他视图
                    adjustEditTextWidth(true); // 增加宽度
                    detailSendButton.setVisibility(View.VISIBLE);
                }else {
                    showViews();
                    adjustEditTextWidth(false);
                    detailSendButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        detailSendButton.setOnClickListener(view -> {
            String comment = detailComment.getText().toString();
            if (!comment.isEmpty()){
                submitComment(noteId, comment); // 调用提交评论的方法
                detailComment.setText(""); // 清空输入框
            }
        });

        // Collect button click listener
        detailCollect.setOnClickListener(view -> {
            if (isCollected) {
                cancelCollect(noteId);
            } else {
                collectPost(noteId);
            }
            isCollected = !isCollected;
            updateCollectIcon();
        });
        // 设置 detailCommentIcon 的点击监听器
        detailCommentIcon.setOnClickListener(v -> {
            detailComment.requestFocus(); // 请求焦点
            detailComment.setSelection(detailComment.length()); // 将光标移至文本末尾
            // 显示软键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(detailComment, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void collectPost(int noteId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 Token

        if (token == null) {
            Toast.makeText(PostDetailActivity.this, "请登录以收藏帖子", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(() -> Toast.makeText(PostDetailActivity.this, "收藏失败，请重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                  runOnUiThread(() -> Toast.makeText(PostDetailActivity.this, "已成功收藏帖子", Toast.LENGTH_SHORT).show());
                } else {
                  runOnUiThread(() -> Toast.makeText(PostDetailActivity.this, "收藏失败，请重试", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void cancelCollect(int noteId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 Token

        if (token == null) {
            Toast.makeText(PostDetailActivity.this, "请登录以取消收藏", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(() -> Toast.makeText(PostDetailActivity.this, "取消收藏失败，请重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(PostDetailActivity.this, "已成功取消收藏帖子", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(PostDetailActivity.this, "取消收藏失败，请重试", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void checkIfPostIsFavorites(int noteId) {
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
                runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "获取收藏状态失败，请重试", Toast.LENGTH_SHORT).show();
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

                        boolean isFavorited = false;

                        for (int i = 0; i < favouriteList.length(); i++) {
                            JSONObject favourite = favouriteList.getJSONObject(i);
                            if (favourite.getInt("userId") == Integer.parseInt(currentUserId)) {
                                isFavorited = true;
                                break;
                            }
                        }
                        // 检查当前用户ID是否为空
                        if (currentUserId == null || currentUserId.isEmpty()) {
                           runOnUiThread(() -> {
                                Toast.makeText(PostDetailActivity.this, "用户ID无效，请重试", Toast.LENGTH_SHORT).show();
                            });
                            return; // 返回，停止执行
                        }

                        // 更新 UI 以显示收藏状态
                        boolean finalIsFavorited = isFavorited;
                        runOnUiThread(() -> {
                            isCollected = finalIsFavorited; // 更新收藏状态
                            updateCollectIcon(); // 更新图标
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(PostDetailActivity.this, "解析响应失败，请重试", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PostDetailActivity.this, "获取收藏状态失败，请重试", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void getCollectCount(int noteId) {
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
                       runOnUiThread(() -> {
                           detailCollectNum.setText(String.valueOf(collectCount)); // 更新收藏数
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @SuppressLint("WrongViewCast")
    private void initView() {

        userAvatar = findViewById(R.id.post_user_avatar_detail);//用户头像
        usernameTextView = findViewById(R.id.post_username_detail);//用户名
        postTime = findViewById(R.id.post_time_detail);//发布时间
        postContent = findViewById(R.id.post_content_detail);//发表内容
        postContentExpand = findViewById(R.id.post_content_expand_detail);//展开内容
        postContentCollapse = findViewById(R.id.post_content_collapse_detail);//收起内容
        postImages = findViewById(R.id.post_images_detail); // RecyclerView 发布图片
        postFollowed = findViewById(R.id.post_followed_detail);//是否关注的按钮
        detailSendButton = findViewById(R.id.detail_send_comment);//底部发送评论按钮
        detailComment = findViewById(R.id.detail_comment);//底部评论编辑框
        detailFavorite = findViewById(R.id.detail_favorite);//底部爱心点赞
        detailFavoriteNum = findViewById(R.id.detail_favorite_num);//底部爱心点赞数
        detailCollect = findViewById(R.id.detail_collect);//底部收藏
        detailCollectNum = findViewById(R.id.detail_collect_num);//底部收藏数
        detailCommentIcon = findViewById(R.id.detail_comment_icon);//底部评论
        detailCommentNum = findViewById(R.id.detail_comment_num);//底部评论数
        commentRecyclerView = findViewById(R.id.comment_list);//评论列表
        detailCommentNumBig = findViewById(R.id.detail_comment_num_big);//评论数（黑大粗字体）



        Toolbar toolbar = findViewById(R.id.PostDetailActivity_btn);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> finish());

    }

    public void fetchComments(int noteId) {
        String url = "http://8.134.144.65:8082/cofood/community/comments?noteId=" + noteId; // 替换为你的 API 端点
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "获得评论失败！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonResponse = response.body().string();
                    parseComments(jsonResponse);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PostDetailActivity.this, "服务器未成功响应！" + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }



    @SuppressLint("NotifyDataSetChanged")
    private void parseComments(String jsonResponse) {
        commentList = new ArrayList<>();

        int totalCommentCount = 0; // 变量用于统计总评论数（父评论 + 子评论）
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray commentsArray = dataObject.getJSONArray("comments");

            for (int i = 0; i < commentsArray.length(); i++) {
                JSONObject commentObject = commentsArray.getJSONObject(i);

                int commentId = commentObject.getInt("commentId");
                int noteId = commentObject.getInt("noteId");
                String userId = commentObject.getString("userId");
                String content = commentObject.getString("content");
                long createdAt = commentObject.getLong("createdAt");
                String commentTime = formatTimestamp(createdAt);

                // 统计父评论
                totalCommentCount++; // 父评论计数加一

                // 解析子评论
                List<SubComment> subCommentsList = new ArrayList<>();
                JSONArray subCommentsArray = commentObject.getJSONArray("subComments");
                CountDownLatch latch = new CountDownLatch(subCommentsArray.length());
                for (int j = 0; j < subCommentsArray.length(); j++) {
                    JSONObject subCommentObject = subCommentsArray.getJSONObject(j);
                    int subCommentId = subCommentObject.getInt("subCommentId");
                    String subUserId = subCommentObject.getString("userId");
                    String subContent = subCommentObject.getString("content");
                    long subCreatedAt = subCommentObject.getLong("createdAt");
                    String subCommentTime = formatTimestamp(subCreatedAt);

                    // 统计子评论
                    totalCommentCount++; // 每个子评论计数加一

                    // 添加子评论到列表
                    // 使用 CountDownLatch 来等待所有子评论的网络请求完成
                    fetchSubCommentUserInfo(subUserId, (username, userAvatarURL) -> {
                        // 添加子评论到列表
                        subCommentsList.add(new SubComment(subCommentId,subUserId, username, subContent,subCommentTime ,userAvatarURL));
                        latch.countDown(); // 计数减1
                    });
                }

                int finalTotalCommentCount = totalCommentCount;
                runOnUiThread(() -> {
                    detailCommentNum.setText(String.valueOf(finalTotalCommentCount)); // 更新小评论数
                    detailCommentNumBig.setText(String.valueOf(finalTotalCommentCount)); // 更新大评论数
                    commentAdapter.notifyDataSetChanged(); // 通知适配器刷新
                });

                // 等待所有子评论的用户信息加载完成
                latch.await(); // 此处会阻塞，直到计数为0
                // 加载用户信息并将其传递到 Comment 对象
                fetchCommentUserInfo(noteId,commentId,userId, content, commentTime, subCommentsList);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(PostDetailActivity.this, "Failed to parse comments", Toast.LENGTH_SHORT).show();
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void fetchSubCommentUserInfo(String subUserId, SubCommentCallback callback) {
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo?id=" + subUserId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", "onFailure: ");
                callback.onUserInfoFetched("",""); // 传递空用户名
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseSubCommentUserResponse(responseData, callback);
                } else {
                    callback.onUserInfoFetched("",""); // 传递空用户名
                }
            }
        });
    }
    private void parseSubCommentUserResponse(String responseData, SubCommentCallback callback) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            if (jsonResponse.getInt("code") == 200) {
                JSONObject user = jsonResponse.getJSONObject("data").getJSONObject("user");
                String username = user.getString("username");
                String userAvatarURL = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + user.getString("avatar");
                callback.onUserInfoFetched(username,userAvatarURL);
            } else {
                callback.onUserInfoFetched("",""); // 传递空用户名
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onUserInfoFetched("",""); // 传递空用户名
        }
    }


    private void fetchCommentUserInfo(int noteId,int commentId,String userId, String content, String commentTime, List<SubComment> subCommentsList) {
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo?id=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", "onFailure: " );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseCommentUserResponse(userId, commentId,noteId, responseData, content, commentTime, subCommentsList);
                }
            }
        });
    }


    // 在 parseCommentUserResponse 方法中更新评论列表
    private void parseCommentUserResponse(String userId,int commentId, int noteId, String responseData, String content, String commentTime, List<SubComment> subCommentsList) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            if (jsonResponse.getInt("code") == 200) {
                JSONObject user = jsonResponse.getJSONObject("data").getJSONObject("user");
                String username = user.getString("username");
                String avatarUrl = user.getString("avatar");
                String userAvatarUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatarUrl;

                Comment comment = new Comment(commentId,noteId, userId,username, userAvatarUrl,content,commentTime, subCommentsList);

                runOnUiThread(() -> {
                    commentAdapter.addComment(comment); // 更新评论列表
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadPostDetails(int noteId) {
        client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/community/NoteByNoteId?noteId=" + noteId;


        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(()-> Toast.makeText(PostDetailActivity.this, "请求失败！" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    assert response.body() != null;
                    String responseData = response.body().string();
                    try {
                        JSONObject responseObject = new JSONObject(responseData);
                        JSONObject dataObject = responseObject.getJSONObject("data");
                        JSONObject noteObject = dataObject.getJSONObject("note");
                        String userId = noteObject.getString("userId");
                        String title = noteObject.getString("title");
                        String content = noteObject.getString("content");
                        long createdAt = noteObject.getLong("createdAt");
                        String timestamp = formatTimestamp(createdAt);

                        JSONArray imagesArray = noteObject.getJSONArray("images");
                        List<String> imageUrls = new ArrayList<>();
                        for (int i = 0; i < imagesArray.length(); i++){
                            JSONObject imageObject = imagesArray.getJSONObject(i);
                            String imageUrl = imageObject.getString("url");
                            // 拼接完整的图片 URL
                            String fullImageUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + imageUrl;
                            imageUrls.add(fullImageUrl);
                        }

                        fetchAllUserInfo(userId, title, content, timestamp, imageUrls);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void fetchAllUserInfo(String userId, String title, String content, String timestamp, List<String> imageUrls) {
        client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo?id=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", "onFailure: " );
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);

                        if (jsonResponse.getInt("code") == 200) {
                            JSONObject user = jsonResponse.getJSONObject("data").getJSONObject("user");
                            String username = user.getString("username");
                            String avatarUrl = user.getString("avatar");
                            String userAvatarUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatarUrl;
                            Log.e("TAG", "parseUserResponse: " + userAvatarUrl );
                            runOnUiThread(() -> {
                                usernameTextView.setText(username);
                                Glide.with(PostDetailActivity.this).load(userAvatarUrl).transform(new CircleCrop()).into(userAvatar);
                                postTime.setText(timestamp);
                                postContent.setText(title + "\n" + content);
                                if (imageUrls != null && !imageUrls.isEmpty()){
                                    postImages.setVisibility(View.VISIBLE);
                                    DisplayImageAdapter displayImageAdapter = new DisplayImageAdapter(PostDetailActivity.this,imageUrls);
                                    postImages.setAdapter(displayImageAdapter);
                                    postImages.setLayoutManager(new GridLayoutManager(PostDetailActivity.this, 3));
                                }
                                expandAndCollapse();
                                followAndWithdraw(userId);
                            });


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void followUser(String followeeId) {
        OkHttpClient client = new OkHttpClient();
        // 从 SharedPreferences 中读取 token
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 如果没有找到，返回 null

        final String url = "http://8.134.144.65:8082/cofood/community/follow";

        RequestBody body = new FormBody.Builder()
                .add("followeeId", followeeId)
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        if (token != null) {
            requestBuilder.addHeader("token", token); // 将 token 添加到请求头
        }

        Request request = requestBuilder.build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", "请求失败: " + e.getMessage());
                // 在 UI 线程中更新按钮状态
                runOnUiThread(() -> {
                    postFollowed.setEnabled(true);
                    Toast.makeText(PostDetailActivity.this, "关注失败，请重试！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;

                    // 在 UI 线程中更新按钮状态
                     runOnUiThread(() -> {
                        postFollowed.setEnabled(false);
                        postFollowed.setText("已关注");
                        postFollowed.setBackgroundResource(R.drawable.followed_my);
                        Toast.makeText(PostDetailActivity.this, "关注成功!", Toast.LENGTH_SHORT).show();

                    });
                } else {
                    // 在 UI 线程中更新按钮状态
                   runOnUiThread(() -> {
                        postFollowed.setEnabled(true);
                        Toast.makeText(PostDetailActivity.this, "关注失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showUnfollowDialog(String followeeId) {
        new AlertDialog.Builder(PostDetailActivity.this)
                .setTitle("确认取消关注")
                .setMessage("您确定要取消关注此用户吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    unfollowUser(followeeId);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void unfollowUser(String followeeId) {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", MODE_PRIVATE);
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
               runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "取消关注失败，请重试！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    Log.i("TAG", "取消关注成功: " + response.body().string());

                    // 在 UI 线程中更新按钮状态
                   runOnUiThread(() -> {
                        postFollowed.setEnabled(true);
                        postFollowed.setText("+关注");
                        postFollowed.setBackgroundResource(R.drawable.followed_not);
                        Toast.makeText(PostDetailActivity.this, "取消关注成功!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.e("TAG", "取消关注失败: " + response.message());
                    // 在 UI 线程中更新按钮状态
                    runOnUiThread(() -> {
                        Toast.makeText(PostDetailActivity.this, "取消关注失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void getFollowList(String followeeId) {
        OkHttpClient client = new OkHttpClient();

        // 从 SharedPreferences 中读取 token
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 如果没有找到，返回 null

        final String url = "http://8.134.144.65:8082/cofood/community/followList";

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        if (token != null) {
            requestBuilder.addHeader("token", token); // 将 token 添加到请求头
        }

        Request request = requestBuilder.build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("TAG", "获取关注列表失败: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();

                    // 解析 JSON 数据
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray followList = jsonObject.getJSONObject("data").getJSONArray("followList");

                        boolean isFollowed = false;//初始化关注状态为 未关注
                        for (int i = 0; i < followList.length(); i++) {
                            JSONObject followItem = followList.getJSONObject(i);
                            String currentFolloweeId = followItem.getString("followeeId");
                            if (currentFolloweeId.equals(followeeId)) { //如果得到的关注列表中 获得该用户的id 与 发布该帖子的用户id 相同 说明已关注
                                isFollowed = true;
                                break;
                            }
                        }

                        // 在 UI 线程中更新按钮状态
                        boolean finalIsFollowed = isFollowed;
                        runOnUiThread(() -> {
                            if (finalIsFollowed) {
                                postFollowed.setText("已关注");
                                postFollowed.setBackgroundResource(R.drawable.followed_my);
                            } else {
                                postFollowed.setEnabled(true);
                                postFollowed.setText("+关注");
                                postFollowed.setBackgroundResource(R.drawable.followed_not);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("TAG", "JSON 解析失败: " + e.getMessage());
                    }
                } else {
                    Log.e("TAG", "获取关注列表失败: " + response.message());
                }
            }
        });
    }
    private void getCommentCount(int noteId) {
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
                detailCommentNum.setText("0"); // 失败时显示 0
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

                            // 更新评论数到UI
                            int commentCount = comments.length();
                            // 在主线程更新UI
                           runOnUiThread(() -> detailCommentNum.setText(String.valueOf(commentCount)));
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
    private void submitComment(int noteId, String comment) {
        // 确保此方法在非UI线程中调用
        OkHttpClient client = new OkHttpClient();

        // 从 SharedPreferences 获取 Token
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
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
                runOnUiThread(() -> {
                    Toast.makeText(PostDetailActivity.this, "评论提交失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(PostDetailActivity.this, "评论提交成功", Toast.LENGTH_SHORT).show();
                        try {
                            assert response.body() != null;
                            Log.e("TAG", "onResponse: " + response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        // 刷新评论或更新UI等
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PostDetailActivity.this, "评论提交失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }


    private void expandAndCollapse() {
        // 检查内容行数
        postContent.post(() -> {
            Log.e("TAG", "initView: " + postContent.getLineCount() );
            if (postContent.getLineCount() >= 10) {
                postContentExpand.setVisibility(View.VISIBLE); // 显示“展开”按钮
            } else {
                postContentExpand.setVisibility(View.GONE); // 隐藏“展开”按钮
            }
        });

        postContentExpand.setOnClickListener(v -> {
            // 展开内容并更新文本
            postContent.setMaxLines(Integer.MAX_VALUE); // 显示所有行
            postContentExpand.setVisibility(View.GONE); // 隐藏展开按钮
            postContentCollapse.setVisibility(View.VISIBLE); // 显示收起按钮


            postContentCollapse.setOnClickListener(view1 -> {
                // 收起内容
                postContent.setMaxLines(10);
                postContentCollapse.setVisibility(View.GONE); // 隐藏收起按钮
                postContentExpand.setVisibility(View.VISIBLE); // 显示展开按钮
            });
        });
    }

    private void followAndWithdraw(String userId){
        // 比较 本地登录的userId 与 发布该帖子的 userId 相同
        if (Objects.equals(userId, getCurrentUserId())) {
            postFollowed.setVisibility(View.GONE); // 隐藏关注按钮
        } else {//否则
            postFollowed.setVisibility(View.VISIBLE); // 显示关注按钮

            // 先调用 getFollowList 判断用户是否已关注
            getFollowList(userId);

            postFollowed.setEnabled(true);
            postFollowed.setText("+关注");
            postFollowed.setOnClickListener(v -> {
                // 在关注按钮上添加点击事件
                if (postFollowed.getText().equals("已关注")) {//显示已关注按钮
                    showUnfollowDialog(userId); // 点击 则弹出是否 取消关注 的提示框
                } else {//显示未关注按钮
                    followUser(userId); //点击 实现添加关注
                }
            });

        }
    }

    private void hideViews() {
        detailFavorite.setVisibility(View.GONE);
        detailFavoriteNum.setVisibility(View.GONE);
        detailCollect.setVisibility(View.GONE);
        detailCollectNum.setVisibility(View.GONE);
        detailCommentIcon.setVisibility(View.GONE);
        detailCommentNum.setVisibility(View.GONE);
    }

    private void showViews() {
        detailFavorite.setVisibility(View.VISIBLE);
        detailFavoriteNum.setVisibility(View.VISIBLE);
        detailCollect.setVisibility(View.VISIBLE);
        detailCollectNum.setVisibility(View.VISIBLE);
        detailCommentIcon.setVisibility(View.VISIBLE);
        detailCommentNum.setVisibility(View.VISIBLE);
    }

    private void adjustEditTextWidth(boolean isExpanded) {
       RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) detailComment.getLayoutParams();
        if (isExpanded) {
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 280, getResources().getDisplayMetrics());
        } else {
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
        }
        detailComment.setLayoutParams(params);
    }

    private String formatTimestamp(long timestamp) {
        // 创建日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 将时间戳转换为Date对象
        Date date = new Date(timestamp);
        // 格式化并返回字符串
        return sdf.format(date);
    }
    //从本地获取用户id
    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", MODE_PRIVATE);
        return sharedPreferences.getString("user_id", null); // -1 表示未找到
    }

    private void updateCollectIcon() {
        if (isCollected) {
            detailCollect.setImageResource(R.drawable.baseline_star_24); // 收藏状态时使用填充星星图标
        } else {
            detailCollect.setImageResource(R.drawable.baseline_star_border_24); // 未收藏状态时使用空星星图标
        }
    }

}