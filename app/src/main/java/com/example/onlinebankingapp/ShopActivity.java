package com.example.onlinebankingapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ShopActivity extends AppCompatActivity {
    private TabLayout tabs;
    private ViewPager2 viewPager;
    private ViewShopAdapter viewShopAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);
        viewShopAdapter = new ViewShopAdapter(this);
        viewPager.setAdapter(viewShopAdapter);

        new TabLayoutMediator(tabs, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Home");
                    break;
                case 1:
                    tab.setText("Load");
                    break;
                case 2:
                    tab.setText("Games");
                    break;
                case 3:
                    tab.setText("Entertainment");
                    break;
                case 4:
                    tab.setText("Transport");
                    break;
            }
        }).attach();

        TabLayout.Tab walletTab = tabs.getTabAt(1);
        if (walletTab != null) {
            walletTab.select();
            walletTab.view.setBackgroundResource(R.drawable.black_rounded_border);
        }

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.view.setBackgroundResource(R.drawable.black_rounded_border);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.transparent));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}