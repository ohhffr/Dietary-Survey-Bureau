package com.example.quantacup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quantacup.adapter.MyAdapter;
import com.example.quantacup.bean.MessageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//我的 ---》 联系客服
public class CommunicateActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String time;//时间（会在每次发送或接收数据时重新赋值）

    private EditText ed_message;
    private List<MessageUtil> list = new ArrayList<MessageUtil>();
    private MyAdapter adapter;
    private ListView list_view;
    private Call currentCall; // 用于存储当前请求的Call对象，以便在取消时使用


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_communicate);

        toolbar = findViewById(R.id.f_btn);

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

        //获取控件
        ed_message = (EditText) findViewById(R.id.ed_message);
        list_view = (ListView) findViewById(R.id.list_view);
        list_view.setDividerHeight(0);//去掉黑线


        //将初始值放入list集合
        MessageUtil util = new MessageUtil();
        util.setJudge(false);
        util.setTime(getTime());
        util.setMessage("您好！我是小灵！");
        list.add(util);

        //定义自定义适配器并赋值
        adapter = new MyAdapter(CommunicateActivity.this, list);

        //将自定义适配器添加到listview
        list_view.setAdapter(adapter);
    }

//获取时间，每次调用都会获得调用时的当前时间。

    public String getTime() {
        Calendar c = Calendar.getInstance();
        time = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH) + "  " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
        Log.e("时间", time);
        return time;
    }

//按钮点击事件

    // 按钮点击事件，提交消息
    public void submit(View view) {
        String message = ed_message.getText().toString();
        String password = "cofood2024"; // 替换为实际的密码

        if (message.isEmpty()) {
            Toast.makeText(CommunicateActivity.this, "输入不能为空！", Toast.LENGTH_SHORT).show();
        } else {
            // 将用户的消息添加到列表中
            MessageUtil util = new MessageUtil();
            util.setJudge(true);
            util.setTime(getTime());
            util.setMessage(message);
            list.add(util);
            adapter.notifyDataSetChanged();
            list_view.setSelection(list.size() - 1);

            // 显示加载对话框
            final ProgressDialog progressDialog = new ProgressDialog(CommunicateActivity.this);
            progressDialog.setMessage("小灵大脑正在飞速运转中...\n\n tip:您可以点任意键取消询问~~");
            progressDialog.setCancelable(true); // 允许用户取消
            progressDialog.setOnCancelListener(dialog -> {
                if (currentCall != null) {
                    currentCall.cancel(); // 取消当前请求
                    Toast.makeText(CommunicateActivity.this, "您已取消询问，请重试！", Toast.LENGTH_SHORT).show();
                }
            });
            progressDialog.show();

            // 发送问题到服务器
            sendQuestionToServer(message, password,progressDialog   );
        }
        ed_message.setText(""); // 清空输入框
    }

    // 发送问题和密码到服务器的方法
    private void sendQuestionToServer(String question, String password, ProgressDialog progressDialog) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)   // 设置连接超时时间
                .readTimeout(100, TimeUnit.SECONDS)      // 设置读取超时时间
                .writeTimeout(100, TimeUnit.SECONDS)     // 设置写入超时时间
                .build();


        // 构建表单数据
        RequestBody formBody = new FormBody.Builder()
                .add("question", question)
                .add("password", password)
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url("https://www.femto.fun/question") // 设置URL
                .post(formBody) // 设置请求方式为POST
                .build();

        // 保存当前的Call对象
        currentCall = client.newCall(request);

        // 异步执行请求
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(CommunicateActivity.this, "请求已取消或请求超时！", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // 请求失败时关闭对话框
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(progressDialog::dismiss); // 请求完成时关闭对话框
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    handleResponse(responseData);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(CommunicateActivity.this, "请求失败，状态码：" + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }



    // 处理服务器的响应
    private void handleResponse(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            String answer = jsonObject.getString("answer");
            runOnUiThread(() -> {
                // 将回答添加到列表并通知适配器更新
                MessageUtil util = new MessageUtil();
                util.setJudge(false); // 表示这是接收到的消息
                util.setTime(getTime());
                util.setMessage(answer);
                list.add(util);
                adapter.notifyDataSetChanged();
                list_view.setSelection(list.size() - 1); // 滚动到最后一项
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(CommunicateActivity.this, "解析失败，请重试。", Toast.LENGTH_SHORT).show();
            });
        }
    }



}