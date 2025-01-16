package com.example.quantacup;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    EditText number;
    EditText edt_password;
    EditText password2;
    EditText captcha;
    Button btn_register, btn_captcha, btn_skip;
    private Toolbar toolbar;
    private OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edt_password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        number = findViewById(R.id.number);
        captcha = findViewById(R.id.captcha);
        btn_register = findViewById(R.id.btn_register);
        btn_captcha = findViewById(R.id.btn_captcha);
        btn_skip = findViewById(R.id.skip_to_login);
        toolbar = findViewById(R.id.tb_login_btn);

        client = new OkHttpClient(); // 初始化 OkHttpClient

        initToolbar();

        // 设置按钮的点击事件
        btn_register.setOnClickListener(this::onClick);
        btn_captcha.setOnClickListener(this::onClick);
        btn_skip.setOnClickListener(this::onClick);
    }

    public void onClick(View v) {
        try {
            String password = edt_password.getText().toString();
            String password2string = password2.getText().toString();
            String email = number.getText().toString();
            String verify = captcha.getText().toString();

            if (v.getId() == R.id.btn_register) {
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                        || TextUtils.isEmpty(password2string) || TextUtils.isEmpty(verify)) {
                    Toast.makeText(RegisterActivity.this, "各项不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (TextUtils.equals(password, password2string)) {
                       registerUser(email,password,verify);
                    } else {
                        Toast.makeText(RegisterActivity.this, "两次密码不一样，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (v.getId() == R.id.btn_captcha) {
                sendCaptchaRequest();
            } else if (v.getId() == R.id.skip_to_login) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // 打印异常信息
            Toast.makeText(RegisterActivity.this, "编码错误，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendCaptchaRequest() throws UnsupportedEncodingException {
        String to = number.getText().toString();
        String subject = "CoFood注册邮箱验证码";
        String content = "欢迎使用CoFood！您的邮箱注册验证码是：";

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
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "验证码发送成功", Toast.LENGTH_SHORT).show());
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "请求失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void registerUser(String email, String password, String verify) {
        final String url = "http://8.134.144.65:8082/cofood/account/register"; // 修正的 URL

        // 从 SharedPreferences 中读取 token
        SharedPreferences sharedPreferences = getSharedPreferences("UserToken", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 如果没有找到，返回 null

        RequestBody body = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "请求失败："
                        + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("TAG", "onResponse失败: " + response);
                        throw new IOException("Unexpected code " + response);
                    }
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this,  "注册成功！"
                                , Toast.LENGTH_SHORT).show());
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
            }
        });
    }

    private void initToolbar() {
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

}
