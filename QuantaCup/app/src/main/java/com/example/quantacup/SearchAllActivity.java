package com.example.quantacup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.quantacup.adapter.SuggestionAdapter;
import com.example.quantacup.bean.Food;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchAllActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all);


        EditText foodInput = findViewById(R.id.search_box1); // 输入食物名称

        ListView suggestionList = findViewById(R.id.searchList1); // 食物建议列表

        SuggestionAdapter adapter = new SuggestionAdapter(this, new ArrayList<>());
        suggestionList.setAdapter(adapter);

        // 编辑框文本变化事件
        foodInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

        // 点击搜索栏目每一项的事件
        suggestionList.setOnItemClickListener((parent, view, position, id) -> {
            Food selectedFood = adapter.getItem(position); // 获取选中的 Food 对象
            if (selectedFood != null) {
                foodInput.setText(selectedFood.getFoodName()); // 显示食物名称在输入框里
                suggestionList.setVisibility(View.GONE); // 隐藏建议列表
                fetchFoodDetails(selectedFood.getFoodName());
            }
        });
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

    private void fetchFoodDetails(String foodName) {

        String url = "http://8.134.144.65:8082/cofood/FoodBaseByName?foodName=" + foodName;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    parseFoodDetailsJson(responseData);
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "请求失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void parseFoodDetailsJson(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            if (jsonObject.getInt("code") == 200) {
                JSONObject foodData = jsonObject.getJSONObject("data").getJSONObject("foodBases");

                String foodName = foodData.getString("foodName");
                int calories = foodData.getInt("calories");
                String picture = "http://8.134.144.65:8082/cofood/foodImages/" + foodData.getString("picture");

                // 启动新页面，传递数据
                Intent intent = new Intent(SearchAllActivity.this, FoodItemActivity.class);
                intent.putExtra("foodName", foodName);
                intent.putExtra("calories", calories);
                intent.putExtra("picture", picture);
                startActivity(intent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "数据解析失败", Toast.LENGTH_SHORT).show());
        }
    }
}