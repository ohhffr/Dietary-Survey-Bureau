package com.example.quantacup.searchview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.quantacup.R;
import com.example.quantacup.adapter.FoodAdapter;
import com.example.quantacup.bean.Food;
import com.example.quantacup.bean.FoodCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DrinkFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ListView listView;
    private FoodAdapter adapter;
    private List<Food> productList;


    public DrinkFragment() {

    }

    public static DrinkFragment newInstance(String param1, String param2) {
        DrinkFragment fragment = new DrinkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view = inflater.inflate(R.layout.fragment_drink, container, false);
        listView = view.findViewById(R.id.listViewDrink);
        productList = new ArrayList<>();
        adapter = new FoodAdapter(getContext(),productList);
        listView.setAdapter(adapter);

        fetchDrink(); // 调用方法获取数据

        return view;
    }

    private void fetchDrink(){
        // 使用 OkHttp 或任何网络请求库
        String url = "http://8.134.144.65:8082/cofood/FoodBaseByCategory?categoryId=" + 6; // 在 URL 中添加 categoryId

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("TAG", "Request failed: " + e.getMessage());
                // 在 UI 线程中更新 UI
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "请求失败", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String jsonResponse = response.body().string();
                    parseJsonAndUpdateUI(jsonResponse);
                } else {
                    Log.e("TAG", "Request failed: " + response.message());
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "请求失败: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }


    private void parseJsonAndUpdateUI(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray foodBaseList = data.getJSONArray("foodBaseList");

            // 选择一个特定的 categoryId
            int targetCategoryId = 6; // 这里可以根据需要更改

            for (int i = 0; i < foodBaseList.length(); i++) {
                JSONObject foodJson = foodBaseList.getJSONObject(i);
                int id = foodJson.getInt("id");
                String foodName = foodJson.getString("foodName");
                String picture = foodJson.getString("picture");
                int calories = foodJson.getInt("calories");
                JSONObject foodCategoryJson = foodJson.getJSONObject("foodCategory");
                int categoryId = foodCategoryJson.getInt("categoryId");
                String pictureUrl = "http://8.134.144.65:8082/cofood/foodImages/" + picture;

                // 仅当 categoryId 匹配时才添加
                if (categoryId == targetCategoryId) {
                    Food food = new Food(id, foodName, pictureUrl, calories, new FoodCategory(id, categoryId));
                    productList.add(food);
                }
            }

            // 更新 UI
            requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("TAG", "JSON parsing error: " + e.getMessage());
        }
    }
}