package com.inventorymanager.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.inventorymanager.app.adapter.MainPagerAdapter;
import com.inventorymanager.app.viewmodel.ItemViewModel;
import com.inventorymanager.app.fragment.InventoryFragment;

public class MainActivity extends AppCompatActivity {
    private ExtendedFloatingActionButton fabAdd;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MainPagerAdapter pagerAdapter;
    private ItemViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_tabs);

        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);

        initViews();
        setupViewPager();
        setupListeners();
        setupBackPressHandler();
    }

    private void initViews() {
        fabAdd = (ExtendedFloatingActionButton) findViewById(R.id.fabAdd);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Inventory");
            } else {
                tab.setText("Dashboard");
            }
        }).attach();
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditItemActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (viewModel != null) {
                        viewModel.setFilter(null, newText);
                    }
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_view) {
            if (viewPager.getCurrentItem() == 0 && viewModel != null) {
                viewModel.requestToggleView();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_application)
                .setMessage(R.string.exit_confirmation)
                .setPositiveButton(R.string.exit, (dialog, which) -> {
                    finishAffinity();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }
}
