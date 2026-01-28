package com.inventorymanager.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventorymanager.app.adapter.CategoryChipAdapter;
import com.inventorymanager.app.database.ItemEntity;
import com.inventorymanager.app.util.ImageUtils;
import com.inventorymanager.app.viewmodel.ItemViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AddEditItemActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;

    private EditText etName, etQuantity, etPrice, etNotes;
    private RecyclerView rvCategoryChips;
    private ImageView ivPhoto;
    private ItemViewModel viewModel;
    private CategoryChipAdapter categoryChipAdapter;
    private long itemId = -1;
    private String currentPhotoPath;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        initViews();
        setupViewModel();

        // Check if editing existing item
        itemId = getIntent().getLongExtra("item_id", -1);
        if (itemId != -1) {
            setTitle("Edit Item");
            loadItemData();
        } else {
            setTitle("Add Item");
        }
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        etNotes = findViewById(R.id.etNotes);
        rvCategoryChips = findViewById(R.id.rvCategoryChips);
        ivPhoto = findViewById(R.id.ivPhoto);

        ivPhoto.setOnClickListener(v -> showImagePickerDialog());
        
        setupCategoryChips();
    }
    
    private void setupCategoryChips() {
        String[] categoriesArray = getResources().getStringArray(R.array.categories);
        List<String> categories = new ArrayList<>(Arrays.asList(categoriesArray));
        
        categoryChipAdapter = new CategoryChipAdapter(categories);
        rvCategoryChips.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategoryChips.setAdapter(categoryChipAdapter);
        
        categoryChipAdapter.setOnCategoryChipClickListener((category, position) -> {
            selectedCategory = category;
            categoryChipAdapter.setSelectedPosition(position);
        });
        
        // Select first category by default
        if (!categories.isEmpty()) {
            selectedCategory = categories.get(0);
            categoryChipAdapter.setSelectedPosition(0);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
    }

    private void loadItemData() {
        viewModel.getItemById(itemId).observe(this, item -> {
            if (item != null) {
                etName.setText(item.getName());
                etQuantity.setText(String.valueOf(item.getQuantity()));
                if (item.getPrice() != null) {
                    etPrice.setText(String.valueOf(item.getPrice()));
                }
                etNotes.setText(item.getNotes());

                // Set category chip
                selectedCategory = item.getCategory();
                String[] categoriesArray = getResources().getStringArray(R.array.categories);
                for (int i = 0; i < categoriesArray.length; i++) {
                    if (categoriesArray[i].equals(item.getCategory())) {
                        categoryChipAdapter.setSelectedPosition(i);
                        break;
                    }
                }

                // Load photo
                if (item.getPhotoPath() != null && !item.getPhotoPath().isEmpty()) {
                    currentPhotoPath = item.getPhotoPath();
                    ImageUtils.loadImage(this, ivPhoto, item.getPhotoPath());
                }
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = ImageUtils.createImageFile(this);
            if (photoFile != null) {
                currentPhotoPath = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                ImageUtils.loadImage(this, ivPhoto, currentPhotoPath);
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImage = data.getData();
                currentPhotoPath = ImageUtils.saveImageFromUri(this, selectedImage);
                ImageUtils.loadImage(this, ivPhoto, currentPhotoPath);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String category = selectedCategory != null ? selectedCategory : categoryChipAdapter.getSelectedCategory();
        
        if (category == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 1;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        Double price = null;
        if (!priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ItemEntity item = new ItemEntity();
        if (itemId != -1) {
            item.setId(itemId);
        }
        item.setName(name);
        item.setCategory(category);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setNotes(notes);
        item.setPhotoPath(currentPhotoPath);
        item.setPurchaseDate(new Date());

        if (itemId != -1) {
            viewModel.update(item);
        } else {
            viewModel.insert(item);
        }

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            saveItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
