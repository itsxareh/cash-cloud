package com.example.onlinebankingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WalkThroughAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 3;

    public WalkThroughAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Page1SignUp();
            case 1:
                return new Page2SignUp();
            case 2:
                return new Page3SignUp();
            default:
                return new Page1SignUp();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}