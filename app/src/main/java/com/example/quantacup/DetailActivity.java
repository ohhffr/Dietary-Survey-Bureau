package com.example.quantacup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;

//推荐  ---》  三餐推荐  ---》 早餐
public class DetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private PieChart pieChart; // 饼状图
    private TextView foodName, foodDescription, calories, caloriesNRV, protein, proteinNRV,
                    fat,fatNRV,carbohydrates,carbohydratesNRV,sodium,sodiumNRV,calcium,calciumNRV;
    private ImageView imageView1, imageView2;
    private CardView cardView;

    private static final FoodItem[] FOOD_ITEMS = {//食物信息
            new FoodItem("煎鸡蛋", "金黄香脆，外焦里嫩，美味简单",
                    "704千焦", "8.4%", "12.6克", "21.0%",
                    "12.1克", "20.2%", "2.3克", "0.8%",
                    "127毫克", "6.4%", "54毫克", "6.8%"),
            new FoodItem("纯牛奶", "高质量的饮品，营养价值高",
                    "203千焦", "3%", "3.1克", "5%",
                    "3.7克","6%","4.8克","2%",
                        "70毫克","4%","100毫克","13%")
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initializeViews();//初始化界面
        setupToolbar();//设置toolbar
        setupClickListeners();//监听事件
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.tb_return_btn);
        imageView1 = findViewById(R.id.food1);
        imageView2 = findViewById(R.id.food2);
        cardView = findViewById(R.id.foodCard);
        foodName = findViewById(R.id.food_name);
        foodDescription = findViewById(R.id.food_description);
        pieChart = findViewById(R.id.consume_pie_chart);
        calories = findViewById(R.id.food_calories);
        caloriesNRV = findViewById(R.id.food_caloriesNRV);
        protein = findViewById(R.id.food_protein);
        proteinNRV = findViewById(R.id.food_proteinNRV);
        fat = findViewById(R.id.food_fat);
        fatNRV = findViewById(R.id.food_fatNRV);
        carbohydrates = findViewById(R.id.food_carbohydrates);
        carbohydratesNRV = findViewById(R.id.food_carbohydratesNRV);
        sodium = findViewById(R.id.food_sodium);
        sodiumNRV = findViewById(R.id.food_sodiumNRV);
        calcium = findViewById(R.id.food_calcium);
        calciumNRV = findViewById(R.id.food_calciumNRV);
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void setupClickListeners() {
        imageView1.setOnClickListener(v -> updateFoodInfo(FOOD_ITEMS[0]));
        imageView2.setOnClickListener(v -> updateFoodInfo(FOOD_ITEMS[1]));
    }
    private void updateFoodInfo(FoodItem foodItem) {
        foodName.setText(foodItem.name);
        foodDescription.setText(foodItem.description);
        calories.setText(foodItem.calories);
        caloriesNRV.setText(foodItem.caloriesNRV);
        protein.setText(foodItem.protein);
        proteinNRV.setText(foodItem.proteinNRV);
        fat.setText(foodItem.fat);
        fatNRV.setText(foodItem.fatNRV);
        carbohydrates.setText(foodItem.carbohydrates);
        carbohydratesNRV.setText(foodItem.carbohydratesNRV);
        sodium.setText(foodItem.sodium);
        sodiumNRV.setText(foodItem.sodiumNRV);
        calcium.setText(foodItem.calcium);
        calciumNRV.setText(foodItem.calciumNRV);

    }
    private static class FoodItem {
        String name,description,calories,caloriesNRV,protein,proteinNRV,fat,fatNRV,carbohydrates,carbohydratesNRV,
                sodium,sodiumNRV,calcium,calciumNRV;
        public FoodItem(String name, String description, String calories, String caloriesNRV,
                        String protein, String proteinNRV, String fat, String fatNRV,
                        String carbohydrates, String carbohydratesNRV, String sodium, String sodiumNRV,
                        String calcium, String calciumNRV) {
            this.name = name;
            this.description = description;
            this.calories = calories;
            this.caloriesNRV = caloriesNRV;
            this.protein = protein;
            this.proteinNRV = proteinNRV;
            this.fat = fat;
            this.fatNRV = fatNRV;
            this.carbohydrates = carbohydrates;
            this.carbohydratesNRV = carbohydratesNRV;
            this.sodium = sodium;
            this.sodiumNRV = sodiumNRV;
            this.calcium = calcium;
            this.calciumNRV = calciumNRV;
        }

    }
}

