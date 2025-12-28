package com.example.myapplication.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.cacheModels.CinemaCache;
import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.CinemaRequest;
import com.example.myapplication.models.PlaceAutocomplete;
import com.example.myapplication.network.ApiCinemaService;
import com.example.myapplication.network.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity để tạo/chỉnh sửa rạp chiếu phim
 * - Tích hợp Goong Places API để tìm kiếm địa chỉ
 * - Chọn vị trí trên bản đồ
 * - Upload ảnh qua Cloudinary
 */
public class CinemaFormActivity extends AppCompatActivity {

    private static final String TAG = "CinemaFormActivity";

    // Views
    private ImageView ivCinemaImage, btnUploadImage, btnBack;
    private Button btnOpenMap;
    private TextView tvTitle;
    private EditText etCinemaName, etAddress, etLatitude, etLongitude, etPhone, etDescription;
    private Button btnCancel, btnSave;
    private ProgressBar progressBar;

    // Data
    private ApiCinemaService apiCinemaService;
    private String authToken;
    private boolean isEditMode = false;
    private Cinema currentCinema;
    private String selectedImageUrl = "";
    private Uri selectedImageUri = null;

    // Flag to prevent TextWatcher loop
    private boolean isUpdatingAddress = false;

    // Image picker launcher
    private ActivityResultLauncher<String> pickImageLauncher;

