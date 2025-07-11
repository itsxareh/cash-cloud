package com.example.onlinebankingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewShopAdapter extends FragmentStateAdapter {
    public ViewShopAdapter(@NonNull FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position){
        switch (position){
            case 0:
                return new ShopFragment();
            case 1:
                return new LoadFragment();
            case 2:
                return new GamesFragment();
            case 3:
                return new EntertainmentFragment();
            case 4:
                return new TransportFragment();
            default:
                return new ShopFragment();
        }
    }
    @Override
    public int getItemCount() {
        return 5;
    }
}
