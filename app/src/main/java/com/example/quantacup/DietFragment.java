package com.example.quantacup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quantacup.help.Carousel;
import com.example.quantacup.help.CircleCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//推荐
public class DietFragment extends Fragment {
    private TextView breakfast,lunch,dinner,breakfastTotalCal,lunchTotalCal,dinnerTotalCal;
    LinearLayout search_view;
    LinearLayout food_view;
    private ImageView breakfastFoodImage1,breakfastFoodImage2,breakfastFoodImage3,lunchFoodImage1,
            lunchFoodImage2,lunchFoodImage3, dinnerFoodImage1,dinnerFoodImage2,dinnerFoodImage3;
    private final OkHttpClient client = new OkHttpClient(); // 将 OkHttpClient 声明为类变量
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //从fragment_diet.xml布局文件中加载一个View对象，并且不立即将其附加到传入的父视图container上，而是将这个加载好的View对象赋值给变量view
        View view = inflater.inflate(R.layout.fragment_diet,container,false);

        //上面的轮播图
        ViewPager2 viewPager2 = view.findViewById(R.id.viewPager2);
        LinearLayout layout = view.findViewById(R.id.index_dot);
        Carousel carousel = new Carousel(getContext(), layout, viewPager2);
        carousel.initViews(new int[]{R.drawable.red_dot,R.drawable.rotation01,R.drawable.rotation02,
                R.drawable.rotation03,R.drawable.grey_dot});
        carousel.startAutoScroll();

        //下面的轮播图
        ViewPager2 viewPager3 = view.findViewById(R.id.viewPager3);
        LinearLayout layout1 = view.findViewById(R.id.index_dot1);
        Carousel carousel1 = new Carousel(getContext(), layout1, viewPager3);
        carousel1.initViews(new int[]{R.drawable.red_dot,R.drawable.rotation04,R.drawable.rotation05,R.drawable.rotation06,R.drawable.grey_dot});
        carousel1.startAutoScroll();


        search_view = view.findViewById(R.id.search_view);
        search_view.setOnClickListener(view1 -> {
            int id = view1.getId();
            if (id == R.id.search_view) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        food_view = view.findViewById(R.id.food_view);
        food_view.setOnClickListener(view12 -> {
            int id = view12.getId();
            if (id == R.id.food_view) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                startActivity(intent);
            }
        });
        EditText edt_search = view.findViewById(R.id.search);
        edt_search.setOnClickListener(view13 -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
        breakfast = view.findViewById(R.id.breakfast);
        lunch = view.findViewById(R.id.lunch);
        dinner = view.findViewById(R.id.dinner);
        breakfastTotalCal = view.findViewById(R.id.breakfast_calories);
        lunchTotalCal = view.findViewById(R.id.lunch_calories);
        dinnerTotalCal = view.findViewById(R.id.dinner_calories);

        breakfastFoodImage1 = view.findViewById(R.id.breakfast_image1);
        breakfastFoodImage2 = view.findViewById(R.id.breakfast_image2);
        breakfastFoodImage3 = view.findViewById(R.id.breakfast_image3);

        lunchFoodImage1 = view.findViewById(R.id.lunch_image1);
        lunchFoodImage2 = view.findViewById(R.id.lunch_image2);
        lunchFoodImage3 = view.findViewById(R.id.lunch_image3);

        dinnerFoodImage1 = view.findViewById(R.id.dinner_image1);
        dinnerFoodImage2 = view.findViewById(R.id.dinner_image2);
        dinnerFoodImage3 = view.findViewById(R.id.dinner_image3);
        checkForDataUpdate();
        return view;
    }

    //每日只推荐一次饮食搭配
    private void checkForDataUpdate() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("Timestamp", Context.MODE_PRIVATE);
        String savedTimestamp = prefs.getString("time_stamp", null);

        if (savedTimestamp != null) {

            if (!isSameDay(savedTimestamp)) {
                // 如果不是同一天，则进行POST请求加载用户当天的饮食搭配
                fetchUserInfoAndPostData();
            } else {
                // 如果是同一天，则加载今天存在本地内存的饮食搭配（未更新的）
                loadDataFromLocal();
            }
        } else {
            // 如果没获取到时间戳再进行一次POST请求
            fetchUserInfoAndPostData();
        }
    }

