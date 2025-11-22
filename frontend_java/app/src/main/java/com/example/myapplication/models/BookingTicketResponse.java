package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.myapplication.models.Broadcast;
import com.google.gson.annotations.SerializedName;

public class BookingTicketResponse implements Parcelable {
    @SerializedName("ID")
    private int ID;
    @SerializedName("DateOrder")
    private String DateOrder;
    @SerializedName("TimeOrder")
    private String TimeOrder; // Assuming this field exists based on the context
    @SerializedName("Price")
    private double Price;
    @SerializedName("RoomID")
    private int RoomID;
    @SerializedName("SeatID")
    private int SeatID;
    @SerializedName("Broadcast")
    private Broadcast Broadcast;

    @SerializedName("UserID")
    private int UserID; // Assuming this field exists based on the context
    protected BookingTicketResponse(Parcel in) {
        ID = in.readInt();
        DateOrder = in.readString();
        TimeOrder = in.readString(); // Read TimeOrder if it exists
        Price = in.readDouble();
        RoomID = in.readInt();
        SeatID = in.readInt();
        Broadcast = in.readParcelable(Broadcast.class.getClassLoader());
        UserID = in.readInt(); // Read UserID if it exists
    }

    public static final Creator<BookingTicketResponse> CREATOR = new Creator<BookingTicketResponse>() {
        @Override
        public BookingTicketResponse createFromParcel(Parcel in) {
            return new BookingTicketResponse(in);
        }

        @Override
        public BookingTicketResponse[] newArray(int size) {
            return new BookingTicketResponse[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ID);
        dest.writeString(DateOrder);
        dest.writeString(TimeOrder); // Write TimeOrder if it exists
        dest.writeDouble(Price);
        dest.writeInt(RoomID);
        dest.writeInt(SeatID);
        dest.writeParcelable(Broadcast, flags);
        dest.writeInt(UserID); // Write UserID if it exists
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public int getID() {
        return ID;
    }
    public int getUserID() {
        return UserID; // Assuming this field exists based on the context
    }
    public String getDateOrder() {
        return DateOrder;
    }
    public String getTimeOrder() {
        return TimeOrder; // Assuming this field exists based on the context
    }
    public double getPrice() {
        return Price;
    }
    public int getRoomID() {
        return RoomID;
    }
    public int getSeatID() {
        return SeatID;
    }
    public Broadcast getBroadcast() {
        return Broadcast;
    }
}
