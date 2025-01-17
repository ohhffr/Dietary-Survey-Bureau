package com.example.quantacup;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.auth0.android.jwt.JWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private OkHttpClient client;
    private EditText et_number,et_password;
    private Button btn_register1;

    private Toolbar toolbar;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_number = findViewById(R.id.et_number);
        et_password = findViewById(R.id.et_password);
        Button btn_login = findViewById(R.id.btn_login);
        btn_register1 = findViewById(R.id.btn_register1);
        Button btn_update = findViewById(R.id.forgot_password);
        toolbar = findViewById(R.id.tb_login_btn);

        client = new OkHttpClient();
        initToolbar();
        onCLickListenBtnRegister();



        btn_login.setOnClickListener(view -> {
            String email = et_number.getText().toString().trim();
            String password = et_password.getText().toString().trim();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "邮箱或密码不能为空", Toast.LENGTH_SHORT).show();
            }else {
                try {
                    sendLoginRequest(email,password);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        btn_update.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this,UpdateActivity.class);
            startActivity(intent);
        });

    }

    private void onCLickListenBtnRegister(){
        btn_register1.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 使用 ContextCompat 获取颜色
        int color = ContextCompat.getColor(this, R.color.black);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        toolbar.setNavigationOnClickListener(view -> finish());
    }

    //发送登录请求
    public void sendLoginRequest(String email, String password) throws UnsupportedEncodingException {
        client = new OkHttpClient();
        String encodedEmail = URLEncoder.encode(email, "UTF-8");
        String encodedPassword = URLEncoder.encode(password, "UTF-8");
        String url = "http://8.134.144.65:8082/cofood/account/login?email=" + encodedEmail + "&password=" + encodedPassword;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("package:mine ", "onFailure: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();

                    // 假设响应为 JSON 格式
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String msg = jsonResponse.optString("msg"); // 获取请求信息
                        int code = jsonResponse.optInt("code"); // 获取请求码

                        // 获取 data 对象
                        JSONObject dataObject = jsonResponse.optJSONObject("data"); // 获取请求数据
                        String token = dataObject != null ? dataObject.optString("token") : null; // 获取后端返回的token
                        String userId = null; // 定义用户id

                        if (token != null) {
                            JWT jwt = new JWT(token); // JWT（JSON Web Token）解析token
                            userId = Objects.requireNonNull(jwt.getAudience()).get(0); // 将解析出来的集合取下标为0的即为用户id
                        } else {
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "用户不存在！", Toast.LENGTH_SHORT).show());
                        }

                        if ("登录成功".equals(msg) && code == 200 && token != null) {
                            storeId(userId); // 将用户id存储到本地缓存
                            storeToken(token); // 将token存储到本地缓存
                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 跳转到主界面
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            Log.e("package:mine ", "onResponse: " + msg);
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录失败：" + msg + "邮箱或密码错误，请重试", Toast.LENGTH_SHORT).show());
                        }
                    } catch (JSONException e) {
                        Log.e("package:mine ", "onResponse: JSON解析失败 - " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "请求失败：响应数据格式不正确", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("package:mine ", "onResponse: " + response.message());
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "请求失败：" + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void storeToken(String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token); // 保存token
        editor.apply(); // 异步保存
    }


    private void storeId(String id) {
        //sp存储用户id，存放在Device Explorer->data->data->本项目->shared-prefs->你以下设置的name.xml
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", id);
        editor.apply(); // 异步保存
    }



}
