package com.example.quantacup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quantacup.adapter.SuggestionAdapter;
import com.example.quantacup.bean.Food;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
//健康 ---》 食卡数据
public class DataActivity extends AppCompatActivity {
    private TextView hadEaten,reminder;
    private boolean isMealRecorded = false; // 标记是否已记录饮食数据
    private Button button;
    private PieChart pieChart;
    private LineChart lineChart;


    // 食物类型与具体食物的映射

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_data);
            hadEaten = findViewById(R.id.eaten);
            reminder = findViewById(R.id.remainder);
            lineChart = findViewById(R.id.line_chart);


            button = findViewById(R.id.btn_takeCard);
            button.setOnClickListener(view -> {
                if (!isMealRecorded) { // 检查是否已记录饮食数据
                    showRecordMealDialog(); // 弹出记录饮食数据对话框
                }
            });

            CalendarView calendarView = findViewById(R.id.calendar);
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth)->{
                String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                if (!isMealRecorded) { // 检查是否已记录饮食数据
                    showMealSelectionDialog(selectedDate); // 弹出记录饮食数据对话框
                }
            });

            setPieChart();//设置环状图

            setData();// 添加数据

            SharedPreferences prefs = getSharedPreferences("MealData", Context.MODE_PRIVATE);
            int targetCalories = prefs.getInt("dayTotalCalories",0);
            if (targetCalories != 0) {
                // 上传 targetCalories 到服务器
                 uploadTargetCalories(targetCalories);
            }
            setCenterTextOnlyCalories("今日推荐\n" + targetCalories + "大卡");
            SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
            String userId = sharedPreferences.getString("user_id", null);//从本地缓存拿用户id
            if (userId != null){
                fetchCaloriesData(userId);
            }

            hideLabels();// 隐藏扇形位置标签和底部标签
            setLineChartData(userId);
            Toolbar toolbar = findViewById(R.id.star_btn);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setTitle("食卡数据");
            toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void setLineChartData(String userId) {
        // 创建日期标签
        final ArrayList<Entry> calorieEntries = new ArrayList<>();
        final ArrayList<Entry> proteinEntries = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();

        // 获取当前日期
        Calendar calendar = Calendar.getInstance();

        // 设定今天的日期
        @SuppressLint("SimpleDateFormat") String today = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        xLabels.add(today); // 添加今天

        // 获取过去六天的日期
        for (int i = 1; i <= 6; i++) {
            Calendar pastDate = (Calendar) calendar.clone();
            pastDate.add(Calendar.DAY_OF_MONTH, -i);
            @SuppressLint("SimpleDateFormat") String pastDay = new SimpleDateFormat("yyyy-MM-dd").format(pastDate.getTime());
            xLabels.add(pastDay); // 添加过去的日期
        }

        // 初始化 Entries
        for (int i = 0; i < 7; i++) {
            calorieEntries.add(new Entry(i, 0));  // 设置所有天的热量数据为0
            proteinEntries.add(new Entry(i, 0));  // 设置所有天的蛋白质量数据为0
        }

        // 发起 GET 请求
        String url = "http://8.134.144.65:8082/cofood/DailySignById?userId=" + userId;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace(); // 处理请求失败情况
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray signList = jsonObject.getJSONObject("data").getJSONArray("signList");

                        // 更新 Entries 数据
                        for (int i = 0; i < signList.length(); i++) {
                            JSONObject sign = signList.getJSONObject(i);
                            long signDate = sign.getLong("date");
                            int index = -1;

                            // 找到对应的日期索引
                            for (int j = 0; j < xLabels.size(); j++) {
                                // 将日期格式化为 "yyyy-MM-dd" 进行比较
                                Calendar entryDate = Calendar.getInstance();
                                entryDate.setTimeInMillis(signDate);
                                @SuppressLint("SimpleDateFormat") String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(entryDate.getTime());
                                if (formattedDate.equals(xLabels.get(j))) {
                                    index = j; // 找到对应的索引
                                    break;
                                }
                            }

                            // 更新热量和蛋白质量数据
                            if (index != -1) {
                                int targetCalories = sign.getInt("targetCalories");
                                int remainCalories = sign.getInt("remainCalories");
                                int isSigned = sign.getInt("isSigned");

                                if (isSigned == 1) {
                                    calorieEntries.set(index, new Entry(index, targetCalories - remainCalories));
                                } else {
                                    calorieEntries.set(index, new Entry(index, 0));
                                }

                                // 这里可以添加蛋白质量数据的更新逻辑，假设蛋白质量在 sign 中也有
                                // proteinEntries.set(index, new Entry(index, proteinIntake));  // 更新蛋白质量
                            }
                        }

                        // 在 UI 线程更新图表
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateChart(calorieEntries, proteinEntries, xLabels);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace(); // 处理 JSON 解析异常
                    }
                }
            }
        });
    }

    private void updateChart(ArrayList<Entry> calorieEntries, ArrayList<Entry> proteinEntries, ArrayList<String> xLabels) {
        // 创建热量折线数据集
        LineDataSet calorieDataSet = new LineDataSet(calorieEntries, "摄入热量");
        calorieDataSet.setColor(getResources().getColor(android.R.color.holo_green_dark));
        calorieDataSet.setValueTextColor(getResources().getColor(android.R.color.black));

        // 创建蛋白质量折线数据集
        LineDataSet proteinDataSet = new LineDataSet(proteinEntries, "摄入蛋白质量");
        proteinDataSet.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        proteinDataSet.setValueTextColor(getResources().getColor(android.R.color.black));

        // 创建 LineData 对象
        LineData lineData = new LineData(calorieDataSet, proteinDataSet);
        lineChart.setData(lineData);

        // 设置 X 轴标签
        XAxis xAxis = lineChart.getXAxis();

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return xLabels.get((int) value); // 返回完整的七天标签
            }
        });

        // 设置横坐标标签倾斜
        xAxis.setLabelRotationAngle(-20f); // 将标签倾斜30度
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X轴标签在折线图下面
        // 增加底部边距
        lineChart.setExtraOffsets(0, 0, 0, 40); // 增加底部边距

        // 设置描述
        Description description = new Description();
        description.setText("一周摄入记录");
        lineChart.setDescription(description);

        // 设置图例
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP); // 设置图例在上方
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // 设置图例居中

        // 设置双Y轴
        lineChart.getAxisLeft().setAxisMaximum(2000);
        lineChart.getAxisLeft().setAxisMinimum(0);
        lineChart.getAxisLeft().setGranularity(200);

        lineChart.getAxisRight().setAxisMaximum(200);
        lineChart.getAxisRight().setAxisMinimum(0);
        lineChart.getAxisRight().setGranularity(20);

        // 刷新图表
        lineChart.invalidate();
    }







    /*******************************************初始设置环状图（由饼状图设置空洞构成）*****************************************************/
    //1、设置饼（环）状图
    private void setPieChart(){
        pieChart = findViewById(R.id.calories_pie_chart);
        pieChart.setNoDataText("暂无数据");
        // 设置 PieChart 的特性
        pieChart.setDrawHoleEnabled(true); // 允许空洞
        pieChart.setHoleColor(Color.WHITE); // 设置空洞的颜色
        pieChart.setTransparentCircleColor(Color.WHITE); // 透明圆的颜色
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(90f);//设置空洞半径为90%
        pieChart.setTransparentCircleRadius(90f);//设置透明圆半径为90%
    }
    //2、初始化饼（环）状图信息
    private void setData() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // 增加灰色和绿色数据
        entries.add(new PieEntry(0, "")); // 灰色
        entries.add(new PieEntry(100, "")); // 绿色

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.GRAY, Color.GREEN); // 设置颜色

        dataSet.setValueLinePart1Length(0f); // 隐藏扇形位置标签的连接线
        dataSet.setValueLinePart2Length(0f); // 同上
        dataSet.setValueLineWidth(0f); // 隐藏扇形位置标签的连接线宽度
        dataSet.setValueTextSize(0f); // 不显示数据值


        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // 刷新图表


    }
    //3、初始空洞内文本（即环状图才有空洞，环内文本）
    private void setCenterTextOnlyCalories(String text) {
        pieChart.setCenterText(text);
        pieChart.setCenterTextSize(16f); // 设置中心文本的大小
        pieChart.setCenterTextColor(Color.BLACK); // 设置中心文本的颜色
    }
    //4、隐藏用不到的标签
    private void hideLabels(){
        pieChart.getDescription().setEnabled(false); // 隐藏底部的描述（如果有的话）
        pieChart.getLegend().setEnabled(false); // 隐藏图例（底部标签）
    }

    /*******************************************上传饮食数据*******************************************************/

    //1、点击日历某日，即可弹出编辑框选择早中晚餐
    private void showMealSelectionDialog(String selectedDate) {

        final String[] mealOptions = {"早餐", "午餐", "晚餐"};
        final int[] checkedMeal = {-1}; // 使用数组记录选中的餐次

        AlertDialog.Builder mealDialogBuilder = new AlertDialog.Builder(this);//初始化弹窗
        mealDialogBuilder.setTitle("选择餐次")
                .setSingleChoiceItems(mealOptions, checkedMeal[0], (dialog, which) -> {
                    checkedMeal[0] = which; // 更新选中的餐次
                })
                .setPositiveButton("确定", (dialog, id) -> {
                    if (checkedMeal[0] != -1) {
                        // 获取选中的餐次
                        String selectedMeal = mealOptions[checkedMeal[0]];
                        showFoodInputDialog(selectedDate, selectedMeal); // 显示食物输入对话框
                    } else {
                        Toast.makeText(DataActivity.this, "请选择餐次", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", (dialog, id) -> dialog.dismiss());

        AlertDialog mealDialog = mealDialogBuilder.create();
        mealDialog.show();
    }
    //2、显示食物输入对话框
    private void showFoodInputDialog(String selectedDate, String selectedMeal) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_food_input, null); // 输入框布局

        EditText foodInput = dialogView.findViewById(R.id.foodInput); // 输入食物名称
        EditText amountInput = dialogView.findViewById(R.id.amountInput); // 输入食物摄入量
        ListView suggestionList = dialogView.findViewById(R.id.suggestionList); // 食物建议列表

        SuggestionAdapter adapter = new SuggestionAdapter(this, new ArrayList<>());
        suggestionList.setAdapter(adapter);

        // 标志变量，指示是否点击了建议项
        final boolean[] isSuggestionClicked = {false};

        // 编辑框文本变化事件
        foodInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSuggestionClicked[0]) {
                    isSuggestionClicked[0] = false; // 重置标志，准备下一次输入
                    return; // 如果选择了建议项，不进行模糊搜索
                }

                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    fetchFoodSuggestions(query, adapter, suggestionList); // 模糊搜索功能
                } else {
                    suggestionList.setVisibility(View.GONE); // foodInput编辑框为空，则继续隐藏搜索显示栏
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        suggestionList.setOnItemClickListener((parent, view, position, id) -> {
            Food selectedFood = adapter.getItem(position); // 获取选中的 Food 对象
            if (selectedFood != null) {
                foodInput.setText(selectedFood.getFoodName()); // 显示食物名称在输入框里
                suggestionList.setVisibility(View.GONE); // 隐藏建议列表
                isSuggestionClicked[0] = true; // 标记为选择了建议项
            }
        });


        AlertDialog.Builder inputDialogBuilder = new AlertDialog.Builder(this);
        inputDialogBuilder.setTitle("输入摄入食物信息")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, id) -> {
                    String foodName = foodInput.getText().toString().trim();
                    String amount = amountInput.getText().toString().trim();

                    if (!foodName.isEmpty() && !amount.isEmpty()) {
                        // 获取用户输入的摄入量并计算卡路里
                        double amountValue = Integer.parseInt(amount); // 摄入量
                        Food selectedFood = adapter.getFoodByName(foodName); // 根据食物名称获取Food对象
                        if (selectedFood != null) {
                            int totalCalories = (int) ((amountValue / 100) * selectedFood.getCalories()); // 计算总卡路里

                            // 更新当天的总卡路里并进行网络请求
                            UpdateCalories(totalCalories);

                            // 弹出提示框告知用户
                            new AlertDialog.Builder(this)
                                    .setTitle("摄入卡路里")
                                    .setMessage("您摄入的 " + selectedFood.getFoodName() + " 的总热量是: " + totalCalories + " 大卡。")
                                    .setPositiveButton("确定", null)
                                    .show();

                            // 上传食物数据
                            uploadFoodData(selectedDate, selectedMeal, foodName, amount);

                        } else {
                            Toast.makeText(DataActivity.this, "未找到食物信息", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DataActivity.this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", (dialog, id) -> dialog.dismiss());

        AlertDialog foodInputDialog = inputDialogBuilder.create();
        foodInputDialog.show();
    }
    //3、实现模糊搜索
    private void fetchFoodSuggestions(String query, SuggestionAdapter adapter, ListView suggestionList) {
        String url = "http://8.134.144.65:8082/cofood/FoodBaseByNameDim?foodName=" + query;

        // 使用 OkHttp 或 Volley 进行网络请求
        // 这里以 OkHttp 为例
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    parseJson(responseData, adapter, suggestionList);//将后端返回的内容进行解析
                }
            }
        });
    }
    //4、解析后端返回的内容，根据需要拿到客户端来
    private void parseJson(String json, SuggestionAdapter adapter, ListView suggestionList) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray foodBases = jsonObject.getJSONObject("data").getJSONArray("foodBases");

            // 将 suggestions 列表改为存储 Food 对象
            List<Food> suggestions = new ArrayList<>();
            for (int i = 0; i < foodBases.length(); i++) {
                String foodName = foodBases.getJSONObject(i).getString("foodName");
                int calories = foodBases.getJSONObject(i).getInt("calories");
                // 创建 Food 对象并添加到建议列表
                suggestions.add(new Food(foodName, calories));
            }

            runOnUiThread(() -> {
                adapter.clear();
                adapter.addAll(suggestions); // 添加 Food 对象
                adapter.notifyDataSetChanged();
                suggestionList.setVisibility(suggestions.isEmpty() ? View.GONE : View.VISIBLE); // 显示或隐藏建议列表
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //5、上传食物数据
    private void uploadFoodData(String selectedDate, String selectedMeal, String foodName, String amount) {
        //0表示早餐，1表示午餐，2表示晚餐
        int type = -1; // 默认为无效类型
        switch (selectedMeal) {
            case "早餐":
                type = 0;
                break;
            case "午餐":
                type = 1;
                break;
            case "晚餐":
                type = 2;
                break;
            default:
                Log.e("TAG", "Invalid meal type: " + selectedMeal);
                return; // 处理无效餐次
        }

        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

        // 从本地缓存获取用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null); // 从本地缓存拿用户id

        // 检查 userId 是否为空
        if (userId == null) {
            Log.e("TAG", "User ID is null");
            return; // 如果 userId 为空，则返回
        }

        // 创建请求体，使用表单编码
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("userId", userId)
                .add("type", String.valueOf(type))
                .add("content", foodName);

        // 创建请求
        Request request = new Request.Builder()
                .url("http://8.134.144.65:8082/cofood/account/UserMealInfo")
                .post(formBuilder.build()) // 使用 POST 方法
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                // 处理请求失败
                e.printStackTrace();
                Log.e("TAG", "Request failedFail: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(DataActivity.this, "请求失败", Toast.LENGTH_SHORT).show());
            }

            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 处理成功的响应
                    assert response.body() != null;
                    String responseData = response.body().string();
                    Log.e("TAG", "Response: " + responseData);
                    runOnUiThread(() -> Toast.makeText(DataActivity.this, "上传饮食数据成功！", Toast.LENGTH_SHORT).show());
                } else {
                    // 处理错误响应
                    Log.e("TAG", "Request failed: " + response.message());
                    runOnUiThread(() -> Toast.makeText(DataActivity.this, "请求失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 弹出打卡完成对话框
    @SuppressLint("ResourceType")
    private void showRecordMealDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("打卡提醒")
                .setMessage("打卡完成！您今日之内则无需再次记录饮食数据~")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 确定后将按钮变灰色并禁用
                    isMealRecorded = true;
                    button.setEnabled(false);
                    button.setBackgroundResource(R.drawable.followed_my);// 修改按钮背景颜色

                    // 进行PUT请求
                    sendPutRequest();
                    dialog.dismiss(); // 关闭对话框
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss(); // 关闭对话框
                });
        builder.create().show(); // 显示对话框
    }

    private void sendPutRequest() {
        // 创建OkHttpClient对象
        OkHttpClient client = new OkHttpClient();
        // 获取 SharedPreferences 中的 token
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 UserLoginToken，默认值为 null

        // 构建请求
        String url = "http://8.134.144.65:8082/cofood/DailySign?isSigned=1";
        assert token != null;
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(null, new byte[0])) // PUT请求需要提供RequestBody，空字节数组即可
                .addHeader("token", token) // 假设你有一个获取token的方法
                .build();

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 请求成功，处理响应
                    assert response.body() != null;
                    String responseData = response.body().string();
                    // 根据需要处理响应数据
                    Log.d("HTTP Response", responseData);
                } else {
                    // 处理请求错误
                    Log.e("HTTP Error", "请求失败: " + response.code());
                }
            }
        });
    }

    /*******************************************饮食数据的显示*******************************************************/

    // 更新当天的总卡路里
    private void UpdateCalories(int totalCalories) {
        String url = "http://8.134.144.65:8082/cofood/DailySign?consumeCalories=" + totalCalories;
        Log.e("TAG", "UpdateCalories: " + totalCalories );
        // 创建 PUT 请求的请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("consumeCalories", String.valueOf(totalCalories)) // 将卡路里作为参数传入
                .build();

        // 获取 SharedPreferences 中的 token
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 UserLoginToken，默认值为 null
        Log.e("TAG", "UpdateCalories: token" + token );
        // 创建请求
        assert token != null;
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("token", token) // 添加 token 到请求头
                .build();


        // 执行请求
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace(); // 请求失败，打印异常
                runOnUiThread(() -> Toast.makeText(DataActivity.this, "网络请求失败，请稍后重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    Log.e("TAG", "Response Data: " + responseData);  // 添加此行打印响应数据

                    // 解析响应数据
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        int isSigned = jsonResponse.getJSONObject("data").getInt("isSigned");
                        int remainCalories = jsonResponse.getJSONObject("data").getInt("remainCalories");

                        // 在UI线程更新用户界面或提示用户
                        runOnUiThread(() -> {
                            new AlertDialog.Builder(DataActivity.this)
                                    .setTitle("更新成功")
                                    .setPositiveButton("确定", null)
                                    .show();
                        });

                    } catch (JSONException e) {
                        Log.e("TAG", "onResponse: " + e.getMessage());
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(DataActivity.this, "数据解析错误", Toast.LENGTH_SHORT).show());
                    }

                } else {
                    runOnUiThread(() -> Toast.makeText(DataActivity.this, "请求失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }

        });
    }


    //上传食物推荐三餐相加的卡路里
    private void uploadTargetCalories(int targetCalories) {
        String url = "http://8.134.144.65:8082/cofood/DailySign";
        Log.e("TAG", "uploadTargetCalories: " + targetCalories );
        // 创建请求体
        RequestBody requestBody = new FormBody.Builder()
                .add("targetCalories", String.valueOf(targetCalories)) // 将卡路里作为参数传入
                .build();

        // 获取 SharedPreferences 中的 token
        SharedPreferences sharedPreferences = getSharedPreferences("UserLoginToken", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null); // 获取 UserLoginToken，默认值为 null

        // 创建请求
        assert token != null;
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody) // 使用 POST 请求
                .addHeader("token", token) // 添加 token 到请求头
                .build();

        // 执行请求
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace(); // 请求失败，打印异常
                runOnUiThread(() -> Toast.makeText(DataActivity.this, "网络请求失败，请稍后重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                } else {
                    runOnUiThread(() -> Toast.makeText(DataActivity.this, "请求失败: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
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

    // 解析 JSON 数据，并更新UI
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
                runOnUiThread(() -> {
                    setCenterTextOnlyCalories("今日推荐\n" + getTargetCalories + "大卡");
                    hadEaten.setText(String.valueOf(getTargetCalories-remainCalories));
                    reminder.setText(String.valueOf(remainCalories));
                    updatePieChartData(getTargetCalories,remainCalories);
                    pieChart.invalidate(); // 强制更新 PieChart
                });
            }
        } catch (Exception e) {
            Log.e("DataActivity", "解析数据失败: " + e.getMessage());
        }
    }
    //更新环状表状态
    private void updatePieChartData(int getTargetCalories, int remainCalories) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // 增加灰色和绿色数据
        entries.add(new PieEntry(getTargetCalories-remainCalories, "")); // 灰色
        entries.add(new PieEntry(remainCalories, "")); // 绿色

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.GRAY, Color.GREEN); // 设置颜色

        dataSet.setValueLinePart1Length(0f); // 隐藏扇形位置标签的连接线
        dataSet.setValueLinePart2Length(0f); // 同上
        dataSet.setValueLineWidth(0f); // 隐藏扇形位置标签的连接线宽度
        dataSet.setValueTextSize(0f); // 不显示数据值


        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // 刷新图表

    }

}


