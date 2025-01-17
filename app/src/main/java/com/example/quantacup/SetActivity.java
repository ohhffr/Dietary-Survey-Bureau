package com.example.quantacup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.quantacup.help.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SetActivity extends AppCompatActivity {

    private boolean isLogoutEnabled = true; // 控制注销按钮是否可用
    Button tb_btn_change;
    Button tb_btn_exit;
    Button tb_btn_logout;
    private Toolbar toolbar;

    private ImageView userAvatar;
    private TextView userName,userAccount;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        toolbar = findViewById(R.id.tb_return_btn);

        tb_btn_change = findViewById(R.id.tb_btn_change);
        tb_btn_exit = findViewById(R.id.tb_btn_exit);
        tb_btn_logout = findViewById(R.id.tb_btn_logout);
        onCLickListenBtnLogin();
        onClickListenBtnExit();
        onClickListenBtnLogout();

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
        userAvatar = findViewById(R.id.set_user_avatar);
        userName = findViewById(R.id.set_user_name);
        userAccount = findViewById(R.id.set_user_account);
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);//从本地缓存拿用户id
        if (userId != null){
            fetchUserInfo(userId);
        }
    }


    private void onCLickListenBtnLogin(){
        tb_btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onClickListenBtnExit( ){
        tb_btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 清除所有 SharedPreferences 数据
                SharedPreferences sharedPreferences = getSharedPreferences("UserId", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // 清除所有用户ID相关数据
                editor.apply(); // 保存更改

                SharedPreferences sharedPreferences1 = getSharedPreferences("CaloriePrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                editor1.clear(); // 清除所有卡路里相关数据
                editor1.apply(); // 保存更改

                SharedPreferences sharedPreferences2 = getSharedPreferences("DatePrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                editor2.clear(); // 清除所有日期相关数据
                editor2.apply(); // 保存更改

                SharedPreferences sharedPreferences3 = getSharedPreferences("MealData", MODE_PRIVATE);
                SharedPreferences.Editor editor3 = sharedPreferences3.edit();
                editor3.clear(); // 清除所有饮食搭配相关数据
                editor3.apply(); // 保存更改

                SharedPreferences sharedPreferences4 = getSharedPreferences("NoteId", MODE_PRIVATE);
                SharedPreferences.Editor editor4 = sharedPreferences4.edit();
                editor4.clear(); // 清除所有笔记Id相关数据
                editor4.apply(); // 保存更改

                SharedPreferences sharedPreferences5 = getSharedPreferences("Timestamp", MODE_PRIVATE);
                SharedPreferences.Editor editor5 = sharedPreferences5.edit();
                editor5.clear(); // 清除所有今日搭配时间相关数据
                editor5.apply(); // 保存更改

                SharedPreferences sharedPreferences6 = getSharedPreferences("UserEmail", MODE_PRIVATE);
                SharedPreferences.Editor editor6 = sharedPreferences6.edit();
                editor6.clear(); // 清除所有今日搭配时间相关数据
                editor6.apply(); // 保存更改

                SharedPreferences sharedPreferences7 = getSharedPreferences("UserIntroduction", MODE_PRIVATE);
                SharedPreferences.Editor editor7 = sharedPreferences7.edit();
                editor7.clear(); // 清除所有今日搭配时间相关数据
                editor7.apply(); // 保存更改

                SharedPreferences sharedPreferences8 = getSharedPreferences("UserToken", MODE_PRIVATE);
                SharedPreferences.Editor editor8 = sharedPreferences8.edit();
                editor8.clear(); // 清除所有今日搭配时间相关数据
                editor8.apply(); // 保存更改


                Intent intent = new Intent(SetActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // 结束 SetActivity
            }
        });

    }

    private void onClickListenBtnLogout() {
        tb_btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示弹出警告对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
                builder.setTitle("警告")
                        .setMessage("一旦注销，则您的一切数据将全部消失！请慎重考虑")
                        .setCancelable(true)
                        .setNegativeButton("取消", null) // 取消按钮

                        .setPositiveButton("确定", null); // 确定按钮，稍后设置点击事件

                AlertDialog dialog = builder.create();
                dialog.show();

                // 获取确定按钮并修改样式
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(Color.RED);
                positiveButton.setEnabled(false); // 禁用按钮，3秒后再启用

                // 设置延迟
                new Handler().postDelayed(() -> positiveButton.setEnabled(true), 3000); // 3秒后启用

                // 确定按钮点击事件
                positiveButton.setOnClickListener(v -> {
                    // TODO: 在这里执行注销逻辑
                    String email = getUserEmail(); // 获取用户的电子邮件
                    logoutUser(email); // 注销用户
                    dialog.dismiss(); // 关闭对话框
                });
            }
        });
    }
    // 获取用户的电子邮件
    private String getUserEmail() {
        // 假设从 SharedPreferences 中获取用户的 email
        SharedPreferences preferences = getSharedPreferences("your_app_prefs", Context.MODE_PRIVATE);
        return preferences.getString("user_email", null); // 默认返回 null
    }

    private void fetchUserInfo(String userId) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo?id=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SetActivity.this, "当前无法连接网络，请检查网络设置是否正常！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONObject user = data.getJSONObject("user");

                        String username = user.getString("username");
                        String email = user.getString("email");
                        String avatar = user.getString("avatar");

                        runOnUiThread(() -> {
                            userName.setText(username);
                            userAccount.setText(email);
                            loadImageFromUrl("https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatar ); // 加载头像
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(SetActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(SetActivity.this, "获取用户信息失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loadImageFromUrl(String url) {
        Log.e("TAG", "loadImageFromUrl: "+ url );
        // 使用Picasso或Glide等库加载图片到ImageView
        Glide.with(SetActivity.this)
                .load(url)
                .transform(new CircleCrop())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.pic_head)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        assert e != null;
                        Log.e("TAG", "onLoadFailed: " + e.getMessage() );
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(userAvatar);

    }
    // 注销用户的方法
    private void logoutUser(String email) {
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo";

        // 创建 DELETE 请求
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .delete() // 设置为 DELETE 请求
                .addHeader("email", email) // 传递用户的 email
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // 请求失败的处理
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "注销失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 注销成功的处理
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "注销成功", Toast.LENGTH_SHORT).show();
                        // 这里可以处理注销后的逻辑，比如返回到登录界面
                    });
                } else {
                    // 请求失败的处理
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "注销失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}