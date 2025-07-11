// MainActivity.java
package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button userButton;
    private ImageButton profileButton;
    private TabLayout tabs;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    URL url = new URL("http://127.0.0.1:5500/");
//                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                    try {
//                        InputStream in = urlConnection.getInputStream();
//                    } finally {
//                        urlConnection.disconnect();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        userManager = UserManager.getInstance();

        userButton = findViewById(R.id.userButton);
        profileButton = findViewById(R.id.profileButton);
        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewPager);

        userButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileMenuActivity.class)));

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabs, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Wallet");
                    break;
                case 1:
                    tab.setText("Savings");
                    break;
//                case 2:
//                    tab.setText("Credit");
//                    break;
//                case 3:
//                    tab.setText("Loans");
//                    break;
                case 2:
                    tab.setText("Cards");
                    break;
            }
        }).attach();

        TabLayout.Tab walletTab = tabs.getTabAt(0);
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

        loadUserData();
    }
    public ViewPager2 getViewPager() {
        return viewPager;
    }

    private void loadUserData() {
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                if (userData != null && userData.containsKey("fullName")) {
                    String userName = userData.get("fullName").toString();
                    userButton.setText(userName);
                } else {
                    userButton.setText("Unknown User");
                }
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                userButton.setText("Error");
            }
        });
    }
}
