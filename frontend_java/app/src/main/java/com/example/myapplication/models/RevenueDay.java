package com.example.myapplication.models;


import com.google.gson.annotations.SerializedName;

/*
     {
    "Date": 25,
    "ID": 17,
    "Month": 6,
    "TotalMoney": 300000.0,
    "TotalTickets": 3,
    "Year": 2025
}
 */
public class
RevenueDay {


    @SerializedName("Date")
    int date;
    @SerializedName("Month")
    int month;
    @SerializedName("Year")
    int year;

    @SerializedName("TotalTickets")
    int TotalTickets;

    @SerializedName("ID")
    int ID;

    @SerializedName("TotalMoney")
    float TotalMoney;

    public RevenueDay(int date, int month, int year, int ID, float totalMoney, int totalTickets) {
        this.date = date;
        this.month = month;
        this.year = year;
        this.ID = ID;
        TotalMoney = totalMoney;
        TotalTickets = totalTickets;
    }

//    getter
    public int getDate() {
        return date;
    }
    public int getMonth() {
        return month;
    }
    public int getYear() {
        return year;
    }
    public int getID() {
        return ID;
    }
    public float getTotalMoney() {
        return TotalMoney;
    }
    public int getTotalTickets() {
        return TotalTickets;
    }



}
