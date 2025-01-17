package com.example.quantacup;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.quantacup.adapter.AllPostsAdapter;
import com.example.quantacup.adapter.MyPostsAdapter;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CommunityPostsFragment extends Fragment {
    private AllPostsAdapter allPostsAdapter;
    private List<Post> postList;
    private OkHttpClient client;

    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CommunityPostsFragment() {

    }

    public static CommunityPostsFragment newInstance(String param1, String param2) {
        CommunityPostsFragment fragment = new CommunityPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_community_posts,container,false);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        RecyclerView recyclerView = view.findViewById(R.id.community_posts_recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        allPostsAdapter = new AllPostsAdapter(getContext(),postList);
        recyclerView.setAdapter(allPostsAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_all);// 设置下拉刷新监听器

        // 下拉刷新获取全部用户笔记
        swipeRefreshLayout.setOnRefreshListener(this::fetchAllUserNotes);

        LoadInitAllUserNotes(); // 初始加载全部的用户帖子

        return view;
    }

    private void LoadInitAllUserNotes() {
        client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/community/Note";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(()-> Toast.makeText(getContext(), "请求失败！" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseAllNotesResponse(responseData);
                } else {
                    requireActivity().runOnUiThread(()->Toast.makeText(getContext(), "请求失败！" + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAllUserNotes() {
        // 显示进度条
        swipeRefreshLayout.setRefreshing(true);

        // 清空现有的帖子列表以避免重复
        postList.clear();
        allPostsAdapter.notifyDataSetChanged(); // 通知适配器数据已改变

        client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/community/Note";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                  Toast.makeText(getContext(), "请求失败！" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseAllNotesResponse(responseData);
                    // 隐藏进度条
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "请求失败！" + response.message(), Toast.LENGTH_SHORT).show();
                        // 隐藏进度条
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }

            }
        });
    }


    private void parseAllNotesResponse(String responseData) {
        try {
            // 解析整个响应
            JSONObject responseObject = new JSONObject(responseData);
            // 获取 noteList 数组
            JSONObject dataObject = responseObject.getJSONObject("data");
            JSONArray noteList = dataObject.getJSONArray("noteList");

            // 遍历 noteList 数组
            for (int i = 0; i < noteList.length(); i++) {
                JSONObject note = noteList.getJSONObject(i);

                // 提取 noteId, userId, title, content, createdAt
                int noteId = note.getInt("noteId");
                String userId = note.getString("userId");
                String title = note.getString("title");
                String content = note.getString("content");
                long createdAt = note.getLong("createdAt");
                String postTime = formatTimestamp(createdAt); // 格式化时间戳

                // 提取 images 数组并拼接 URL
                JSONArray imagesArray = note.getJSONArray("images");
                List<String> imageUrls = new ArrayList<>();
                for (int j = 0; j < imagesArray.length(); j++) {
                    JSONObject imageObject = imagesArray.getJSONObject(j);
                    String imageUrl = imageObject.getString("url");
                    // 拼接完整的图片 URL
                    String fullImageUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + imageUrl;
                    imageUrls.add(fullImageUrl);
                }
                    fetchAllUserInfo(userId, noteId, title, content, postTime, imageUrls);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchAllUserInfo(String userId,int noteId,String title, String content, String postTime, List<String> imageUrls) {
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

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseAllUserResponse(userId,noteId,responseData, title, content, postTime, imageUrls);
                }
            }
        });
    }

    private void parseAllUserResponse(String userId,int noteId,String responseData,
                                      String title, String content, String postTime, List<String> imageUrls) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);

            if (jsonResponse.getInt("code") == 200) {
                JSONObject user = jsonResponse.getJSONObject("data").getJSONObject("user");
                String username = user.getString("username");
                String avatarUrl = user.getString("avatar");
                String userAvatarUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatarUrl;

                Post post = new Post(userId,noteId,userAvatarUrl, username, postTime, title + "\n" + content, imageUrls);
                addPostToList(post);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addPostToList(Post post) {
        new Handler(Looper.getMainLooper()).post(() -> {
            postList.add(post);
            allPostsAdapter.notifyDataSetChanged();
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




