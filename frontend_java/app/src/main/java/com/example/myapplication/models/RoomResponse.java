package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/*
    {
        "ID": 2,
        "Name": "Room 50",
        "Seats": 20,
        "CinemaId": 1
    }
 */
public class RoomResponse implements Parcelable {
    @SerializedName("ID")
    private int id;
    @SerializedName("Name")
    private String name;
    @SerializedName("Seats")
    private int seats;
    @SerializedName("CinemaId")
    private Integer cinemaId;
    @SerializedName("Cinema")
    private Cinema cinema;

    // Constructor
    public RoomResponse(int id, String name, int seats) {
        this.id = id;
        this.name = name;
        this.seats = seats;
    }

    public RoomResponse(int id, String name, int seats, Integer cinemaId) {
        this.id = id;
        this.name = name;
        this.seats = seats;
        this.cinemaId = cinemaId;
    }

    protected RoomResponse(Parcel in) {
        id = in.readInt();
        name = in.readString();
        seats = in.readInt();
        if (in.readByte() == 0) {
            cinemaId = null;
        } else {
            cinemaId = in.readInt();
        }
        cinema = in.readParcelable(Cinema.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(seats);
        if (cinemaId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(cinemaId);
        }
        dest.writeParcelable(cinema, flags);
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
    public Integer getCinemaId() {
        return cinemaId;
    }
    public Cinema getCinema() {
        return cinema;
    }

    // Setters
    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }
    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }
}