    private boolean isSameDay(String savedTimestamp) {
        //解析后端响应中的时间戳
        String[] parts = savedTimestamp.split(" ");
        String datePart = parts[0]; // Format: yyyy-MM-dd

        // 得到今天的日期
        Calendar calendar = Calendar.getInstance();
        String today = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

        return datePart.equals(today);
    }

    //首先通过GET请求获取用户的 “身高体重年龄性别”
    private void fetchUserInfoAndPostData() {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserId", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);//从本地缓存拿用户id

        // GET 请求以获取用户信息
        String urlString = "http://8.134.144.65:8082/cofood/account/UserBodyInfo?userId=" + userId;
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("TAG", "onFailure: " + e.getMessage() );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    try {

                        JSONObject userInfo = new JSONObject(responseData);
                        int age = userInfo.getJSONObject("data").getJSONObject("list").getInt("age");
                        int gender = userInfo.getJSONObject("data").getJSONObject("list").getInt("gender");
                        int height = userInfo.getJSONObject("data").getJSONObject("list").getInt("height");
                        int weight = userInfo.getJSONObject("data").getJSONObject("list").getInt("weight");
                        // 现在使用获取的参数发送 POST 请求
                        if (gender == 0){
                            gender = 2;//因为身体数据那，女0男1，食物数据那又是男1女2
                        }
                        //将用户的“身高体重年龄性别”传入 食物推荐算法
                        postDietData(age, gender, height, weight);
                    }catch (JSONException e){
                        e.printStackTrace();
                        Log.e("TAG", "onResponse: " + e.getMessage() );
                    }

                }
            }
        });
    }


    //POST请求食物推荐算法
    private void postDietData(int age, int gender, int height, int weight) {
        new Thread(() -> {
            // 定义 POST 请求的 URL
            String postUrl = "http://8.134.144.65:8083/cofood/recommend";

            // 创建 form-data 请求体
            RequestBody formBody = new FormBody.Builder()
                    .add("age", String.valueOf(age))
                    .add("gender", String.valueOf(gender))
                    .add("height", String.valueOf(height))
                    .add("weight", String.valueOf(weight))
                    .build();

            // 创建请求
            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(formBody)
                    .build();

            // 发送请求并处理响应
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace(); // 处理失败情况
                    // 这里可以提示用户请求失败
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "请求失败，请重试", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        // 解析响应数据
                        try {
                            JSONObject responseJson = new JSONObject(responseData);
                            // 使用 runOnUiThread 更新 UI
                            requireActivity().runOnUiThread(() -> updateMealViews(responseJson));

                            // 保存后端返回的时间戳
                            SharedPreferences prefs = requireContext().getSharedPreferences("Timestamp", Context.MODE_PRIVATE);
                            prefs.edit().putString("time_stamp", responseJson.getString("timestamp")).apply();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // 这里可以提示用户JSON解析失败
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "数据解析错误", Toast.LENGTH_SHORT).show());
                        }

                    } else {
                        // 处理不成功的响应情况
                        // 这里可以提示用户请求失败
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "请求失败: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        }).start();

    }

    @SuppressLint("SetTextI18n")
    private void updateMealViews(JSONObject responseJson) {
        try {
            // 获取早餐、午餐和晚餐的 JSON 数组
            JSONArray breakfastArray = responseJson.getJSONArray("breakfast");
            JSONArray lunchArray = responseJson.getJSONArray("lunch");
            JSONArray dinnerArray = responseJson.getJSONArray("dinner");

            StringBuilder breakfastText = new StringBuilder();
            StringBuilder lunchText = new StringBuilder();
            StringBuilder dinnerText = new StringBuilder();

            StringBuilder breakfastImages = new StringBuilder();
            StringBuilder lunchImages = new StringBuilder();
            StringBuilder dinnerImages = new StringBuilder();

            double breakfastCalories = 0;
            double lunchCalories = 0;
            double dinnerCalories = 0;

            // 处理早餐
            if (breakfastArray.length() > 0) {

                for (int i = 0; i < breakfastArray.length(); i++) {
                    JSONObject meal = breakfastArray.getJSONObject(i);
                    double foodCalories = meal.getDouble("calories");
                    String breakfastFood = meal.getString("foodName");
                    double breakfastQuality = meal.getDouble("quality"); // 获取质量
                    String breakfastImage = "http://8.134.144.65:8082/cofood/foodImages/" + meal.getString("picture");

                    // 将质量添加到文本中
                    breakfastText.append(breakfastFood).append(" [").append(breakfastQuality).append("g]\n");
                    breakfastImages.append(breakfastImage).append(","); // 收集早餐图片 URL
                    // 获取早餐的总热量
                    breakfastCalories += foodCalories;

                    // 根据索引加载图片
                    if (i == 0) {
                        Glide.with(this).load(breakfastImage).transform(new CircleCrop()).into(breakfastFoodImage1);
                    } else if (i == 1) {
                        Glide.with(this).load(breakfastImage).transform(new CircleCrop()).into(breakfastFoodImage2);
                    } else if (i == 2) {
                        Glide.with(this).load(breakfastImage).transform(new CircleCrop()).into(breakfastFoodImage3);
                    }
                }
                breakfastTotalCal.setText(breakfastCalories + "大卡");
                breakfast.setText(breakfastText.toString());
            } else {
                breakfast.setText("No breakfast suggestions available.");
            }

            // 处理午餐
            if (lunchArray.length() > 0) {

                for (int i = 0; i < lunchArray.length(); i++) {
                    JSONObject meal = lunchArray.getJSONObject(i);
                    double foodCalories = meal.getDouble("calories");
                    String lunchFood = meal.getString("foodName");
                    double lunchQuality = meal.getDouble("quality"); // 获取质量
                    String lunchImage = "http://8.134.144.65:8082/cofood/foodImages/" + meal.getString("picture");

                    // 获取午餐热量
                    lunchCalories += foodCalories;
                    // 将质量添加到文本中
                    lunchText.append(lunchFood).append(" [").append(lunchQuality).append("g]\n");
                    lunchImages.append(lunchImage).append(","); // 收集午餐图片 URL

                    // 根据索引加载图片
                    if (i == 0) {
                        Glide.with(this).load(lunchImage).transform(new CircleCrop()).into(lunchFoodImage1);
                    } else if (i == 1) {
                        Glide.with(this).load(lunchImage).transform(new CircleCrop()).into(lunchFoodImage2);
                    } else if (i == 2) {
                        Glide.with(this).load(lunchImage).transform(new CircleCrop()).into(lunchFoodImage3);
                    }
                }
                lunchTotalCal.setText(lunchCalories + "大卡");
                lunch.setText(lunchText.toString());
            } else {
                lunch.setText("No lunch suggestions available.");
            }

            // 处理晚餐
            if (dinnerArray.length() > 0) {

                for (int i = 0; i < dinnerArray.length(); i++) {
                    JSONObject meal = dinnerArray.getJSONObject(i);
                    double foodCalories = meal.getDouble("calories");
                    String dinnerFood = meal.getString("foodName");
                    double dinnerQuality = meal.getDouble("quality"); // 获取质量
                    String dinnerImage = "http://8.134.144.65:8082/cofood/foodImages/" + meal.getString("picture");

                    // 将质量添加到文本中
                    dinnerText.append(dinnerFood).append(" [").append(dinnerQuality).append("g]\n");
                    dinnerImages.append(dinnerImage).append(","); // 收集晚餐图片 URL

                    dinnerCalories += foodCalories;
                    // 根据索引加载图片
                    if (i == 0) {
                        Glide.with(this).load(dinnerImage).transform(new CircleCrop()).into(dinnerFoodImage1);
                    } else if (i == 1) {
                        Glide.with(this).load(dinnerImage).transform(new CircleCrop()).into(dinnerFoodImage2);
                    } else if (i == 2) {
                        Glide.with(this).load(dinnerImage).transform(new CircleCrop()).into(dinnerFoodImage3);
                    }
                }
                dinnerTotalCal.setText(dinnerCalories + "大卡");
                dinner.setText(dinnerText.toString());
            } else {
                dinner.setText("No dinner suggestions available.");
            }

            // 去掉最后一个逗号
            if (breakfastImages.length() > 0) breakfastImages.setLength(breakfastImages.length() - 1);
            if (lunchImages.length() > 0) lunchImages.setLength(lunchImages.length() - 1);
            if (dinnerImages.length() > 0) dinnerImages.setLength(dinnerImages.length() - 1);

            int dayTotalCalories = (int) (breakfastCalories + lunchCalories + dinnerCalories);

            // 保存数据到本地
            saveDataToLocal(breakfastText.toString(), lunchText.toString(), dinnerText.toString(),
                    breakfastImages.toString(), lunchImages.toString(), dinnerImages.toString(), String.valueOf(breakfastCalories),
                    String.valueOf(lunchCalories), String.valueOf(dinnerCalories), dayTotalCalories);

        } catch (JSONException e) {
            e.printStackTrace();
            breakfast.setText("Error parsing breakfast data.");
            lunch.setText("Error parsing lunch data.");
            dinner.setText("Error parsing dinner data.");
        }
    }



    //保存饮食推荐数据到本地
    private void saveDataToLocal(String breakfastData, String lunchData, String dinnerData,
                                 String breakfastImages, String lunchImages, String dinnerImages,String breakfastCalories,
                                 String lunchCalories,String dinnerCalories,int dayTotalCalories) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MealData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 将数据存储到 SharedPreferences
        editor.putString("breakfast", breakfastData);
        editor.putString("lunch", lunchData);
        editor.putString("dinner", dinnerData);

        // 存储图片 URL
        editor.putString("breakfastImageUrls", breakfastImages);
        editor.putString("lunchImageUrls", lunchImages);
        editor.putString("dinnerImageUrls", dinnerImages);

        editor.putString("breakfastCalories",breakfastCalories);
        editor.putString("lunchCalories",lunchCalories);
        editor.putString("dinnerCalories",dinnerCalories);

        editor.putInt("dayTotalCalories",dayTotalCalories);

        editor.apply(); // 提交更改
    }

    //从本地加载今日推荐饮食数据
    @SuppressLint("SetTextI18n")
    private void loadDataFromLocal() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MealData", Context.MODE_PRIVATE);

        // 加载早餐、午餐和晚餐的数据
        String breakfastData = prefs.getString("breakfast", null);
        String lunchData = prefs.getString("lunch", null);
        String dinnerData = prefs.getString("dinner", null);

        // 加载图片 URL
        String breakfastImageUrls = prefs.getString("breakfastImageUrls", "");
        String lunchImageUrls = prefs.getString("lunchImageUrls", "");
        String dinnerImageUrls = prefs.getString("dinnerImageUrls", "");

        // 加载早餐、午餐和晚餐的卡路里
        String breakfastCalories = prefs.getString("breakfastCalories",null);
        String lunchCalories = prefs.getString("lunchCalories",null);
        String dinnerCalories = prefs.getString("dinnerCalories",null);
        // 检查并加载早餐数据
        if (breakfastData != null) {
            breakfast.setText(breakfastData);
            loadMealImages(breakfastImageUrls, breakfastFoodImage1, breakfastFoodImage2, breakfastFoodImage3);
            breakfastTotalCal.setText(breakfastCalories + "大卡");
        } else {
            breakfast.setText("No breakfast data available.");
        }

        // 检查并加载午餐数据
        if (lunchData != null) {
            lunch.setText(lunchData);
            loadMealImages(lunchImageUrls, lunchFoodImage1, lunchFoodImage2, lunchFoodImage3);
            lunchTotalCal.setText(lunchCalories + "大卡");
        } else {
            lunch.setText("No lunch data available.");
        }

        // 检查并加载晚餐数据
        if (dinnerData != null) {
            dinner.setText(dinnerData);
            loadMealImages(dinnerImageUrls, dinnerFoodImage1, dinnerFoodImage2, dinnerFoodImage3);
            dinnerTotalCal.setText(dinnerCalories + "大卡");
        } else {
            dinner.setText("No dinner data available.");
        }
    }

    // 加载图片的辅助方法
    private void loadMealImages(String imageUrls, ImageView imageView1, ImageView imageView2, ImageView imageView3) {
        // 将 URL 字符串分割成数组
        String[] urls = imageUrls.split(",");

        // 加载图片到对应的 ImageView 中
        if (urls.length > 0) {
            Glide.with(this).load(urls[0]).transform(new CircleCrop()).into(imageView1); // 加载第一张图片
        }
        if (urls.length > 1) {
            Glide.with(this).load(urls[1]).transform(new CircleCrop()).into(imageView2); // 加载第二张图片
        }
        if (urls.length > 2) {
            Glide.with(this).load(urls[2]).transform(new CircleCrop()).into(imageView3); // 加载第三张图片
        }
    }




}