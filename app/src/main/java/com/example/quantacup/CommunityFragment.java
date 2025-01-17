package com.example.quantacup;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.quantacup.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

//社区
public class CommunityFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    List<Fragment> fragmentList = new ArrayList<>();//每个页面的列表
    ViewPagerAdapter viewPagerAdapter;
    String[] titles = {"食卡社区", "我的关注", "购物商场"};//设置导航栏各量级名称



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_community, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);


        fragmentList.add(new CommunityPostsFragment());//添加页面，有多少页面添加多少个fragment对象
        fragmentList.add(new MyPostsFragment());//添加页面
        fragmentList.add(new ShoppingMallFragment());//添加页面

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragmentList, titles);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);



        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                View customTab = LayoutInflater.from(getContext()).inflate(R.layout.tab_item, null);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView tabText = customTab.findViewById(R.id.tab_item_text);

                tabText.setText(viewPagerAdapter.getPageTitle(i)); // 设置Tab的标题
                tab.setCustomView(customTab);
            }
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabView(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 可以忽略
            }
        });

        // 初始化选中状态
        TabLayout.Tab initialTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        if (initialTab != null) {
            updateTabView(initialTab, true);
        }
    }

    private void updateTabView(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView != null) {
            TextView tabText = customView.findViewById(R.id.tab_item_text);

            if (isSelected) {
                tabText.setTextSize(25); // 选中时的字体大小
                tabText.setTextColor(getActivity().getResources().getColor(R.color.deep_green));
                //todo 添加fragment判断
            } else {
                tabText.setTextSize(18); // 未选中时的字体大小
                tabText.setTextColor(getActivity().getResources().getColor(R.color.black));
            }
        }
    }





}