    // Map picker launcher
    private ActivityResultLauncher<Intent> mapPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_form);

        // Get data from intent
        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        authToken = getIntent().getStringExtra("token");
        if (isEditMode) {
            currentCinema = getIntent().getParcelableExtra("cinema");
        }

        initViews();
        initApi();
        setupLaunchers();
        setupListeners();

        if (isEditMode && currentCinema != null) {
            populateData();
        }
    }

    private void initViews() {
        ivCinemaImage = findViewById(R.id.ivCinemaImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        etCinemaName = findViewById(R.id.etCinemaName);
        etAddress = findViewById(R.id.etAddress);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etPhone = findViewById(R.id.etPhone);
        etDescription = findViewById(R.id.etDescription);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        // Toolbar
        View toolbar = findViewById(R.id.toolbar);
        btnBack = toolbar.findViewById(R.id.buttonBack);
        tvTitle = toolbar.findViewById(R.id.titleToolbar);
        tvTitle.setText(isEditMode ? "Chỉnh sửa rạp" : "Thêm rạp mới");
    }

    private void initApi() {
        apiCinemaService = ApiClient.getRetrofit().create(ApiCinemaService.class);
    }

    private void setupLaunchers() {
        // Image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this)
                                .load(uri)
                                .centerCrop()
                                .into(ivCinemaImage);
                    }
                }
        );

        // Map picker
        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        double lat = data.getDoubleExtra("latitude", 0);
                        double lng = data.getDoubleExtra("longitude", 0);
                        String address = data.getStringExtra("address");

                        etLatitude.setText(String.format(java.util.Locale.US, "%.6f", lat));
                        etLongitude.setText(String.format(java.util.Locale.US, "%.6f", lng));

                        if (address != null && !address.isEmpty()) {
                            // Set flag to prevent TextWatcher from triggering
                            isUpdatingAddress = true;
                            etAddress.setText(address);
                            isUpdatingAddress = false;
                        }
                    }
                }
        );
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveCinema());

        // Upload image
        btnUploadImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Open map picker
        if (btnOpenMap != null) {
            btnOpenMap.setOnClickListener(v -> openMapPicker());
        }

        // Make coordinates NOT editable directly, only via map picker
        etLatitude.setFocusable(false);
        etLatitude.setClickable(true);
        etLongitude.setFocusable(false);
        etLongitude.setClickable(true);

        // Make coordinates clickable to open map
        View.OnClickListener openMapListener = v -> openMapPicker();
        etLatitude.setOnClickListener(openMapListener);
        etLongitude.setOnClickListener(openMapListener);

        // Address autocomplete
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Skip if we're programmatically setting the address
                if (isUpdatingAddress) {
                    return;
                }

                if (s.length() >= 3) {
                    searchPlaces(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openMapPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);

        // Truyền tọa độ hiện tại nếu có
        String latStr = etLatitude.getText().toString().trim();
        String lngStr = etLongitude.getText().toString().trim();
        if (!latStr.isEmpty() && !lngStr.isEmpty()) {
            try {
                intent.putExtra("latitude", Double.parseDouble(latStr));
                intent.putExtra("longitude", Double.parseDouble(lngStr));
            } catch (NumberFormatException ignored) {}
        }

        mapPickerLauncher.launch(intent);
    }

    private void populateData() {
        if (currentCinema == null) return;

        etCinemaName.setText(currentCinema.getName());

        // Set flag to prevent TextWatcher from triggering
        isUpdatingAddress = true;
        etAddress.setText(currentCinema.getAddress());
        isUpdatingAddress = false;

        etLatitude.setText(String.format(java.util.Locale.US, "%.6f", currentCinema.getLatitude()));
        etLongitude.setText(String.format(java.util.Locale.US, "%.6f", currentCinema.getLongitude()));
        etPhone.setText(currentCinema.getPhone());
        etDescription.setText(currentCinema.getDescription());

        if (currentCinema.getImageUrl() != null && !currentCinema.getImageUrl().isEmpty()) {
            selectedImageUrl = currentCinema.getImageUrl();
            Glide.with(this)
                    .load(currentCinema.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(ivCinemaImage);
        }
    }

    private void searchPlaces(String keyword) {
        apiCinemaService.searchPlaces(keyword, null, null)
                .enqueue(new retrofit2.Callback<List<PlaceAutocomplete.Prediction>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<List<PlaceAutocomplete.Prediction>> call,
                                           @NonNull retrofit2.Response<List<PlaceAutocomplete.Prediction>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            showPlaceSuggestions(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<List<PlaceAutocomplete.Prediction>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error searching places", t);
                    }
                });
    }

    private void showPlaceSuggestions(List<PlaceAutocomplete.Prediction> predictions) {
        if (predictions.isEmpty()) return;

        String[] items = new String[predictions.size()];
        for (int i = 0; i < predictions.size(); i++) {
            items[i] = predictions.get(i).getDescription();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn địa chỉ")
                .setItems(items, (dialog, which) -> {
                    PlaceAutocomplete.Prediction selected = predictions.get(which);

                    // Set flag to prevent TextWatcher from triggering
                    isUpdatingAddress = true;
                    etAddress.setText(selected.getDescription());
                    isUpdatingAddress = false;

                    getPlaceDetail(selected.getPlaceId());
                })
                .show();
    }

    private void getPlaceDetail(String placeId) {
        apiCinemaService.getPlaceDetail(placeId)
                .enqueue(new retrofit2.Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Map<String, Object>> call,
                                           @NonNull retrofit2.Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                Map<String, Object> data = response.body();
                                if (data.containsKey("geometry")) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> geometry = (Map<String, Object>) data.get("geometry");
                                    if (geometry != null && geometry.containsKey("location")) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                                        if (location != null) {
                                            double lat = ((Number) location.get("lat")).doubleValue();
                                            double lng = ((Number) location.get("lng")).doubleValue();
                                            etLatitude.setText(String.format(java.util.Locale.US, "%.6f", lat));
                                            etLongitude.setText(String.format(java.util.Locale.US, "%.6f", lng));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing place detail", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Map<String, Object>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error getting place detail", t);
                    }
                });
    }

    private void saveCinema() {
        // Validate input
        String name = etCinemaName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String latStr = etLatitude.getText().toString().trim();
        String lngStr = etLongitude.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            etCinemaName.setError("Vui lòng nhập tên rạp");
            etCinemaName.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Vui lòng nhập địa chỉ");
            etAddress.requestFocus();
            return;
        }

        if (latStr.isEmpty() || lngStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude, longitude;
        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lngStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Tọa độ không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

// Nếu có ảnh mới, upload trước
        if (selectedImageUri != null) {
            uploadImageToCloudinary(selectedImageUri, imageUrl -> {
                selectedImageUrl = imageUrl;
                submitCinemaRequest(name, address, latitude, longitude, phone, description);
            });
        } else {
            submitCinemaRequest(name, address, latitude, longitude, phone, description);
        }
    }

    private void submitCinemaRequest(String name, String address, double latitude, double longitude,
                                      String phone, String description) {
        CinemaRequest request = new CinemaRequest(name, address, latitude, longitude, phone, selectedImageUrl, description);

        if (isEditMode && currentCinema != null) {
            updateCinema(request);
        } else {
            createCinema(request);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri, Consumer<String> onSuccess) {
        String cloudName = "dfu6ly3og";
        String uploadPreset = "android_upload";
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] imageBytes = byteBuffer.toByteArray();
            inputStream.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "cinema_image.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(CinemaFormActivity.this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            String imageUrl = jsonObject.getString("secure_url");
                            Log.d(TAG, "Uploaded image URL: " + imageUrl);
                            runOnUiThread(() -> onSuccess.accept(imageUrl));
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(CinemaFormActivity.this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(CinemaFormActivity.this, "Upload ảnh thất bại", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createCinema(CinemaRequest request) {
        apiCinemaService.createCinema("Bearer " + authToken, request)
                .enqueue(new retrofit2.Callback<Cinema>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Cinema> call, @NonNull retrofit2.Response<Cinema> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            CinemaCache.clearCache(); // Clear cache to reload updated data
                            Toast.makeText(CinemaFormActivity.this, "Tạo rạp thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(CinemaFormActivity.this, "Không thể tạo rạp", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Cinema> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(CinemaFormActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error creating cinema", t);
                    }
                });
    }

    private void updateCinema(CinemaRequest request) {
        apiCinemaService.updateCinema("Bearer " + authToken, currentCinema.getId(), request)
                .enqueue(new retrofit2.Callback<Cinema>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Cinema> call, @NonNull retrofit2.Response<Cinema> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            CinemaCache.clearCache(); // Clear cache to reload updated data
                            Toast.makeText(CinemaFormActivity.this, "Cập nhật rạp thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(CinemaFormActivity.this, "Không thể cập nhật rạp", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Cinema> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(CinemaFormActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating cinema", t);
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}

