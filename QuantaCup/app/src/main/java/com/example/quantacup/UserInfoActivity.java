package com.example.quantacup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.quantacup.help.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {
    private ScheduledExecutorService scheduler;
//    private static final long REFRESH_INTERVAL = 5; // 每5秒刷新一次

    private static final int PICK_IMAGE = 1;

    private ImageView infoAvatar;
    private TextView infoName,infoEmail,infoAge,infoGender,infoBirth,infoPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        Toolbar toolbar = findViewById(R.id.tb_return_btn);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setTitle("个人主页");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        infoName = findViewById(R.id.user_info_name);
        infoEmail = findViewById(R.id.user_info_email);
        infoAvatar = findViewById(R.id.user_info_avatar);
        infoAge = findViewById(R.id.user_info_age);
        infoGender = findViewById(R.id.user_info_gender);
        infoBirth = findViewById(R.id.user_info_birth);
        infoPhone = findViewById(R.id.user_info_phone);


        RelativeLayout rlAvatar = findViewById(R.id.home_head_btn);
        rlAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();//打开相册
            }
        });



        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);//从本地缓存拿用户id
        if (userId != null){
            fetchUserInfo(userId);
            fetchUserOtherInfo(userId);
        }

        RelativeLayout rlName = findViewById(R.id.home_name_btn);
        rlName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUsernameDialog(userId);
            }
        });

        RelativeLayout rlAge = findViewById(R.id.home_age_btn);
        rlAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserAgeDialog(userId);
            }
        });

        RelativeLayout rlGender = findViewById(R.id.home_sex_btn);
        rlGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showGenderDialog();
            }
        });

        RelativeLayout rlBriefIntro = findViewById(R.id.home_summary_btn);
        rlBriefIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserInfoActivity.this,UserBriefIntroActivity.class);
                startActivity(intent);
            }
        });

        RelativeLayout rlBirth = findViewById(R.id.home_born_btn);
        rlBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(); // 显示日期选择对话框
            }
        });

        RelativeLayout rlPhone = findViewById(R.id.home_phone_btn);
        rlPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserPhoneDialog(userId);
            }
        });

    }
    /****************************************显示用户信息****************************************/
    //1、加载用户信息
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
                    Toast.makeText(UserInfoActivity.this, "当前无法连接网络，请检查网络设置是否正常！", Toast.LENGTH_SHORT).show();
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
                        long birth = user.getLong("birth");
                        String birthDate = formatTimestamp(birth);
                        String phone = user.getString("phone");

                        SharedPreferences preferences = getSharedPreferences("UserEmail", MODE_PRIVATE);//存用户的邮箱方便实现注销账号
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("user_email",email);
                        editor.apply();

                        runOnUiThread(() -> {
                            infoName.setText(username);
                            infoEmail.setText(email);
                            infoPhone.setText(phone);
                            infoBirth.setText(birthDate);
                            loadImageFromUrl("https://yuhi-oss.oss-cn-shenzhen.aliyuncs.com/" + avatar ); // 加载头像
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(UserInfoActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(UserInfoActivity.this, "获取用户信息失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    //2、将图片加载到界面上
    private void loadImageFromUrl(String url) {
        Log.e("TAG", "loadImageFromUrl: "+ url );
        // 使用Picasso或Glide等库加载图片到ImageView
        Glide.with(UserInfoActivity.this)
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
                .into(infoAvatar);


    }

    private void fetchUserOtherInfo(String userId) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBodyInfo?userId=" + userId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UserInfoActivity.this, "当前无法连接网络，请检查网络设置是否正常！", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    assert response.body() != null;
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONObject dataObject = jsonObject.getJSONObject("data");

                        // 修改这里从 JSONObject 获取数据
                        JSONObject userInfo = dataObject.getJSONObject("list");

                        // 直接使用 userInfo 进行比较，不需要再获取 userId
                        if (userInfo.getInt("userId") == Integer.parseInt(userId)) {
                            int age = userInfo.getInt("age");
                            int gender = userInfo.optInt("gender", -1); // 使用 optInt 以防止 null
                            String genderStr = (gender == 0) ? "女" : (gender == 1) ? "男" : "未知";
                            runOnUiThread(() -> {
                                infoAge.setText(String.valueOf(age));
                                infoGender.setText(genderStr);
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(UserInfoActivity.this, "解析数据失败！", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        });
    }


    /****************************************修改图片的逻辑****************************************/
    // 1、打开相册的实现
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }
    // 2、处理返回的图片
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadImage(selectedImageUri);
            }
        }
    }

    //3、使用OkHttp将图片和用户ID上传到后端
    private void uploadImage(Uri imageUri) {
        String userId = getSharedPreferences("UserId", Context.MODE_PRIVATE).getString("user_id", null);
        if (userId != null) {
            File file = new File(getRealPathFromURI(imageUri));

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("image/jpeg")))
                    .addFormDataPart("userId", userId)
                    .build();

            Request request = new Request.Builder()
                    .url("http://8.134.144.65:8082/cofood/uploadAvatar")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("TAG", "onResponse上传响应失败: " + e.getMessage() );
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "上传成功", Toast.LENGTH_SHORT).show());
                        Log.e("package:mine ", "onResponse: " + response + "\n" + request + "\n" + requestBody );
                    } else {
                        Log.e("TAG", "onResponse上传响应失败: "+ response.message() );
                        runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "上传失败: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        }
    }

    // 4、获取真实路径
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /****************************************修改其他用户信息的逻辑****************************************/
    private void showUsernameDialog(String userId) {
        // 创建一个EditText用于输入新的用户名
        final EditText input = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改用户名")
                .setMessage("请输入新的用户名:")
                .setView(input)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newUsername = input.getText().toString();
                        updateUsername(userId, newUsername);  // 将userId传入
                    }
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();
    }

    private void updateUsername(String userId, String username) {

        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo";

        // 构建form-data请求体，包括userId和username
        RequestBody formBody = new FormBody.Builder()
                .add("id", userId)  // 添加userId
                .add("username", username)
                .build();

        // 创建请求
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(formBody);
        Request request = builder.build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "更新失败，请检查网络", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "用户名更新成功", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "更新失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showUserAgeDialog(String userId) {
        // 创建一个EditText用于输入新的用户名
        final EditText input = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改年龄信息")
                .setMessage("请输入年龄:")
                .setView(input)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newAge = input.getText().toString();
                        updateUserAge(userId, newAge);  // 将userId传入
                    }
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();
    }

    private void updateUserAge(String userId, String age) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBodyInfo";

        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("age", age)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "更新失败，请检查网络", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "年龄信息更新成功", Toast.LENGTH_SHORT).show());
                    Log.e("TAG", "onResponse: " + response.body().string());
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "无详细信息";
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "更新失败: " + errorBody, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showGenderDialog() {
        // 创建一个单选按钮组
        final RadioGroup radioGroup = new RadioGroup(this);
        RadioButton rbMale = new RadioButton(this);
        rbMale.setText("男");
        RadioButton rbFemale = new RadioButton(this);
        rbFemale.setText("女");
        radioGroup.addView(rbMale);
        radioGroup.addView(rbFemale);

        // 创建对话框
        new AlertDialog.Builder(this)
                .setTitle("选择性别")
                .setView(radioGroup)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedGender = -1; // -1表示未选择
                        if (radioGroup.getCheckedRadioButtonId() == rbMale.getId()) {
                            selectedGender = 1; // 男
                        } else if (radioGroup.getCheckedRadioButtonId() == rbFemale.getId()) {
                            selectedGender = 0; // 女
                        }

                        if (selectedGender != -1) {
                            // 获取用户ID并发送请求
                            updateUserGender(selectedGender);
                        } else {
                            Toast.makeText(UserInfoActivity.this, "请选择性别", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateUserGender(int gender) {

        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null); // 从本地缓存拿用户ID

        // 创建 POST 请求的 URL
        String url = "http://8.134.144.65:8082/cofood/account/UserBodyInfo";
        assert userId != null;
        RequestBody formBody = new FormBody.Builder()
                .add("userId",userId)
                .add("gender",String.valueOf(gender))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(UserInfoActivity.this, "请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(UserInfoActivity.this, "性别更新成功", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(UserInfoActivity.this, "性别更新失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showDatePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_date_picker, null);
        builder.setView(dialogView);

        Spinner yearSpinner = dialogView.findViewById(R.id.year_spinner);
        Spinner monthSpinner = dialogView.findViewById(R.id.month_spinner);
        Spinner daySpinner = dialogView.findViewById(R.id.day_spinner);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);

        // 设置年份下拉框
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[currentYear - 1900 + 1];
        for (int i = 0; i <= currentYear - 1900; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // 设置月份下拉框
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = String.valueOf(i + 1);
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // 设置天数
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDaySpinner(daySpinner, Integer.parseInt(yearSpinner.getSelectedItem().toString()), position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 确认按钮点击事件
        confirmButton.setOnClickListener(v -> {
            int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
            int selectedMonth = Integer.parseInt(monthSpinner.getSelectedItem().toString());
            int selectedDay = Integer.parseInt(daySpinner.getSelectedItem().toString());

            @SuppressLint("DefaultLocale") String birthDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay);
            sendBirthDateToServer(birthDate);
            Toast.makeText(UserInfoActivity.this, "选择的出生日期: " + birthDate, Toast.LENGTH_SHORT).show();
        });

        builder.create().show();
    }

    private void updateDaySpinner(Spinner daySpinner, int year, int month) {
        int daysInMonth;
        if (month == 2) {
            // 判断是否为闰年
            daysInMonth = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
        } else {
            daysInMonth = (month == 4 || month == 6 || month == 9 || month == 11) ? 30 : 31;
        }

        String[] days = new String[daysInMonth];
        for (int i = 0; i < daysInMonth; i++) {
            days[i] = String.valueOf(i + 1);
        }

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
    }

    private void sendBirthDateToServer(String birthDate) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null); // 从本地缓存获取用户ID
        Log.e("TAG", "sendBirthDateToServer: " + userId + " " + birthDate );
        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo";

        assert userId != null;
        RequestBody formBody = new FormBody.Builder()
                .add("id",userId)
                .add("birth",birthDate)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody) // 使用 POST 请求
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "出生日期保存成功", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "保存失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String formatTimestamp(long timestamp) {
        // 创建日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // 将时间戳转换为Date对象
        Date date = new Date(timestamp);
        // 格式化并返回字符串
        return sdf.format(date);
    }


    private void showUserPhoneDialog(String userId) {
        // 创建一个EditText用于输入新的用户名
        final EditText input = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改手机号码")
                .setMessage("请输入新的手机号码:")
                .setView(input)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newUserPhone = input.getText().toString();
                        updateUserPhone(userId, newUserPhone);  // 将userId传入
                    }
                })
                .setNegativeButton("取消", null)
                .create();

        dialog.show();
    }

    private void updateUserPhone(String userId, String phone) {

        OkHttpClient client = new OkHttpClient();
        String url = "http://8.134.144.65:8082/cofood/account/UserBasicInfo";

        // 构建form-data请求体，包括userId和username
        RequestBody formBody = new FormBody.Builder()
                .add("id", userId)  // 添加userId
                .add("phone", phone)
                .build();

        // 创建请求
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(formBody);
        Request request = builder.build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "更新失败，请检查网络", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "手机号码更新成功", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "更新失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}



