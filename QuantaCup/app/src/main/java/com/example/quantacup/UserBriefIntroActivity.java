package com.example.quantacup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserBriefIntroActivity extends AppCompatActivity {
    private EditText editTextIntro;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_brief_intro);
        toolbar = findViewById(R.id.x_btn);
        Button buttonSave = findViewById(R.id.save_introduction);
        editTextIntro = findViewById(R.id.brief_introduction);


        // 从 SharedPreferences 中加载简介
        loadIntroduction();
        // 获取用户 ID
        SharedPreferences userSharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = userSharedPreferences.getString("user_id", null); // 从本地缓存拿用户ID

        buttonSave.setOnClickListener(v -> {
            String introduction = editTextIntro.getText().toString().trim(); // 获取用户输入的简介

            // 允许简介为空，发送网络请求
            sendIntroductionToServer(userId, introduction);
            saveIntroductionLocally(introduction); // 保存到本地
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void sendIntroductionToServer(String userId, String introduction) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo";

        // 创建请求体，使用 form-data
        RequestBody formBody = new FormBody.Builder()
                .add("id",userId)
                .add("introduction",introduction)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody) // 使用 POST 请求
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SaveIntroduction", "Failed to save introduction: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(UserBriefIntroActivity.this, "请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("SaveIntroduction", "Introduction saved successfully: " + introduction);
                    runOnUiThread(() -> Toast.makeText(UserBriefIntroActivity.this, "简介保存成功", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e("SaveIntroduction", "Failed to save introduction: " + response.code() + " " + response.message());
                    runOnUiThread(() -> Toast.makeText(UserBriefIntroActivity.this, "保存失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void saveIntroductionLocally(String introduction) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserIntroduction", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("introduction", introduction); // 保存简介
        editor.apply(); // 提交更改
    }

    private void loadIntroduction() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserIntroduction", Context.MODE_PRIVATE);
        String savedIntroduction = sharedPreferences.getString("introduction", ""); // 获取保存的简介
        editTextIntro.setText(savedIntroduction); // 设置到 EditText
    }
}