package com.example.quantacup;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private DietFragment dietFragment;
    private CommunityFragment communityFragment;
    private MyselfFragment myselfFragment;
    private HealthFragment healthFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        StatusBar statusBar = new StatusBar(MainActivity.this);
        statusBar.setColor(R.color.white);
        statusBar.setTextColor(false);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        selectFragment(0);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_diet) {
                    selectFragment(0);
                } else if (item.getItemId() == R.id.menu_health) {
                    selectFragment(1);
                } else if (item.getItemId() == R.id.menu_community) {
                    selectFragment(2);
                } else if (item.getItemId() == R.id.menu_myself) {
                    selectFragment(3);
                }else if(item.getItemId() == R.id.menu_add){
                    Intent intent = new Intent(MainActivity.this, AddActivity.class);
                    startActivity(intent);
                    return true; // 表示已处理
                }
                return true;
            }
        });
    }

    private void selectFragment(int position) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        hideFragment(fragmentTransaction);
        if (position == 0) {
            if (dietFragment == null) {
                dietFragment = new DietFragment();
                fragmentTransaction.add(R.id.content, dietFragment);
            } else {
                fragmentTransaction.show(dietFragment);
            }
        } else if (position == 1) {
            if (healthFragment == null) {
                healthFragment = new HealthFragment();
                fragmentTransaction.add(R.id.content, healthFragment);
            } else {
                fragmentTransaction.show(healthFragment);
            }
        } else if (position == 2) {
            if (communityFragment == null) {
                communityFragment = new CommunityFragment();
                fragmentTransaction.add(R.id.content, communityFragment);
            } else {
                fragmentTransaction.show(communityFragment);
            }
        } else {
            if (myselfFragment == null) {
                myselfFragment = new MyselfFragment();
                fragmentTransaction.add(R.id.content, myselfFragment);
            } else {
                fragmentTransaction.show(myselfFragment);
            }
        }

        // 提交
        fragmentTransaction.commit();
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (dietFragment != null) {
            fragmentTransaction.hide(dietFragment);
        }
        if (healthFragment != null) {
            fragmentTransaction.hide(healthFragment);
        }
        if (communityFragment != null) {
            fragmentTransaction.hide(communityFragment);
        }
        if (myselfFragment != null) {
            fragmentTransaction.hide(myselfFragment);
        }
    }


}