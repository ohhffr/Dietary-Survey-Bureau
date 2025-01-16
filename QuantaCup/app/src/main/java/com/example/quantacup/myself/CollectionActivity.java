package com.example.quantacup.myself;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.quantacup.R;
import com.example.quantacup.adapter.AllPostsAdapter;
import com.example.quantacup.adapter.MyCollectPostsAdapter;
import com.example.quantacup.bean.Post;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CollectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private MyCollectPostsAdapter myCollectPostsAdapter;
    private List<Post> postList;
    private OkHttpClient client;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        initView();
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);//从本地缓存拿用户id
        if (userId != null){
            fetchFavoritePosts(userId);
        }

    }
    private void initView() {
        recyclerView = findViewById(R.id.collection_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        myCollectPostsAdapter = new MyCollectPostsAdapter(this,postList);
        recyclerView.setAdapter(myCollectPostsAdapter);

        toolbar = findViewById(R.id.star_btn);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchFavoritePosts(String userId) {
        client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/community/favouriteByUserId?userId=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "获取收藏帖子失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray favouriteList = jsonResponse.getJSONObject("data").getJSONArray("favouriteList");
                        // 遍历收藏的帖子
                        for (int i = 0; i < favouriteList.length(); i++) {
                            JSONObject favourite = favouriteList.getJSONObject(i);
                            int noteId = favourite.getInt("noteId");
                            fetchPostDetails(noteId); // 根据 noteId 获取帖子详情
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "解析收藏列表失败", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "获取收藏列表失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void fetchPostDetails(int noteId) {
        String url = "http://8.134.144.65:8082/cofood/community/NoteByNoteId?noteId=" + noteId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "获取帖子详情失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject note = jsonResponse.getJSONObject("data").getJSONObject("note");
                        String userId = note.getString("userId");
                        String title = note.getString("title");
                        String content = note.getString("content");
                        long createdAt = note.getLong("createdAt");
                        String timeStamp = formatTimestamp(createdAt);

                        // 处理图片URL
                        JSONArray imagesArray = note.getJSONArray("images");
                        List<String> imageUrls = new ArrayList<>();
                        for (int j = 0; j < imagesArray.length(); j++) {
                            JSONObject image = imagesArray.getJSONObject(j);
                            String imageUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + image.getString("url");
                            imageUrls.add(imageUrl);
                        }
                        // 获取用户信息
                        fetchUserInfo(userId, noteId, title, content, timeStamp, imageUrls );
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "解析帖子详情失败", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "获取帖子详情失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void fetchUserInfo(String userId, int noteId,String title, String content, String createdAt,List<String> imageUrls) {
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo?id=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONObject user = jsonResponse.getJSONObject("data").getJSONObject("user");
                        String username = user.getString("username");
                        String avatar = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + user.getString("avatar");

                        Post post = new Post(userId,noteId,avatar, username, createdAt, title + "\n" + content, imageUrls);
                        addPostToList(post);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "解析用户信息失败", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(CollectionActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addPostToList(Post post) {
        new Handler(Looper.getMainLooper()).post(() -> {
            postList.add(post);
            myCollectPostsAdapter.notifyDataSetChanged();
        });
    }


    private String formatTimestamp(long timestamp) {
        // 创建日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 将时间戳转换为Date对象
        Date date = new Date(timestamp);
        // 格式化并返回字符串
        return sdf.format(date);
    }


}