package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class BroadcastFirm implements Parcelable {

    @SerializedName("FirmID")
    private int FirmID;

    @SerializedName("ID")
    private int ID;

    @SerializedName("DateBroadcast")
    private String DateBroadcast;

    @SerializedName("TimeBroadcast")
    private String TimeBroadcast;

    @SerializedName("RoomID")
    private int RoomID;

    @SerializedName("Seats")
    private int Seats;

    @SerializedName("Price")
    private double Price;

    protected BroadcastFirm(Parcel in) {
        FirmID = in.readInt();
        ID = in.readInt();
        DateBroadcast = in.readString();
        TimeBroadcast = in.readString();
        RoomID = in.readInt();
        Seats = in.readInt();
        Price = in.readDouble();
    }
    public static final Creator<BroadcastFirm> CREATOR = new Creator<BroadcastFirm>() {
        @Override
        public BroadcastFirm createFromParcel(Parcel in) {
            return new BroadcastFirm(in);
        }

        @Override
        public BroadcastFirm[] newArray(int size) {
            return new BroadcastFirm[size];
        }
    };
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(FirmID);
        dest.writeInt(ID);
        dest.writeString(DateBroadcast);
        dest.writeString(TimeBroadcast);
        dest.writeInt(RoomID);
        dest.writeInt(Seats);
        dest.writeDouble(Price);
    }

//    getters
    public int getFirmID() {
        return FirmID;
    }

    public int getID() {
        return ID;
    }

    public String getDateBroadcast() {
        return DateBroadcast;
    }

    public String getTimeBroadcast() {
        return TimeBroadcast;
    }

    public int getRoomID() {
        return RoomID;
    }

    public int getSeats() {
        return Seats;
    }

    public double getPrice() {
        return Price;
    }


    @Override
    public int describeContents() {
        return 0;
    }


}
