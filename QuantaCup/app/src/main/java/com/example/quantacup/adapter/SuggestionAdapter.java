package com.example.quantacup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.quantacup.bean.Food;

import java.util.List;

public class SuggestionAdapter extends ArrayAdapter<Food> {
    private List<Food> foodList; // 存储 Food 对象的列表
    public SuggestionAdapter(Context context, List<Food> foods) {
        super(context, 0, foods);
        this.foodList = foods; // 初始化 foodList
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 获取当前食物对象
        Food food = getItem(position);

        // 如果视图为空，则创建新的视图
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        // 更新视图内容
        TextView textView = convertView.findViewById(android.R.id.text1);
        if (food != null) {
            textView.setText(food.getFoodName()); // 显示食物名称
        }

        return convertView;
    }

    public Food getFoodByName(String foodName) {
        for (Food food : foodList) {
            if (food.getFoodName().equalsIgnoreCase(foodName)) {
                return food;
            }
        }
        return null; // 如果没有找到对应的 Food 对象
    }

}

