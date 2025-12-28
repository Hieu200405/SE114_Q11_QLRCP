package com.example.myapplication.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.helper.LocationHelper;
import com.example.myapplication.models.PlaceAutocomplete;
import com.example.myapplication.network.ApiCinemaService;
import com.example.myapplication.network.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity để chọn vị trí trên bản đồ Goong
 * Sử dụng WebView để hiển thị bản đồ Goong
 */
public class MapPickerActivity extends AppCompatActivity {

    private static final String TAG = "MapPickerActivity";

    // Views
    private WebView webViewMap;
    private EditText etSearchPlace;
    private ImageView btnBack, btnClearSearch;
    private TextView tvSelectedAddress, tvSelectedCoordinates;
    private Button btnConfirmLocation;
    private FloatingActionButton fabMyLocation;
    private ProgressBar progressBar;

    // Data
    private double selectedLat = 0;
    private double selectedLng = 0;
    private String selectedAddress = "";
    private LocationHelper locationHelper;
    private ApiCinemaService apiCinemaService;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Flag to prevent TextWatcher loop
    private boolean isUpdatingSearchText = false;

    // Goong API Key
    private static final String GOONG_MAP_KEY = BuildConfig.GOONG_MAP_KEY;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        initViews();
        initApi();
        initLocationHelper();
        setupListeners();
        setupWebView();

        // Lấy tọa độ ban đầu từ intent (nếu có)
        double initLat = getIntent().getDoubleExtra("latitude", 0);
        double initLng = getIntent().getDoubleExtra("longitude", 0);
        if (initLat != 0 && initLng != 0) {
            selectedLat = initLat;
            selectedLng = initLng;
        } else {
            // Mặc định: TP.HCM
            selectedLat = 10.7769;
            selectedLng = 106.7009;
        }

