package com.example.quantacup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Objects;

public class FoodItemActivity extends AppCompatActivity {

    private ImageView foodImage;
    private TextView foodNameTextView,titleFoodName;
    private TextView foodCaloriesTextView;
    private Toolbar toolbar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_item); // 确保你有一个名为 activity_food_detail 的布局

        // 初始化 Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // 显示返回箭头
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 不显示标题
        toolbar.setNavigationOnClickListener(v -> finish()); // 返回按钮的点击事件

        titleFoodName = findViewById(R.id.result_foodName);
        foodImage = findViewById(R.id.food_image);
        foodNameTextView = findViewById(R.id.food_name);
        foodCaloriesTextView = findViewById(R.id.food_calories);

        // 获取传递过来的数据

        Intent intent = getIntent();
        String foodName = intent.getStringExtra("foodName");
        int calories = intent.getIntExtra("calories", 0);
        String picture = intent.getStringExtra("picture");

        // 设置数据到 UI 组件
        titleFoodName.setText(foodName);
        foodNameTextView.setText(foodName);
        foodCaloriesTextView.setText(calories + "千卡/100克");

        // 使用 Glide 加载图片
        Glide.with(this)
                .load(picture)
                .into(foodImage);
    }
}