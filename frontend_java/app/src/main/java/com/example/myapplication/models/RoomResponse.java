package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/*
    {
        "ID": 2,
        "Name": "Room 50",
        "Seats": 20
    }
 */
public class RoomResponse implements Parcelable {
    @SerializedName("ID")
    private int id;
    @SerializedName("Name")
    private String name;
    @SerializedName("Seats")
    private int seats;
    // Constructor
    public RoomResponse(int id, String name, int seats) {
        this.id = id;
        this.name = name;
        this.seats = seats;
    }

    protected RoomResponse(Parcel in) {
        id = in.readInt();
        name = in.readString();
        seats = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(seats);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoomResponse> CREATOR = new Creator<RoomResponse>() {
        @Override
        public RoomResponse createFromParcel(Parcel in) {
            return new RoomResponse(in);
        }

        @Override
        public RoomResponse[] newArray(int size) {
            return new RoomResponse[size];
        }
    };

    // Getters
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getSeats() {
        return seats;
    }
}
