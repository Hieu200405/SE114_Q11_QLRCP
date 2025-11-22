package com.example.myapplication.activities;

import static android.graphics.Color.*;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.helper.CustomMarkerView;
import com.example.myapplication.models.RevenueDay;

import com.example.myapplication.models.StatusMessage;
import com.example.myapplication.models.UserInfo;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiRevenueService;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.sql.DataTruncation;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminMainActivity extends AppCompatActivity {
    String accessToken;
    LocalDate today = LocalDate.now();
    LocalDate oneWeekAgo = today.minusDays(6);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    List<RevenueDay> revenueDays = new ArrayList<>();

    private Map<String, List<Float>> weeklyRevenue = new HashMap<>();
    LineChart lineChart;
    TextView tvRevenueToday, tvDateRange, tvTotalTickets;
    Button btnStartDate, btnEndDate;
    ImageView imageHistory, imageHome, imageManageUser, imageManageFirm, imageProfile, imageRefresh, imageManageRoom;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_main);
//        findViewById
        setFindIdElement();

//        Set access token from SharedPreferences
        accessToken = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getString("access_token", null);

        // load     api revenue
        refreshRevenueDay();


        // listen menu button
        listenerSetupMenuButton();


    }

    void listenerSetupMenuButton() {
        imageProfile.setOnClickListener(
                v -> {
                    startActivity(new android.content.Intent(AdminMainActivity.this, AdminActivityProfile.class));
                }
        );

        imageHistory.setOnClickListener(
                v -> {
                    startActivity(new android.content.Intent(AdminMainActivity.this, AdminActivityHistoryBooking.class));
                }
        );

        imageRefresh.setOnClickListener(
                v -> {
                    refreshRevenueDay();
                }
        );

        imageManageRoom.setOnClickListener(
                v -> {
                    startActivity(new android.content.Intent(AdminMainActivity.this, AdminActivityManageRoom.class));
                }
        );

        imageManageFirm.setOnClickListener(
                v -> {
                    startActivity(new android.content.Intent(AdminMainActivity.this, AdminActivityManageFirm.class));
                }
        );
        imageManageUser.setOnClickListener(
                v -> {
                    startActivity(new android.content.Intent(AdminMainActivity.this, AdminActivityManageUser.class));
                }
        );

        btnStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        btnStartDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        btnEndDate.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });



    }

    void setFindIdElement() {
        imageHistory = findViewById(R.id.imageHistory);
        imageHome = findViewById(R.id.imageHome);
        imageManageFirm = findViewById(R.id.imageManageFirm);
        imageManageUser = findViewById(R.id.imageManageUser);
        imageProfile = findViewById(R.id.imageProfile);
        lineChart = findViewById(R.id.lineChart);
        tvRevenueToday = findViewById(R.id.tvTotalRevenue);
        tvDateRange = findViewById(R.id.tvDateRange);
        imageRefresh = findViewById(R.id.imageRefresh);
        imageManageRoom = findViewById(R.id.imageManageRoom);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        tvTotalTickets = findViewById(R.id.tvTotalTickets);
    }








    void loadApiRevenueOneweekLastest(){
        String token = "Bearer " + accessToken;
        String todayStr = btnEndDate.getText().toString().trim();
        String oneWeekAgoStr = btnStartDate.getText().toString().trim();
        if (!isFormatDate(todayStr) || !isFormatDate(oneWeekAgoStr)) {
            // Nếu định dạng ngày không hợp lệ, sử dụng ngày mặc định
            todayStr = today.format(formatter);
            oneWeekAgoStr = oneWeekAgo.format(formatter);
        }

        ApiRevenueService apiRevenueService = ApiClient.getRetrofit().create(ApiRevenueService.class);
        Call<List<RevenueDay>> call = apiRevenueService.getMoreTotalDays(token, oneWeekAgoStr, todayStr);
        call.enqueue(new retrofit2.Callback<List<RevenueDay>>() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<List<RevenueDay>> call, retrofit2.Response<List<RevenueDay>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_RESPONSE", "Revenue days loaded successfully: " + response.body());

                    // Clear previous data
                    revenueDays.clear();
                    List <String> weekLabels = new ArrayList<>();
                    // Process the response data
                    for (RevenueDay revenueDay : response.body()) {
                        Log.d("Date", "RevenueDay: " + revenueDay.getDate() + ", Total: " + revenueDay.getTotalMoney());
                    }
                    // Add new data
                    revenueDays.addAll(response.body());
                    // Update the TextView with today's revenue
                    int size = revenueDays.size();


                    // Update the date range TextView
                    tvDateRange.setText("Doanh thu từ "  + revenueDays.get(0).getDate() + "/" + revenueDays.get(0).getMonth() +" đến "+ revenueDays.get(size-1).getDate() + "/" + revenueDays.get(size-1).getMonth());
                    showLineChart();
                } else {
                    Log.e("API_ERROR", "Response error: " + response.code());
                    // Handle the case where the response is not successful
                    Log.e("API_ERROR", "Failed to load revenue days: " + response.message());
                }

            }

            @Override
            public void onFailure(Call<List<RevenueDay>> call, Throwable t) {

            }
        });
    }

    private void showLineChart() {
        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        for (int i = 0; i < revenueDays.size(); i++) {
            entries.add(new Entry(i, revenueDays.get(i).getTotalMoney()));
            xLabels.add(String.valueOf(revenueDays.get(i).getDate()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.CYAN);
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(4f);
        dataSet.setCircleHoleColor(Color.CYAN);
        dataSet.setDrawValues(false);  // Tắt số hiển thị trên line
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        // Tắt tooltip khi chạm
        lineChart.setMarker(null);

        // Trục Y
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        // Trục X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(15f);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(xLabels.size());
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(0f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);

        // Tắt legend & description
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);

        // Background và animation
        lineChart.setBackgroundColor(Color.parseColor("#081029"));
        lineChart.setExtraOffsets(20f, 20f, 20f, 30f);
        lineChart.animateX(1200, Easing.EaseInOutCubic);


        // Gán marker view khi chạm vào điểm
        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.custom_marker_view);
        lineChart.setMarker(markerView);

        // Set data & vẽ
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }



    private void refreshRevenueDay(){
        String token = "Bearer " + accessToken;
        String day = String.valueOf(today.getDayOfMonth());
        String month = String.valueOf(today.getMonthValue());
        String year = String.valueOf(today.getYear());
        ApiRevenueService apiRevenueService = ApiClient.getRetrofit().create(ApiRevenueService.class);
        Call<RevenueDay> call = apiRevenueService.refreshTotalDay(token, day, month, year);
        call.enqueue(new Callback<RevenueDay>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<RevenueDay> call, retrofit2.Response<RevenueDay> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_RESPONSE", "Refresh revenue day successful: " + response.code());
                    RevenueDay refreshedRevenueDay = response.body();
                    tvRevenueToday.setText(String.valueOf(refreshedRevenueDay.getTotalMoney()) + " VNĐ");
                    tvTotalTickets.setText(String.valueOf(refreshedRevenueDay.getTotalTickets()));
                    // Cập nhật lại dữ liệu doanh thu hôm nay
                    loadApiRevenueOneweekLastest();
                } else {
                    Log.e("API_ERROR", "Failed to refresh revenue day: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RevenueDay> call, Throwable t) {
                Log.e("API_ERROR", "Error refreshing revenue day: " + t.getMessage());
            }
        });

    }


    Boolean isFormatDate(String date) {
        // Kiểm tra định dạng ngày tháng năm
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        return date.matches(regex);
    }

}
