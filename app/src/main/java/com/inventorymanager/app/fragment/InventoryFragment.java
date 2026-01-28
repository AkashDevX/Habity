package com.inventorymanager.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventorymanager.app.R;
import com.inventorymanager.app.AddEditItemActivity;
import com.inventorymanager.app.ItemDetailActivity;
import com.inventorymanager.app.adapter.CategoryAdapter;
import com.inventorymanager.app.adapter.ItemAdapter;
import com.inventorymanager.app.adapter.ItemGridAdapter;
import com.inventorymanager.app.database.ItemEntity;
import com.inventorymanager.app.viewmodel.ItemViewModel;
import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment {
    private RecyclerView rvItems, rvCategories;
    private ItemAdapter listAdapter;
    private ItemGridAdapter gridAdapter;
    private CategoryAdapter categoryAdapter;
    private ItemViewModel viewModel;
    private TextView tvTotalItems, tvTotalCategories, tvTotalValue;
    private View emptyStateLayout;
    private boolean isGridView = false;
    private String selectedCategory = "All";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerViews();
        setupViewModel();
        setupListeners();
    }

    private void initViews(View view) {
        rvItems = view.findViewById(R.id.rvItems);
        rvCategories = view.findViewById(R.id.rvCategories);
        tvTotalItems = view.findViewById(R.id.tvTotalItems);
        tvTotalCategories = view.findViewById(R.id.tvTotalCategories);
        tvTotalValue = view.findViewById(R.id.tvTotalValue);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        View btnAddFirst = view.findViewById(R.id.btnAddFirst);
        if (btnAddFirst != null) {
            btnAddFirst.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddEditItemActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        listAdapter = new ItemAdapter();
        gridAdapter = new ItemGridAdapter();
        rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvItems.setAdapter(listAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            List<String> categoryList = new ArrayList<>();
            categoryList.add("All");
            if (categories != null) {
                categoryList.addAll(categories);
            }
            categoryAdapter.setCategories(categoryList);
        });

        viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                String cat = viewModel.getSelectedCategory().getValue();
                String q = viewModel.getSearchQuery().getValue();
                if (cat != null) selectedCategory = cat;
                if (q != null) searchQuery = q;
                List<ItemEntity> filtered = filterItems(items, selectedCategory, searchQuery);
                listAdapter.submitList(filtered);
                gridAdapter.submitList(filtered);
                updateStatistics(items);
                updateEmptyState(filtered.isEmpty());
            }
        });

        viewModel.getSearchQuery().observe(getViewLifecycleOwner(), q -> {
            if (q != null) searchQuery = q;
        });
        viewModel.getSelectedCategory().observe(getViewLifecycleOwner(), cat -> {
            if (cat != null) selectedCategory = cat;
        });
        viewModel.getToggleViewRequest().observe(getViewLifecycleOwner(), request -> {
            if (Boolean.TRUE.equals(request)) {
                toggleView();
                viewModel.clearToggleViewRequest();
            }
        });
    }

    private void setupListeners() {
        listAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), ItemDetailActivity.class);
            intent.putExtra("item_id", item.getId());
            startActivity(intent);
        });

        gridAdapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(requireContext(), ItemDetailActivity.class);
            intent.putExtra("item_id", item.getId());
            startActivity(intent);
        });

        categoryAdapter.setOnCategoryClickListener(category -> {
            selectedCategory = category;
            categoryAdapter.setSelectedCategory(category);
            viewModel.setFilter(category, null);
            viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
                if (items != null) {
                    List<ItemEntity> filtered = filterItems(items, selectedCategory, searchQuery);
                    listAdapter.submitList(filtered);
                    gridAdapter.submitList(filtered);
                }
            });
        });
    }

    private void updateStatistics(List<ItemEntity> items) {
        if (tvTotalItems != null) {
            tvTotalItems.setText(String.valueOf(items.size()));
        }
        if (tvTotalCategories != null) {
            viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
                if (categories != null) tvTotalCategories.setText(String.valueOf(categories.size()));
            });
        }
        double totalValue = 0;
        for (ItemEntity item : items) {
            if (item.getPrice() != null && item.getPrice() > 0) {
                totalValue += item.getPrice() * item.getQuantity();
            }
        }
        if (tvTotalValue != null) {
            tvTotalValue.setText(String.format("$%.0f", totalValue));
        }
    }

    private void updateEmptyState(boolean empty) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (rvItems != null) {
            rvItems.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private List<ItemEntity> filterItems(List<ItemEntity> items, String category, String query) {
        List<ItemEntity> filtered = new ArrayList<>();
        for (ItemEntity item : items) {
            boolean matchesCategory = category == null || category.equals("All") || item.getCategory().equals(category);
            boolean matchesQuery = query == null || query.isEmpty() || item.getName().toLowerCase().contains(query.toLowerCase());
            if (matchesCategory && matchesQuery) filtered.add(item);
        }
        return filtered;
    }

    public void setSearchQuery(String query) {
        searchQuery = query;
    }

    public void setFilter(String category, String query) {
        selectedCategory = category;
        searchQuery = query;
    }

    public void toggleView() {
        isGridView = !isGridView;
        if (isGridView) {
            rvItems.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            rvItems.setAdapter(gridAdapter);
        } else {
            rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvItems.setAdapter(listAdapter);
        }
    }
}
