package com.example.quantacup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//健康 ---》 我的状态
public class ConditionActivity extends AppCompatActivity {
    private TextView heightTextView,weightTextView,bloodPressureTextView,bloodSugarTextView,basalMetabolismTextView
            ,heartRateTextView,stepCountTextView,waterIntakeTextView;
    private ImageView conditionAvatar;
    private TextView conditionName;
    private Toolbar toolbar;

    private Button button;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_condition);
        button = findViewById(R.id.update_body_data);
        conditionAvatar = findViewById(R.id.condition_avatar);
        conditionName = findViewById(R.id.condition_name);
        heightTextView = findViewById(R.id.height);
        weightTextView = findViewById(R.id.weight);
        bloodPressureTextView = findViewById(R.id.bloodPressure);
        bloodSugarTextView = findViewById(R.id.bloodSugar);
        basalMetabolismTextView = findViewById(R.id.basalMetabolism);
        heartRateTextView = findViewById(R.id.heartRate);
        stepCountTextView = findViewById(R.id.stepCount);
        waterIntakeTextView = findViewById(R.id.waterIntake);

        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);//从本地缓存拿用户id
        if (userId != null){
            fetchUserInfo(userId);
            fetchUserBodyInfo(userId);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateBodyDataDialog();
            }
        });

        toolbar = findViewById(R.id.star_btn);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
                    Toast.makeText(ConditionActivity.this, "当前无法连接网络，请检查网络设置是否正常！", Toast.LENGTH_SHORT).show();
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
                            conditionName.setText(username);
                            loadImageFromUrl("https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatar ); // 加载头像
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(ConditionActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ConditionActivity.this, "获取用户信息失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

    }

    private void fetchUserBodyInfo(String userId) {
        String url = "http://8.134.144.65:8082/cofood/account/UserBodyInfo?userId=" + userId;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("UserBodyInfoActivity", "Request failed: " + e.getMessage());
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONObject list = data.getJSONObject("list");

                        // 解析身体数据
                        String height = list.getString("height");
                        String weight = list.getString("weight");
                        String bloodPressure = list.getString("bloodPressure");
                        String bloodSugar = list.getString("bloodSugar");
                        String basalMetabolism = list.getString("basalMetabolism");
                        String heartRate = list.getString("heartRate");
                        String stepCount = list.getString("stepCount");
                        String waterIntake = list.getString("waterIntake");

                        // 更新UI
                        runOnUiThread(() -> {
                            heightTextView.setText(height + "cm");
                            weightTextView.setText(weight + "kg");
                            bloodPressureTextView.setText( bloodPressure + "mmHg");
                            bloodSugarTextView.setText(bloodSugar + "mg/dL");
                            basalMetabolismTextView.setText(basalMetabolism + "千卡");
                            heartRateTextView.setText( heartRate + "bpm");
                            stepCountTextView.setText( stepCount + "步");
                            waterIntakeTextView.setText( waterIntake + "ml");
                        });
                    } catch (JSONException e) {
                        Log.e("UserBodyInfoActivity", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    Log.e("UserBodyInfoActivity", "Response not successful: " + response.message());
                }
            }
        });
    }

    private void loadImageFromUrl(String url) {
        // 使用Picasso或Glide等库加载图片到ImageView
        Glide.with(ConditionActivity.this)
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
                .into(conditionAvatar);

    }
    private void showUpdateBodyDataDialog() {
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(ConditionActivity.this);
        builder.setTitle("请输入身体信息");

        // 创建布局
        LinearLayout layout = new LinearLayout(ConditionActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // 创建8个EditText
        EditText heightInput = new EditText(ConditionActivity.this);
        heightInput.setHint("请输入身高 (cm)");
        layout.addView(heightInput);

        EditText weightInput = new EditText(ConditionActivity.this);
        weightInput.setHint("请输入体重 (kg)");
        layout.addView(weightInput);

        EditText bloodPressureInput = new EditText(ConditionActivity.this);
        bloodPressureInput.setHint("请输入血压 (mmHg)");
        layout.addView(bloodPressureInput);

        EditText bloodSugarInput = new EditText(ConditionActivity.this);
        bloodSugarInput.setHint("请输入血糖 (mg/dL)");
        layout.addView(bloodSugarInput);

        EditText basalMetabolismInput = new EditText(ConditionActivity.this);
        basalMetabolismInput.setHint("请输入基础代谢 (千卡)");
        layout.addView(basalMetabolismInput);

        EditText heartRateInput = new EditText(ConditionActivity.this);
        heartRateInput.setHint("请输入心率 (bpm)");
        layout.addView(heartRateInput);

        EditText stepCountInput = new EditText(ConditionActivity.this);
        stepCountInput.setHint("请输入步数");
        layout.addView(stepCountInput);

        EditText waterIntakeInput = new EditText(ConditionActivity.this);
        waterIntakeInput.setHint("请输入水摄入量 (ml)");
        layout.addView(waterIntakeInput);

        builder.setView(layout);

        // 加载已保存的数据
        SharedPreferences sharedPreferences = getSharedPreferences("BodyData", MODE_PRIVATE);
        heightInput.setText(sharedPreferences.getString("height", ""));
        weightInput.setText(sharedPreferences.getString("weight", ""));
        bloodPressureInput.setText(sharedPreferences.getString("bloodPressure", ""));
        bloodSugarInput.setText(sharedPreferences.getString("bloodSugar", ""));
        basalMetabolismInput.setText(sharedPreferences.getString("basalMetabolism", ""));
        heartRateInput.setText(sharedPreferences.getString("heartRate", ""));
        stepCountInput.setText(sharedPreferences.getString("stepCount", ""));
        waterIntakeInput.setText(sharedPreferences.getString("waterIntake", ""));

        // 添加确认按钮
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 获取输入框的值
            String height = heightInput.getText().toString();
            String weight = weightInput.getText().toString();
            String bloodPressure = bloodPressureInput.getText().toString();
            String bloodSugar = bloodSugarInput.getText().toString();
            String basalMetabolism = basalMetabolismInput.getText().toString();
            String heartRate = heartRateInput.getText().toString();
            String stepCount = stepCountInput.getText().toString();
            String waterIntake = waterIntakeInput.getText().toString();

            // 存储数据到 SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("height", height);
            editor.putString("weight", weight);
            editor.putString("bloodPressure", bloodPressure);
            editor.putString("bloodSugar", bloodSugar);
            editor.putString("basalMetabolism", basalMetabolism);
            editor.putString("heartRate", heartRate);
            editor.putString("stepCount", stepCount);
            editor.putString("waterIntake", waterIntake);
            editor.apply(); // 保存更改

            // 调用方法提交数据
            submitBodyInfo(height, weight, bloodPressure, bloodSugar, basalMetabolism, heartRate, stepCount, waterIntake);
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        // 显示对话框
        builder.show();
    }
    // 提交数据的方法
    private void submitBodyInfo(String height, String weight, String bloodPressure, String bloodSugar,
                                String basalMetabolism, String heartRate, String stepCount, String waterIntake) {
        OkHttpClient client = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null); // 从本地缓存拿用户ID

        // 创建 POST 请求的 URL
        String url = "http://8.134.144.65:8082/cofood/account/UserBodyInfo";
        assert userId != null;
        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("height", height)
                .add("weight", weight)
                .add("bloodPressure", bloodPressure)
                .add("bloodSugar", bloodSugar)
                .add("basalMetabolism", basalMetabolism)
                .add("heartRate", heartRate)
                .add("stepCount", stepCount)
                .add("waterIntake", waterIntake)
                .build();


        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ConditionActivity.this, "提交失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ConditionActivity.this, "提交成功", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ConditionActivity.this, "提交失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


}