package com.example.quantacup.help;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ResetCaloriesReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取 SharedPreferences 中的值
        SharedPreferences preferences = context.getSharedPreferences("CaloriePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // 重置 dayTotalCalories 和 calorieRecommendation
        int calorieRecommendation = 2250;
        int dayTotalCalories = 0;

        editor.putInt("dayTotalCalories", dayTotalCalories);
        editor.putInt("calorieRecommendation", calorieRecommendation);
        editor.apply();

        // 可以使用一个本地广播通知 UI 更新
        Intent updateIntent = new Intent("com.example.UPDATE_CALORIES");
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
    }
}
