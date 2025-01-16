package com.example.quantacup.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.quantacup.searchview.DairyProductFragment;
import com.example.quantacup.searchview.CookingOilFragment;
import com.example.quantacup.searchview.CondimentsFragment;
import com.example.quantacup.searchview.MainDishGrainFragment;
import com.example.quantacup.searchview.MeatFragment;
import com.example.quantacup.searchview.NutFragment;
import com.example.quantacup.searchview.OtherFoodFragment;
import com.example.quantacup.searchview.SnackFragment;
import com.example.quantacup.searchview.VegetableFragment;

public class MyPagerAdapter extends FragmentStateAdapter {

    private final String[] tabTitles = new String[]{"主食", "奶类制品", "蔬菜", "肉类", "海鲜类", "谷类", "水果", "豆类坚果", "零食饮料"};

    public MyPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new DairyProductFragment();
            case 2:
                return new VegetableFragment();
            case 3:
                return new MeatFragment();
            case 4:
                return new OtherFoodFragment();
            case 5:
                return new CondimentsFragment();
            case 6:
                return new CookingOilFragment();
            case 7:
                return new NutFragment();
            case 8:
                return new SnackFragment();
            case 0:
            default:
                return new MainDishGrainFragment(); // 默认返回
        }
    }

    @Override
    public int getItemCount() {
        return 9;
    }

    @Nullable
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
