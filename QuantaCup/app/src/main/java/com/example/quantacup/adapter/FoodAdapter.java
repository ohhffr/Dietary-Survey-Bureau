package com.example.quantacup.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.quantacup.R;
import com.example.quantacup.bean.Food;

import java.util.List;

public class FoodAdapter extends ArrayAdapter<Food> {
    private Context context;
    private List<Food> foodList;

    public FoodAdapter(Context context, List<Food> foodList) {
        super(context, 0, foodList);
        this.context = context;
        this.foodList = foodList;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // 检查是否需要回收视图
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_food, parent, false);
        }

        // 获取当前食品对象
        Food food = getItem(position);

        // 填充视图
        TextView foodNameTextView = convertView.findViewById(R.id.food_name);
        ImageView foodImageView = convertView.findViewById(R.id.food_image);
        TextView foodCaloriesTextView = convertView.findViewById(R.id.food_calories);

        assert food != null;
        foodNameTextView.setText(food.getFoodName());

        // 转换卡路里数为字符串
        foodCaloriesTextView.setText(String.valueOf(food.getCalories()) + "千卡/100克"); // 修正这行

        // 使用 Glide 加载图片
        Glide.with(context).load(food.getPicture()).into(foodImageView);

        return convertView;
    }

}

