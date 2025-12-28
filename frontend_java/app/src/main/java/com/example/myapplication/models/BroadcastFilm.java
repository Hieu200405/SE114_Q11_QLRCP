package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class BroadcastFilm implements Parcelable {

    @SerializedName("FilmID")
    private int FilmID;

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

    // Cinema info (loaded separately from Room -> Cinema)
    // NOT transient - data must be preserved
    private String cinemaName;
    private String cinemaAddress;
    private String distanceText;
    private String durationText;
    private int cinemaId;
    private Double cinemaLatitude;
    private Double cinemaLongitude;

    protected BroadcastFilm(Parcel in) {
        FilmID = in.readInt();
        ID = in.readInt();
        DateBroadcast = in.readString();
        TimeBroadcast = in.readString();
        RoomID = in.readInt();
        Seats = in.readInt();
        Price = in.readDouble();
        cinemaName = in.readString();
        cinemaAddress = in.readString();
        distanceText = in.readString();
        durationText = in.readString();
        cinemaId = in.readInt();
        cinemaLatitude = (Double) in.readSerializable();
        cinemaLongitude = (Double) in.readSerializable();
    }
    public static final Creator<BroadcastFilm> CREATOR = new Creator<BroadcastFilm>() {
        @Override
        public BroadcastFilm createFromParcel(Parcel in) {
            return new BroadcastFilm(in);
        }

        @Override
        public BroadcastFilm[] newArray(int size) {
            return new BroadcastFilm[size];
        }
    };
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(FilmID);
        dest.writeInt(ID);
        dest.writeString(DateBroadcast);
        dest.writeString(TimeBroadcast);
        dest.writeInt(RoomID);
        dest.writeInt(Seats);
        dest.writeDouble(Price);
        dest.writeString(cinemaName);
        dest.writeString(cinemaAddress);
        dest.writeString(distanceText);
        dest.writeString(durationText);
        dest.writeInt(cinemaId);
        dest.writeSerializable(cinemaLatitude);
        dest.writeSerializable(cinemaLongitude);
    }

//    getters
    public int getFilmID() {
        return FilmID;
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

    // Cinema getters/setters
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }

    public String getCinemaAddress() { return cinemaAddress; }
    public void setCinemaAddress(String cinemaAddress) { this.cinemaAddress = cinemaAddress; }

    public String getDistanceText() { return distanceText; }
    public void setDistanceText(String distanceText) { this.distanceText = distanceText; }

    public String getDurationText() { return durationText; }
    public void setDurationText(String durationText) { this.durationText = durationText; }

    public int getCinemaId() { return cinemaId; }
    public void setCinemaId(int cinemaId) { this.cinemaId = cinemaId; }

    public Double getCinemaLatitude() { return cinemaLatitude; }
    public void setCinemaLatitude(Double cinemaLatitude) { this.cinemaLatitude = cinemaLatitude; }

    public Double getCinemaLongitude() { return cinemaLongitude; }
    public void setCinemaLongitude(Double cinemaLongitude) { this.cinemaLongitude = cinemaLongitude; }


    @Override
    public int describeContents() {
        return 0;
    }


}
