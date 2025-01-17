package com.example.quantacup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quantacup.adapter.MyPagerAdapter;
import com.example.quantacup.adapter.TabAdapter;
import com.example.quantacup.help.SpacingItemDecoration;

import java.util.Objects;


public class SearchActivity extends AppCompatActivity {


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);


        EditText foodInput = findViewById(R.id.search_box);

        foodInput.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, SearchAllActivity.class);
            startActivity(intent);
        });


        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ViewPager2 viewPager = findViewById(R.id.view_pager);
        RecyclerView tabRecyclerView = findViewById(R.id.tab_recycler_view);

        // 设置ViewPager的Adapter
        MyPagerAdapter adapter1 = new MyPagerAdapter(this);
        viewPager.setAdapter(adapter1);

        // 禁用用户输入（滑动切换）
        viewPager.setUserInputEnabled(false);  // 添加这一行代码

        // 设置RecyclerView的Adapter
        String[] tabTitles = new String[]{"谷薯芋、杂豆、主食", "奶类及制品",
                "蔬菜和菌藻", "蛋类、肉类及制品", "饮料", "食用油、油脂及制品", "调味品", "坚果大豆及制品", "零食、点心、冷饮", "其他"};
        TabAdapter tabAdapter = new TabAdapter(tabTitles, position -> viewPager.setCurrentItem(position, true));
        tabRecyclerView.setAdapter(tabAdapter);
        tabRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // 添加间隔装饰
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing); // 在这里可以定义间隔的大小
        tabRecyclerView.addItemDecoration(new SpacingItemDecoration(spacingInPixels));

        // 设置ViewPager的回调
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabAdapter.notifyItemChanged(tabAdapter.selectedPosition);
                tabAdapter.selectedPosition = position;
                tabAdapter.notifyItemChanged(position);
            }
        });

        Toolbar toolbar = findViewById(R.id.tb_search_btn);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

        toolbar.setNavigationOnClickListener(view -> finish());
    }

}
