package com.example.quantacup;


import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.quantacup.help.CircleCrop;
import com.example.quantacup.myself.CouponActivity;
import com.example.quantacup.myself.OrderActivity;
import com.example.quantacup.myself.ShoppingActivity;
import com.example.quantacup.myself.CollectionActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MyselfFragment extends Fragment implements View.OnClickListener {
    private static final long REFRESH_INTERVAL = 5000; // 每5秒刷新
    private Handler handler = new Handler();
    private Runnable refreshRunnable;
    RelativeLayout communicate_btn, usehelp_btn, about_btn, set_btn, privacy_btn, update_btn;  //预算中心,使用帮助,关于我们,设置
    Button btn_coupon,btn_star,btn_shopping,btn_order;
    ImageView btn_myself;
    private TextView userName,loginStatue,myInfo;
    private OkHttpClient client = new OkHttpClient();

    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myself, container, false);

        // 初始化控件
        communicate_btn = view.findViewById(R.id.communicate_btn);
        usehelp_btn = view.findViewById(R.id.usehelp_btn);
        about_btn = view.findViewById(R.id.about_btn);
        set_btn = view.findViewById(R.id.set_btn);
        privacy_btn = view.findViewById(R.id.privacy_btn);
        update_btn = view.findViewById(R.id.update_btn);
        btn_coupon = view.findViewById(R.id.btn_coupon);
        btn_order = view.findViewById(R.id.btn_order);
        btn_star = view.findViewById(R.id.btn_star);
        btn_shopping = view.findViewById(R.id.btn_shopping);
        btn_myself = view.findViewById(R.id.btn_myself);

        // 设置点击事件
        communicate_btn.setOnClickListener(this);
        usehelp_btn.setOnClickListener(this);
        about_btn.setOnClickListener(this);
        set_btn.setOnClickListener(this);
        privacy_btn.setOnClickListener(this);
        update_btn.setOnClickListener(this);
        btn_coupon.setOnClickListener(this);
        btn_star.setOnClickListener(this);
        btn_shopping.setOnClickListener(this);
        btn_order.setOnClickListener(this);
        btn_myself.setOnClickListener(this);

        userName = view.findViewById(R.id.user_name);
        loginStatue = view.findViewById(R.id.login_status);
        myInfo = view.findViewById(R.id.my_info);
        myInfo.setOnClickListener(this);

        // 从 SharedPreferences 中获取用户 ID
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);
        Log.e("TAG", "onCreateView: " + userId);

        // 检查 userId 是否为空，设置未登录状态
        if (userId != null) {
            fetchUserName(userId);
        } else {
            // 用户未登录，更新 UI 显示未登录状态
            userName.setText("未登录");
            loginStatue.setText("请登录");
            myInfo.setVisibility(View.GONE); // 隐藏用户信息部分
        }

        // 初始化定时刷新任务
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (userId != null) {
                    fetchUserName(userId);
                } else {
                    // 如果用户未登录，也可以考虑不执行 fetchUserName
                    Log.e("TAG", "用户未登录，无法刷新用户名");
                }
                // 重新安排下次执行
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };

        // 开始定时刷新任务
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 启动定时刷新
        handler.post(refreshRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        // 停止定时刷新
        handler.removeCallbacks(refreshRunnable);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.communicate_btn) {//预算中心
            Intent intent2 = new Intent(getContext(), CommunicateActivity.class);
            startActivity(intent2);
        } else if (id == R.id.usehelp_btn) {//使用帮助
            Intent intent3 = new Intent(getContext(), UsehelpActivity.class);
            startActivity(intent3);
        } else if (id == R.id.about_btn) {//关于我们
            Intent intent4 = new Intent(getContext(), AboutActivity.class);
            startActivity(intent4);
        } else if (id == R.id.set_btn) {//设置
            Intent intent5 = new Intent(getContext(), SetActivity.class);
            startActivity(intent5);
        } else if (id == R.id.privacy_btn) {//隐私
            Intent intent6 = new Intent(getContext(), PrivacyActivity.class);
            startActivity(intent6);
        } else if (id == R.id.update_btn) {//更新
            Intent intent7 = new Intent(getContext(), UpdateActivity.class);
            startActivity(intent7);
        } else if (id == R.id.btn_coupon){
            Intent intent8 = new Intent(getContext(), CouponActivity.class);
            startActivity(intent8);
        }
        else if (id == R.id.btn_star){
            Intent intent8 = new Intent(getContext(), CollectionActivity.class);
            startActivity(intent8);
        }
        else if (id == R.id.btn_shopping){
            Intent intent8 = new Intent(getContext(), ShoppingActivity.class);
            startActivity(intent8);
        }
        else if (id == R.id.btn_order){
            Intent intent8 = new Intent(getContext(), OrderActivity.class);
            startActivity(intent8);
        }
        else if (id == R.id.btn_myself){
            Intent intent9 = new Intent(getContext(), LoginActivity.class);
            startActivity(intent9);
        }else if (id == R.id.my_info){
           Intent intent10 = new Intent(getContext(), UserInfoActivity.class);
           startActivity(intent10);
        }
    }

    private void fetchUserName(String userId) {
        client = new OkHttpClient();
        userId = userId.replaceAll("[\\[\\] ]", "");


        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo?id=" + userId;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       requireActivity().runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(getActivity(), "无互联网连接，请检查网络设置", Toast.LENGTH_SHORT).show();
                           }
                       });
                    }
                });
                // 可以考虑在这里显示用户友好的息
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONObject dataObject = jsonResponse.optJSONObject("data");
                        if (dataObject != null) {
                            JSONObject userObject = dataObject.optJSONObject("user");
                            if (userObject != null) {
                                String username = userObject.optString("username");
                                String avatar = userObject.optString("avatar");
                                requireActivity().runOnUiThread(() -> userName.setText(username));
                                requireActivity().runOnUiThread(() -> loginStatue.setText(""));
                                requireActivity().runOnUiThread(() -> myInfo.setVisibility(View.VISIBLE));
                                requireActivity().runOnUiThread(()-> loadImageFromUrl("https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatar));
                            } else {
                                Log.e("MyselfFragment", "用户对象为空");
                            }
                        } else {
                            Log.e("MyselfFragment", "数据对象为空");
                        }
                    } catch (JSONException e) {
                        Log.e("MyselfFragment", "JSON解析错误", e);
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "没有响应体";
                    Log.e("MyselfFragment", "请求失败: " + response.code() + ", 错误: " + errorBody);
                }
            }
        });
    }

    private void loadImageFromUrl(String url) {
        Log.e("TAG", "loadImageFromUrl: "+ url );
        // 使用Picasso或Glide等库加载图片到ImageView
        Glide.with(requireContext())
                .load(url)
                .transform(new CircleCrop())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.pic_head)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        Log.e("TAG", "onLoadFailed: " + e.getMessage() );
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(btn_myself);
    }
}

    //签到功能的实现
//    public void setSignup(){
//
//
//
//        int count = Integer.parseInt(clock_day.getText().toString())+1;
//
//        editor = sp.edit();
//        editor.putString("tice",Integer.toString(count));
//        editor.commit(); //写入
//        onResume();//刷新
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        SharedPreferences sp =getActivity().getSharedPreferences("tice",Context.MODE_PRIVATE);
//        clock_day.setText(sp.getString("tice","0"));
//    }
//}