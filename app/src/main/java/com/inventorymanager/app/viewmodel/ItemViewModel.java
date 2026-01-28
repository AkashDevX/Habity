package com.inventorymanager.app.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.inventorymanager.app.database.InventoryDatabase;
import com.inventorymanager.app.database.ItemDao;
import com.inventorymanager.app.database.ItemEntity;
import java.util.List;

public class ItemViewModel extends AndroidViewModel {
    private ItemDao itemDao;
    private LiveData<List<ItemEntity>> allItems;
    private LiveData<List<String>> allCategories;
    private MutableLiveData<String> selectedCategory = new MutableLiveData<>("All");
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private MutableLiveData<Boolean> toggleViewRequest = new MutableLiveData<>(false);

    public ItemViewModel(Application application) {
        super(application);
        InventoryDatabase database = InventoryDatabase.getInstance(application);
        itemDao = database.itemDao();
        allItems = itemDao.getAllItems();
        allCategories = itemDao.getAllCategories();
    }

    public LiveData<List<ItemEntity>> getAllItems() {
        return allItems;
    }

    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<ItemEntity>> getFilteredItems(String category, String query) {
        // For simplicity, return all items and filter in the adapter
        // In production, you could create a more complex DAO query
        return allItems;
    }

    public void setFilter(String category, String query) {
        if (category != null) selectedCategory.setValue(category);
        if (query != null) searchQuery.setValue(query);
    }

    public MutableLiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public MutableLiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public void requestToggleView() {
        toggleViewRequest.setValue(true);
    }

    public MutableLiveData<Boolean> getToggleViewRequest() {
        return toggleViewRequest;
    }

    public void clearToggleViewRequest() {
        toggleViewRequest.setValue(false);
    }

    public LiveData<Integer> getTotalItemCount() {
        return itemDao.getTotalItemCount();
    }

    public LiveData<Integer> getTotalCategoryCount() {
        return itemDao.getTotalCategoryCount();
    }

    public void insert(ItemEntity item) {
        new Thread(() -> itemDao.insert(item)).start();
    }

    public void update(ItemEntity item) {
        new Thread(() -> itemDao.update(item)).start();
    }

    public void delete(ItemEntity item) {
        new Thread(() -> itemDao.delete(item)).start();
    }

    public LiveData<ItemEntity> getItemById(long id) {
        return itemDao.getItemById(id);
    }
}