        loadMap();
    }

    private void initViews() {
        webViewMap = findViewById(R.id.webViewMap);
        etSearchPlace = findViewById(R.id.etSearchPlace);
        btnBack = findViewById(R.id.buttonBack);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        tvSelectedAddress = findViewById(R.id.tvSelectedAddress);
        tvSelectedCoordinates = findViewById(R.id.tvSelectedCoordinates);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initApi() {
        apiCinemaService = ApiClient.getRetrofit().create(ApiCinemaService.class);
    }

    private void initLocationHelper() {
        locationHelper = new LocationHelper(this);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            etSearchPlace.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        btnConfirmLocation.setOnClickListener(v -> confirmLocation());

        fabMyLocation.setOnClickListener(v -> moveToMyLocation());

        // Search với debounce
        etSearchPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Skip if we're programmatically setting the text
                if (isUpdatingSearchText) {
                    return;
                }

                // Debounce search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (s.length() >= 3) {
                    searchRunnable = () -> searchPlaces(s.toString());
                    searchHandler.postDelayed(searchRunnable, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webViewMap.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // Add JavaScript interface để nhận tọa độ từ map
        webViewMap.addJavascriptInterface(new MapJsInterface(), "Android");

        webViewMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                showLoading(false);
            }
        });
    }

    private void loadMap() {
        showLoading(true);

        String html = generateMapHtml(selectedLat, selectedLng, 15);
        webViewMap.loadDataWithBaseURL("https://tiles.goong.io", html, "text/html", "UTF-8", null);

        updateSelectedLocationDisplay();
    }

    private String generateMapHtml(double lat, double lng, int zoom) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8' />\n" +
                "    <meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />\n" +
                "    <script src='https://cdn.jsdelivr.net/npm/@goongmaps/goong-js@1.0.9/dist/goong-js.js'></script>\n" +
                "    <link href='https://cdn.jsdelivr.net/npm/@goongmaps/goong-js@1.0.9/dist/goong-js.css' rel='stylesheet' />\n" +
                "    <style>\n" +
                "        body { margin: 0; padding: 0; }\n" +
                "        #map { position: absolute; top: 0; bottom: 0; width: 100%; }\n" +
                "        .center-marker {\n" +
                "            position: absolute;\n" +
                "            top: 50%;\n" +
                "            left: 50%;\n" +
                "            transform: translate(-50%, -100%);\n" +
                "            z-index: 1000;\n" +
                "            font-size: 40px;\n" +
                "            color: #E53935;\n" +
                "            text-shadow: 0 2px 4px rgba(0,0,0,0.3);\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='map'></div>\n" +
                "    <div class='center-marker'>📍</div>\n" +
                "    <script>\n" +
                "        goongjs.accessToken = '" + GOONG_MAP_KEY + "';\n" +
                "        var map = new goongjs.Map({\n" +
                "            container: 'map',\n" +
                "            style: 'https://tiles.goong.io/assets/goong_map_web.json',\n" +
                "            center: [" + lng + ", " + lat + "],\n" +
                "            zoom: " + zoom + "\n" +
                "        });\n" +
                "\n" +
                "        map.on('moveend', function() {\n" +
                "            var center = map.getCenter();\n" +
                "            Android.onLocationChanged(center.lat, center.lng);\n" +
                "        });\n" +
                "\n" +
                "        function moveToLocation(lat, lng) {\n" +
                "            map.flyTo({ center: [lng, lat], zoom: 16 });\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    // JavaScript Interface để nhận callback từ WebView
    private class MapJsInterface {
        @JavascriptInterface
        public void onLocationChanged(double lat, double lng) {
            runOnUiThread(() -> {
                selectedLat = lat;
                selectedLng = lng;
                updateSelectedLocationDisplay();
                reverseGeocode(lat, lng);
            });
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateSelectedLocationDisplay() {
        tvSelectedCoordinates.setText(String.format(java.util.Locale.US, "%.6f, %.6f", selectedLat, selectedLng));
    }

    private void reverseGeocode(double lat, double lng) {
        apiCinemaService.reverseGeocode(lat, lng).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> data = response.body();
                        if (data.containsKey("formatted_address")) {
                            selectedAddress = (String) data.get("formatted_address");
                            tvSelectedAddress.setText(selectedAddress);
                        } else {
                            tvSelectedAddress.setText("Không tìm thấy địa chỉ");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing reverse geocode", e);
                        tvSelectedAddress.setText("Đang tải...");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Reverse geocode failed", t);
            }
        });
    }

    private void searchPlaces(String query) {
        apiCinemaService.searchPlaces(query, selectedLat, selectedLng)
                .enqueue(new Callback<List<PlaceAutocomplete.Prediction>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PlaceAutocomplete.Prediction>> call,
                                           @NonNull Response<List<PlaceAutocomplete.Prediction>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            showPlaceResults(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<PlaceAutocomplete.Prediction>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Search places failed", t);
                    }
                });
    }

    private void showPlaceResults(List<PlaceAutocomplete.Prediction> predictions) {
        String[] items = new String[predictions.size()];
        for (int i = 0; i < predictions.size(); i++) {
            items[i] = predictions.get(i).getDescription();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn địa điểm")
                .setItems(items, (dialog, which) -> {
                    PlaceAutocomplete.Prediction selected = predictions.get(which);

                    // Set flag to prevent TextWatcher from triggering
                    isUpdatingSearchText = true;
                    etSearchPlace.setText(selected.getDescription());
                    isUpdatingSearchText = false;

                    getPlaceDetail(selected.getPlaceId());
                })
                .show();
    }

    private void getPlaceDetail(String placeId) {
        showLoading(true);
        apiCinemaService.getPlaceDetail(placeId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                showLoading(false);
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
                                    moveCameraTo(lat, lng);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing place detail", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Get place detail failed", t);
            }
        });
    }

    private void moveCameraTo(double lat, double lng) {
        selectedLat = lat;
        selectedLng = lng;
        webViewMap.evaluateJavascript("moveToLocation(" + lat + ", " + lng + ");", null);
        updateSelectedLocationDisplay();
    }

    private void moveToMyLocation() {
        if (!locationHelper.hasLocationPermission()) {
            locationHelper.requestLocationPermission(this);
            return;
        }

        if (!locationHelper.isLocationEnabled()) {
            Toast.makeText(this, "Vui lòng bật dịch vụ vị trí", Toast.LENGTH_SHORT).show();
            locationHelper.openLocationSettings(this);
            return;
        }

        showLoading(true);
        locationHelper.getCurrentLocation(new LocationHelper.LocationResultListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                showLoading(false);
                moveCameraTo(latitude, longitude);
            }

            @Override
            public void onLocationError(String error) {
                showLoading(false);
                Toast.makeText(MapPickerActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmLocation() {
        if (selectedLat == 0 && selectedLng == 0) {
            Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", selectedLat);
        resultIntent.putExtra("longitude", selectedLng);
        resultIntent.putExtra("address", selectedAddress);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (LocationHelper.handlePermissionResult(requestCode, grantResults)) {
            moveToMyLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webViewMap != null) {
            webViewMap.destroy();
        }
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
    }
}
