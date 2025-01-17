package com.example.quantacup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateActivity extends AppCompatActivity {

    private EditText updateEmail,newPassword,newPassword2,updateVerify;
    private OkHttpClient client;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        updateEmail = findViewById(R.id.upd_number);
        newPassword = findViewById(R.id.new_password);
        newPassword2 = findViewById(R.id.new_password2);
        updateVerify = findViewById(R.id.upd_captcha);

        Button sendVerify = findViewById(R.id.update_btn_captcha);
        Button submit = findViewById(R.id.btn_submit);

        sendVerify.setOnClickListener(this::onClick);
        submit.setOnClickListener(this::onClick);

        client = new OkHttpClient(); // 初始化 OkHttpClient

        Toolbar toolbar = findViewById(R.id.tb_update_btn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setTitle("修改密码");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void onClick(View v) {
        try {
            String UpdEmail = updateEmail.getText().toString().trim();
            String UpdPassword = newPassword.getText().toString().trim();
            String UpdPassword2= newPassword2.getText().toString().trim();
            String UpdVerify =updateVerify.getText().toString().trim();

            if (v.getId() == R.id.btn_submit) {
                if (TextUtils.isEmpty(UpdEmail) || TextUtils.isEmpty(UpdPassword)
                        || TextUtils.isEmpty(UpdPassword2) || TextUtils.isEmpty(UpdVerify)) {
                    Toast.makeText(UpdateActivity.this, "各项不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (TextUtils.equals(UpdPassword, UpdPassword2)) {
                        updatePassword(UpdEmail,UpdPassword,UpdVerify);
                    } else {
                        Toast.makeText(UpdateActivity.this, "两次密码不一样，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (v.getId() == R.id.update_btn_captcha) {
                sendCaptchaToUpdatePassword();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // 打印异常信息
            Toast.makeText(UpdateActivity.this, "编码错误，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCaptchaToUpdatePassword() throws UnsupportedEncodingException {
        String to = updateEmail.getText().toString();
        String subject = "CoFood修改密码 邮箱验证码";
        String content = "您的邮箱修改验证码是：";

        if (TextUtils.isEmpty(to)) {
            Toast.makeText(this, "请填写邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证邮箱格式
        if (!Patterns.EMAIL_ADDRESS.matcher(to).matches()) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构造请求 URL
        String url = "http://8.134.144.65:8082/cofood/sendCode?to=" + to
                + "&subject=" + URLEncoder.encode(subject, "UTF-8")
                + "&content=" + URLEncoder.encode(content, "UTF-8");

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(UpdateActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 读取返回的 JSON 数据
                    String jsonResponse = response.body() != null ? response.body().string() : "";
                    try {
                        // 解析 JSON 数据
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String token = jsonObject.optString("token");
                        Log.e("TAG", "onResponse发送验证码: " + token );
                        // 将 token 存储到 SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserToken", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.apply();

                        // 显示成功提示
                        runOnUiThread(() -> Toast.makeText(UpdateActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show());
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(UpdateActivity.this, "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(UpdateActivity.this, "请求失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updatePassword(String email, String password, String verify) {
        final String url = "http://8.134.144.65:8082/cofood/account/changePwd"; // 修正的 URL

        // 从 SharedPreferences 中读取 token
        SharedPreferences sharedPreferences = getSharedPreferences("UserToken", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 如果没有找到，返回 null

        Log.e("TAG", "registerUser: "+ token );
        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("newPassword", password)
                .add("verify", verify)
                .build();

        // 创建请求，并添加 Authorization 头
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        if (token != null) {
            requestBuilder.addHeader("token",  token); // 将 token 添加到请求头
        }

        Request request = requestBuilder.build();

        Log.e("TAG", "registerUser: " + email + " " + password + " " + verify);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(UpdateActivity.this, "请求失败："
                        + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (!response.isSuccessful()) {
                    Log.e("TAG", "onResponse失败: " + response);
                    throw new IOException("Unexpected code " + response);
                }
                runOnUiThread(() -> Toast.makeText(UpdateActivity.this,  "修改密码成功！", Toast.LENGTH_SHORT).show());
                Intent intent = new Intent(UpdateActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


}