package com.example.quantacup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.quantacup.help.CircleProgressView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//健康
public class HealthFragment extends Fragment implements View.OnClickListener {
    private TextView textView1,textView2;
    RelativeLayout data_btn,condition_btn,clock_btn;  //预算中心,使用帮助,关于我们,设置
    LinearLayout search_view;
    private TextView tv_height,tv_weight;
    private CircleProgressView circleProgressView;//圆形进度（这里用来表示已吃一日目标总量的百分之几）
    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        textView1 = view.findViewById(R.id.had_eaten);
        textView2 = view.findViewById(R.id.had_reminder);
        circleProgressView = view.findViewById(R.id.eat_proportions);
        data_btn = view.findViewById(R.id.data_btn);
        condition_btn = view.findViewById(R.id.condition_btn);
        clock_btn = view.findViewById(R.id.clock_btn);
        search_view = view.findViewById(R.id.search_view);
        tv_height = view.findViewById(R.id.height);
        tv_weight = view.findViewById(R.id.weight);

        data_btn.setOnClickListener(this);
        condition_btn.setOnClickListener(this);
        clock_btn.setOnClickListener(this);
        search_view.setOnClickListener(this);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);

        if (userId != null) {
            fetchUserBodyData(userId);
            fetchCaloriesData(userId);
        } else {
            Log.e("package:mine ", "User ID not found in SharedPreferences");
        }

        return view;

    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.data_btn) {//食卡数据
            Intent intent1 = new Intent(getContext(), DataActivity.class);
            startActivity(intent1);
        } else if (id == R.id.condition_btn) {//我的状态
            Intent intent2 = new Intent(getContext(), ConditionActivity.class);
            startActivity(intent2);
        } else if (id == R.id.clock_btn) {//定时提醒
            Intent intent3 = new Intent(getContext(), ClockActivity.class);
            startActivity(intent3);
        } else if (id == R.id.search_view) {//
            Intent intent4 = new Intent(getContext(), SearchActivity.class);
            startActivity(intent4);
        }
    }

    private void fetchUserBodyData(String userId) {
        userId = userId.replaceAll("[\\[\\] ]", "");
        String url = "http://8.134.144.65:8082/cofood/account/UserBodyInfo?userId=" + userId;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "无互联网连接，请检查网络设置", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseUserBodyData(responseData);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseUserBodyData(String jsonData) {
        // 解析 JSON 数据以获取 height 和 weight
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonData);
            org.json.JSONObject dataObject = jsonObject.getJSONObject("data");
            org.json.JSONObject listObject = dataObject.getJSONObject("list");


            int height = listObject.getInt("height");
            int weight = listObject.getInt("weight");
            tv_height.setText(height + "cm");
            tv_weight.setText(weight + "kg");

        } catch (Exception e) {
           e.printStackTrace();
        }
    }


    // 发起 GET 请求，获得每天第一次上传的食物推荐的卡路里
    private void fetchCaloriesData(String userId) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/DailySignById?userId=" + userId; // URL 及 userId

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("DataActivity", "请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    parseCaloriesData(responseData);
                } else {
                    Log.e("DataActivity", "请求失败，响应代码: " + response.code());
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void parseCaloriesData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONArray signList = dataObject.getJSONArray("signList");

            if (signList.length() > 0) {
                // 获取最后一个 sign 的 targetCalories
                JSONObject lastSign = signList.getJSONObject(signList.length() - 1);
                int getTargetCalories = lastSign.getInt("targetCalories");
                int remainCalories = lastSign.getInt("remainCalories");
                // 更新 UI 在主线程中
                requireActivity().runOnUiThread(() -> {
                    textView1.setText("已吃" + (getTargetCalories - remainCalories) +" 大卡");
                    textView2.setText("剩余" + remainCalories + " 大卡");
                    circleProgressView.setProgress(Math.round((float) (getTargetCalories-remainCalories) * 100 / getTargetCalories));
                });
            }
        } catch (Exception e) {
            Log.e("DataActivity", "解析数据失败: " + e.getMessage());
        }
    }

}