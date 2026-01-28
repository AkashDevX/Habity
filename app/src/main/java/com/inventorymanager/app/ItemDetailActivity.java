package com.inventorymanager.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.inventorymanager.app.database.ItemEntity;
import com.inventorymanager.app.util.ImageUtils;
import com.inventorymanager.app.viewmodel.ItemViewModel;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ItemDetailActivity extends AppCompatActivity {
    private ImageView ivPhoto;
    private TextView tvName, tvCategory, tvQuantity, tvPrice, tvPurchaseDate, tvNotes;
    private ItemViewModel viewModel;
    private long itemId;
    private ItemEntity currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        itemId = getIntent().getLongExtra("item_id", -1);
        if (itemId == -1) {
            finish();
            return;
        }

        initViews();
        setupViewModel();
    }

    private void initViews() {
        ivPhoto = findViewById(R.id.ivPhoto);
        tvName = findViewById(R.id.tvName);
        tvCategory = findViewById(R.id.tvCategory);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvPrice = findViewById(R.id.tvPrice);
        tvPurchaseDate = findViewById(R.id.tvPurchaseDate);
        tvNotes = findViewById(R.id.tvNotes);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        viewModel.getItemById(itemId).observe(this, item -> {
            if (item != null) {
                currentItem = item;
                displayItem(item);
            }
        });
    }

    private void displayItem(ItemEntity item) {
        tvName.setText(item.getName());
        tvCategory.setText("Category: " + item.getCategory());
        tvQuantity.setText("Quantity: " + item.getQuantity());

        if (item.getPrice() != null) {
            tvPrice.setText("Price: $" + String.format(Locale.getDefault(), "%.2f", item.getPrice()));
        } else {
            tvPrice.setText("Price: Not specified");
        }

        if (item.getPurchaseDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvPurchaseDate.setText("Purchase Date: " + sdf.format(item.getPurchaseDate()));
        } else {
            tvPurchaseDate.setText("Purchase Date: Not specified");
        }

        if (item.getNotes() != null && !item.getNotes().isEmpty()) {
            tvNotes.setText(item.getNotes());
        } else {
            tvNotes.setText("No notes");
        }

        if (item.getPhotoPath() != null && !item.getPhotoPath().isEmpty()) {
            ImageUtils.loadImage(this, ivPhoto, item.getPhotoPath());
        }
    }

    private void editItem() {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra("item_id", itemId);
        startActivity(intent);
    }

    private void deleteItem() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentItem != null) {
                        viewModel.delete(currentItem);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            editItem();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            deleteItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
