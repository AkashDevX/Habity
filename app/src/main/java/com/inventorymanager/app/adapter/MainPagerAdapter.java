package com.inventorymanager.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.inventorymanager.app.fragment.DashboardFragment;
import com.inventorymanager.app.fragment.InventoryFragment;

public class MainPagerAdapter extends FragmentStateAdapter {
    private static final int TAB_COUNT = 2;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new InventoryFragment();
        } else {
            return new DashboardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
