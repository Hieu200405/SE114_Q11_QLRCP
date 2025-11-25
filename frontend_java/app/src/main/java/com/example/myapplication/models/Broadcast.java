package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Broadcast implements Parcelable {
    @SerializedName("ID")
    private int ID;
    @SerializedName("FirmID")
    private int FirmID;
    @SerializedName("FirmName")
    private String FirmName;
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
        FirmID = in.readInt();
        FirmName = in.readString();
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
        dest.writeInt(FirmID);
        dest.writeString(FirmName);
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
    public int getFirmID() {
        return FirmID;
    }
    public String getFirmName() {
        return FirmName;
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