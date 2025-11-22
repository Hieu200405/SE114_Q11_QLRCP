package com.example.myapplication.network;



import android.net.http.UrlRequest;

import com.example.myapplication.models.RevenueDay;
import com.example.myapplication.models.StatusMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;


/*
    {
        "Date": "14-06-2025",
        "ID": 2,
        "TotalMoney": 500000.0
    }
 */
public interface ApiRevenueService {

    @GET("total_day/get/more-total-days")
    Call<List<RevenueDay>> getMoreTotalDays(
            @Header("Authorization") String token,
            @Query("start_date") String startDate,
            @Query("end_date") String endDate
    );

    @POST("total_day/refresh")
    Call<RevenueDay> refreshTotalDay(
            @Header("Authorization") String token,
            @Query("day") String day,
            @Query("month") String month,
            @Query("year") String year
    );
}
