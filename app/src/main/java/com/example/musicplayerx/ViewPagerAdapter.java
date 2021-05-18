package com.example.musicplayerx;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.musicplayerx.R;

import java.util.ArrayList;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    //@StringRes
    //private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    //private final Context mContext;

    private ArrayList<Fragment> fragments;

    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
        this.fragments = new ArrayList<>();
    }

    public void addFragments(Fragment fragment){
        fragments.add(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}