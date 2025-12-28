package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CinemaAdapter;
import com.example.myapplication.cacheModels.CinemaCache;
import com.example.myapplication.helper.LocationHelper;
import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.NearbyCinemaRequest;
import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.network.ApiCinemaService;
import com.example.myapplication.network.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity để hiển thị danh sách rạp chiếu phim
 * - Hiển thị danh sách rạp gần nhất dựa trên vị trí người dùng
 * - Tính khoảng cách thực tế bằng Goong Distance Matrix API
 * - Admin có thể thêm/sửa/xóa rạp
 */
public class CinemaListActivity extends AppCompatActivity implements CinemaAdapter.OnCinemaClickListener {

    private static final String TAG = "CinemaListActivity";

    // Views
    private RecyclerView rvCinemas;
    private EditText etSearch;
    private ImageView btnMyLocation;
    private TextView tvUserLocation;
    private LinearLayout layoutEmpty, layoutBottomMenu;
    private TextView tvEmptyMessage;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddCinema;
    private ImageView btnBack;
    private TextView tvTitle;

    // Menu bar
    private ImageView imageHome, imageManageFilm, imageManageRoom, imageManageCinema, imageManageUser, imageProfile;

    // Adapter
    private CinemaAdapter cinemaAdapter;
    private List<Cinema> allCinemas = new ArrayList<>();
    private List<Cinema> filteredCinemas = new ArrayList<>();

    // Location
    private LocationHelper locationHelper;
    private double userLat = 0;
    private double userLng = 0;

    // API
    private ApiCinemaService apiCinemaService;

