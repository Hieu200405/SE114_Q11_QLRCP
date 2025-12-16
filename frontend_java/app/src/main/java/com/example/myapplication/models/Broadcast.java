package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Broadcast implements Parcelable {
    @SerializedName("ID")
    private int ID;
    @SerializedName("FilmID")
    private int FilmID;
    @SerializedName("FilmName")
    private String FilmName;
    @SerializedName("DateBroadcast")
    private String DateBroadcast;
    @SerializedName("TimeBroadcast")
    private String TimeBroadcast;
    @SerializedName("Runtime")
    private int Runtime;
    @SerializedName("Thumbnail")
    private String Thumbnail;

    protected Broadcast(Parcel in) {
        ID = in.readInt();
        FilmID = in.readInt();
        FilmName = in.readString();
        DateBroadcast = in.readString();
        TimeBroadcast = in.readString();
        Runtime = in.readInt();
        Thumbnail = in.readString();
    }

    public static final Creator<Broadcast> CREATOR = new Creator<Broadcast>() {
        @Override
        public Broadcast createFromParcel(Parcel in) {
            return new Broadcast(in);
        }

        @Override
        public Broadcast[] newArray(int size) {
            return new Broadcast[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeInt(FilmID);
        dest.writeString(FilmName);
        dest.writeString(DateBroadcast);
        dest.writeString(TimeBroadcast);
        dest.writeInt(Runtime);
        dest.writeString(Thumbnail);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public int getID() {
        return ID;
    }
    public int getFilmID() {
        return FilmID;
    }
    public String getFilmName() {
        return FilmName;
    }
    public String getDateBroadcast() {
        return DateBroadcast;
    }
    public String getTimeBroadcast() {
        return TimeBroadcast;
    }
    public int getRuntime() {
        return Runtime;
    }
    public String getThumbnail() {
        return Thumbnail;
    }
}