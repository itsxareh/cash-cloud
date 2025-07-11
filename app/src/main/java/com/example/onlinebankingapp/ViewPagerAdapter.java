package com.example.onlinebankingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity){
        super(fragmentActivity);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position){
        switch (position){
            case 0:
                return new WalletFragment();
            case 1:
                return new SavingsFragment();
//            case 2:
//                return new CreditFragment();
////            case 3:
////                return new LoansFragment();
            case 2:
                return new CardsFragment();
            default:
                return new WalletFragment();
        }
    }
    @Override
    public int getItemCount() {
        return 3;
    }
}
