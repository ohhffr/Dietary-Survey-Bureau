package com.example.quantacup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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


public class MyPostsFragment extends Fragment {
    private MyPostsAdapter myPostsAdapter;
    private List<Post> postList;
    private OkHttpClient client;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String API_URL_PERSON_NOTES = "http://8.134.144.65:8082/cofood/community/NoteByUserId";
    private static final String API_URL_FOLLOW_NOTES = "http://8.134.144.65:8082/cofood/community/followNote";
    private static final String API_URL_USER = "http://8.134.144.65:8082/cofood/account/UserBasicInfo";


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public MyPostsFragment() {

    }

    public static MyPostsFragment newInstance(String param1, String param2) {
        MyPostsFragment fragment = new MyPostsFragment();
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
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);//从本地缓存拿用户id


        View view = inflater.inflate(R.layout.fragment_my_posts_fragemnt, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.my_posts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();


        myPostsAdapter = new MyPostsAdapter(getContext(), postList);
        recyclerView.setAdapter(myPostsAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_my);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchPersonUserNotes(userId); // 获取用户笔记
            // 获取关注用户的帖子
            fetchFollowedUserNotes(); // 添加这一行
        });
        LoadInitPersonUserNotes(userId);
        LoadInitFollowedUserNotes();
        return view;

    }

    private void LoadInitFollowedUserNotes() {
        client = new OkHttpClient();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserLoginToken",Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 如果没有找到，返回 null

        Request.Builder requestBuilder = new Request.Builder()
                .url(API_URL_FOLLOW_NOTES)
                .get();
        if (token != null){
            requestBuilder.addHeader("token", token); // 将 token 添加到请求头
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "请求失败！" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseFollowedUserNotesResponse(responseData);
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "请求失败！" + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void LoadInitPersonUserNotes(String userId) {
        client = new OkHttpClient();
        String url = API_URL_PERSON_NOTES + "?userId=" + userId;
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
                    parsePersonNotesResponse(responseData);
                } else {
                    requireActivity().runOnUiThread(()->Toast.makeText(getContext(), "请求失败！" + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchFollowedUserNotes() {
        // 显示进度条
        swipeRefreshLayout.setRefreshing(true);
        client = new OkHttpClient();

        // 清空现有的帖子列表以避免重复
        postList.clear();
        myPostsAdapter.notifyDataSetChanged(); // 通知适配器数据已改变
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserLoginToken",Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 如果没有找到，返回 null

        Request.Builder requestBuilder = new Request.Builder()
                .url(API_URL_FOLLOW_NOTES)
                .get();
        if (token != null){
            requestBuilder.addHeader("token", token); // 将 token 添加到请求头
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "请求失败！" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // 隐藏进度条
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseFollowedUserNotesResponse(responseData);
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

    private void parseFollowedUserNotesResponse(String responseData) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);

            // 检查响应码是否为200
            if (jsonResponse.getInt("code") == 200) {
                // 获取笔记列表
                JSONArray notes = jsonResponse.getJSONObject("data").getJSONArray("noteList");

                for (int i = 0; i < notes.length(); i++) {
                    JSONObject note = notes.getJSONObject(i);

                    // 提取笔记信息
                    int noteId = note.getInt("noteId");
                    String userId = note.getString("userId");
                    String title = note.getString("title");
                    String content = note.getString("content");
                    long createdAt = note.getLong("createdAt");
                    String postTime = formatTimestamp(createdAt); // 格式化时间戳

                    // 获取图片URLs
                    List<String> imageUrls = new ArrayList<>();
                    JSONArray images = note.getJSONArray("images");
                    for (int j = 0; j < images.length(); j++) {
                        // 假设images数组中的每个对象都有一个"url"字段
                        String imageUrl = images.getJSONObject(j).getString("url");
                        imageUrls.add("https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + imageUrl); // 添加完整的URL
                    }

                    // 通过用户ID获取用户信息
                    fetchFollowedUserInfo(userId,noteId,title, content, postTime, imageUrls);
                }
            } else {
                // 处理响应中的错误
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "未找到笔记！", Toast.LENGTH_SHORT).show());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "解析JSON失败！", Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchFollowedUserInfo(String userId, int noteId,String title, String content, String postTime, List<String> imageUrls) {
        client = new OkHttpClient();
        String url = API_URL_USER + "?id=" + userId;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(()-> Toast.makeText(getContext(), "获取用户信息失败！" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseFollowedUserResponse(userId,noteId,responseData, title, content, postTime, imageUrls);
                } else {
                    requireActivity().runOnUiThread(()->{
                        Toast.makeText(getContext(), "获取用户信息失败！" + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void parseFollowedUserResponse(String userId,int noteId,String responseData, String title,
                                           String content, String postTime, List<String> imageUrls) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);

            if (jsonResponse.getInt("code") == 200) {
                JSONObject user = jsonResponse.getJSONObject("data").getJSONObject("user");
                String username = user.getString("username");
                String avatarUrl = user.getString("avatar");
                String userAvatarUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatarUrl;
                Log.e("TAG", "parseUserResponse: " + userAvatarUrl );

                Post post = new Post(userId,noteId,userAvatarUrl, username, postTime, title + "\n" + content, imageUrls);
                addPostToList(post);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(()->{
                Toast.makeText(getContext(), "获取用户信息失败！" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchPersonUserNotes(String userId) {
        // 显示进度条
        swipeRefreshLayout.setRefreshing(true);
        client = new OkHttpClient();

        // 清空现有的帖子列表以避免重复
        postList.clear();
        myPostsAdapter.notifyDataSetChanged(); // 通知适配器数据已改变
        String url = API_URL_PERSON_NOTES + "?userId=" + userId;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(()-> {
                    Toast.makeText(getContext(), "请求失败！" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // 隐藏进度条
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parsePersonNotesResponse(responseData);
                    // 隐藏进度条
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    requireActivity().runOnUiThread(()->{
                        Toast.makeText(getContext(), "请求失败！" + response.message(), Toast.LENGTH_SHORT).show();
                        // 隐藏进度条
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }
        });
    }
    private void parsePersonNotesResponse(String responseData) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);

            if (jsonResponse.getInt("code") == 200) {
                JSONArray notes = jsonResponse.getJSONObject("data").getJSONArray("notes");

                // 获取 SharedPreferences 实例
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("NoteId", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                for (int i = 0; i < notes.length(); i++) {
                    JSONObject note = notes.getJSONObject(i);
                    int noteId = note.getInt("noteId"); // 获取 noteId
                    String userId = note.getString("userId");
                    String title = note.getString("title");
                    String content = note.getString("content");
                    long createdAt = note.getLong("createdAt");
                    String postTime = formatTimestamp(createdAt); // 格式化时间戳

                    List<String> imageUrls = new ArrayList<>();
                    JSONArray images = note.getJSONArray("images");
                    for (int j = 0; j < images.length(); j++) {
                        String imageUrl = images.getJSONObject(j).getString("url");
                        imageUrls.add("https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + imageUrl);

                    }
                    // 保存 noteId 到 SharedPreferences
                    editor.putInt("noteId_", noteId); // 使用 noteId 作为键
                    editor.apply(); // 提交更改


                    // 获取个人用户的头像、用户名信息
                    fetchPersonUserInfo(userId,noteId, title, content, postTime, imageUrls);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "解析JSON失败！", Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchPersonUserInfo(String userId,int noteId, String title, String content, String postTime, List<String> imageUrls) {
        client = new OkHttpClient();
        String url = API_URL_USER + "?id=" + userId;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(()->{
                    Toast.makeText(getContext(), "获取用户信息失败！" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parsePersonUserResponse(userId,noteId,responseData, title, content, postTime, imageUrls);
                } else {
                    requireActivity().runOnUiThread(()->{
                        Toast.makeText(getContext(), "获取用户信息失败！" + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void parsePersonUserResponse(String userId,int noteId, String responseData, String title,
                                         String content, String postTime, List<String> imageUrls) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);

            if (jsonResponse.getInt("code") == 200) {
                JSONObject user = jsonResponse.getJSONObject("data").getJSONObject("user");
                String username = user.getString("username");
                String avatarUrl = user.getString("avatar");
                String userAvatarUrl = "https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatarUrl;
                Log.e("TAG", "parseUserResponse: " + userAvatarUrl );

                Post post = new Post(userId,noteId,userAvatarUrl, username, postTime, title + "\n" + content, imageUrls);
                addPostToList(post);
            }
        } catch (JSONException e) {
            e.printStackTrace();
           requireActivity().runOnUiThread(()->{
               Toast.makeText(getContext(), "获取用户信息失败！" + e.getMessage(), Toast.LENGTH_SHORT).show();
           });

        }
    }


    private String formatTimestamp(long timestamp) {
        // 创建日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 将时间戳转换为Date对象
        Date date = new Date(timestamp);
        // 格式化并返回字符串
        return sdf.format(date);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addPostToList(Post post) {
        new Handler(Looper.getMainLooper()).post(() -> {
            postList.add(post);
            myPostsAdapter.notifyDataSetChanged();
        });
    }
}