    // Mode
    private boolean isAdminMode = false;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_list);

        // Get mode from intent
        isAdminMode = getIntent().getBooleanExtra("isAdminMode", false);
        authToken = getIntent().getStringExtra("token");

        initViews();
        initApi();
        initLocationHelper();
        setupRecyclerView();
        setupListeners();

        // Load cinemas
        loadCinemas();
    }

    private void initViews() {
        rvCinemas = findViewById(R.id.rvCinemas);
        etSearch = findViewById(R.id.etSearch);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        tvUserLocation = findViewById(R.id.tvUserLocation);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        progressBar = findViewById(R.id.progressBar);
        fabAddCinema = findViewById(R.id.fabAddCinema);

        // Toolbar
        View toolbar = findViewById(R.id.toolbar);
        btnBack = toolbar.findViewById(R.id.buttonBack);
        tvTitle = toolbar.findViewById(R.id.titleToolbar);
        tvTitle.setText("Danh sách rạp chiếu phim");

        // Menu bar
        layoutBottomMenu = findViewById(R.id.layoutBottomMenu);
        imageHome = findViewById(R.id.imageHome);
        imageManageFilm = findViewById(R.id.imageManageFilm);
        imageManageRoom = findViewById(R.id.imageManageRoom);
        imageManageCinema = findViewById(R.id.imageManageCinema);
        imageManageUser = findViewById(R.id.imageManageUser);
        imageProfile = findViewById(R.id.imageProfile);

        // Show FAB and menu for admin only
        if (isAdminMode) {
            fabAddCinema.setVisibility(View.VISIBLE);
            layoutBottomMenu.setVisibility(View.VISIBLE);
        } else {
            layoutBottomMenu.setVisibility(View.GONE);
        }
    }

    private void initApi() {
        apiCinemaService = ApiClient.getRetrofit().create(ApiCinemaService.class);
    }

    private void initLocationHelper() {
        locationHelper = new LocationHelper(this);
    }

    private void setupRecyclerView() {
        cinemaAdapter = new CinemaAdapter(filteredCinemas, isAdminMode);
        cinemaAdapter.setOnCinemaClickListener(this);
        rvCinemas.setLayoutManager(new LinearLayoutManager(this));
        rvCinemas.setAdapter(cinemaAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMyLocation.setOnClickListener(v -> getCurrentLocation());

        fabAddCinema.setOnClickListener(v -> {
            Intent intent = new Intent(this, CinemaFormActivity.class);
            intent.putExtra("isEditMode", false);
            intent.putExtra("token", authToken);
            startActivity(intent);
        });

        // Search filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCinemas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Menu bar listeners (only for admin)
        if (isAdminMode) {
            setupMenuListeners();
        }
    }

    private void setupMenuListeners() {
        imageHome.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminMainActivity.class));
        });

        imageManageFilm.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivityManageFilm.class));
        });

        imageManageRoom.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivityManageRoom.class));
        });

        // imageManageCinema is current screen - no action needed

        imageManageUser.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivityManageUser.class));
        });

        imageProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivityProfile.class));
        });
    }

    private void getCurrentLocation() {
        if (!locationHelper.hasLocationPermission()) {
            locationHelper.requestLocationPermission(this);
            return;
        }

        if (!locationHelper.isLocationEnabled()) {
            Toast.makeText(this, "Vui lòng bật dịch vụ vị trí", Toast.LENGTH_SHORT).show();
            locationHelper.openLocationSettings(this);
            return;
        }

        tvUserLocation.setVisibility(View.VISIBLE);
        tvUserLocation.setText("Đang xác định vị trí...");

        locationHelper.getCurrentLocation(new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                userLat = latitude;
                userLng = longitude;
                tvUserLocation.setText(String.format("Vị trí của bạn: %.6f, %.6f", latitude, longitude));

                // Reload cinemas with location
                loadNearbyCinemas();
            }

            @Override
            public void onLocationError(String error) {
                tvUserLocation.setText("Không thể xác định vị trí");
                Toast.makeText(CinemaListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCinemas() {
        showLoading(true);

        apiCinemaService.getAllCinemas().enqueue(new Callback<List<Cinema>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Cinema>> call, @NonNull Response<List<Cinema>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allCinemas = response.body();
                    filteredCinemas.clear();
                    filteredCinemas.addAll(allCinemas);
                    cinemaAdapter.notifyDataSetChanged();
                    updateEmptyState();

                    // Try to get user location for distance calculation
                    getCurrentLocation();
                } else {
                    showError("Không thể tải danh sách rạp");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cinema>> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Error loading cinemas", t);
            }
        });
    }

    private void loadNearbyCinemas() {
        if (userLat == 0 && userLng == 0) {
            return;
        }

        showLoading(true);

        NearbyCinemaRequest request = new NearbyCinemaRequest(userLat, userLng, 50, true);
        apiCinemaService.getNearbyCinemas(request).enqueue(new Callback<List<Cinema>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Cinema>> call, @NonNull Response<List<Cinema>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    allCinemas = response.body();
                    filteredCinemas.clear();
                    filteredCinemas.addAll(allCinemas);
                    cinemaAdapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    showError("Không thể tải danh sách rạp gần đây");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Cinema>> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Error loading nearby cinemas", t);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterCinemas(String query) {
        filteredCinemas.clear();
        if (query.isEmpty()) {
            filteredCinemas.addAll(allCinemas);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Cinema cinema : allCinemas) {
                if (cinema.getName().toLowerCase().contains(lowerQuery) ||
                    cinema.getAddress().toLowerCase().contains(lowerQuery)) {
                    filteredCinemas.add(cinema);
                }
            }
        }
        cinemaAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredCinemas.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvCinemas.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvCinemas.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // CinemaAdapter.OnCinemaClickListener implementation
    @Override
    public void onCinemaClick(Cinema cinema) {
        // Open cinema detail or select for booking
        Intent intent = new Intent(this, CinemaDetailActivity.class);
        intent.putExtra("cinema", cinema);
        intent.putExtra("userLat", userLat);
        intent.putExtra("userLng", userLng);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Cinema cinema) {
        if (!isAdminMode) return;

        Intent intent = new Intent(this, CinemaFormActivity.class);
        intent.putExtra("isEditMode", true);
        intent.putExtra("cinema", cinema);
        intent.putExtra("token", authToken);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Cinema cinema) {
        if (!isAdminMode) return;

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa rạp \"" + cinema.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCinema(cinema))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onNavigateClick(Cinema cinema) {
        locationHelper.openNavigationApp(this, cinema.getLatitude(), cinema.getLongitude(), cinema.getName());
    }

    private void deleteCinema(Cinema cinema) {
        if (authToken == null) {
            showError("Vui lòng đăng nhập lại");
            return;
        }

        showLoading(true);
        apiCinemaService.deleteCinema("Bearer " + authToken, cinema.getId())
                .enqueue(new Callback<StatusMessage>() {
                    @Override
                    public void onResponse(@NonNull Call<StatusMessage> call, @NonNull Response<StatusMessage> response) {
                        showLoading(false);
                        if (response.isSuccessful()) {
                            CinemaCache.clearCache(); // Clear cache to reload updated data
                            Toast.makeText(CinemaListActivity.this, "Đã xóa rạp thành công", Toast.LENGTH_SHORT).show();
                            loadCinemas(); // Reload list
                        } else {
                            showError("Không thể xóa rạp");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StatusMessage> call, @NonNull Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when coming back from form
        if (userLat != 0 && userLng != 0) {
            loadNearbyCinemas();
        } else {
            loadCinemas();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (LocationHelper.handlePermissionResult(requestCode, grantResults)) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Cần cấp quyền vị trí để hiển thị rạp gần nhất", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }
}

