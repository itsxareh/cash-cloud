package com.example.onlinebankingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
public class WalkThroughFragment extends Fragment{
    private ViewPager2 viewPager;
    private WalkThroughAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sign_up_fragment, container, false);

        viewPager = rootView.findViewById(R.id.viewPager);
        adapter = new WalkThroughAdapter(getActivity());
        viewPager.setAdapter(adapter);

        return rootView;
    }

    public void navigateToLastPage() {
        viewPager.setCurrentItem(adapter.getItemCount() - 1, true);
    }
}
