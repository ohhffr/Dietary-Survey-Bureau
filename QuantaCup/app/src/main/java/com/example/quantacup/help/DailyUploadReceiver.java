package com.example.quantacup.help;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.FormBody;
import okhttp3.Response;

public class DailyUploadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 从 SharedPreferences 获取 userId 和 dayTotalCalories
        SharedPreferences userPreferences = context.getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = userPreferences.getString("user_id", null);

        SharedPreferences caloriePreferences = context.getSharedPreferences("CaloriePrefs", Context.MODE_PRIVATE);
        int dayTotalCalories = caloriePreferences.getInt("dayTotalCalories", 0);

        // 上传数据
        new UploadDailyCaloriesTask().execute(userId, String.valueOf(dayTotalCalories));
    }

    private static class UploadDailyCaloriesTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String userId = params[0];
            String totalCalories = params[1];

            OkHttpClient client = new OkHttpClient();

            // 构造 POST 请求
            FormBody body = new FormBody.Builder()
                    .add("userId", userId)
                    .add("totalCalories", totalCalories)
                    .build();

            Request request = new Request.Builder()
                    .url("http://8.134.144.65:8082/cofood/DailySign")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
