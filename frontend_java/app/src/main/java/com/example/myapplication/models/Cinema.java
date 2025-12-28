package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class representing a Cinema
 * Response from API:
 * {
 *     "ID": 1,
 *     "Name": "CGV Vincom",
 *     "Address": "72 Lê Thánh Tôn, Bến Nghé, Quận 1, TP.HCM",
 *     "Latitude": 10.776889,
 *     "Longitude": 106.701686,
 *     "Phone": "1900 6017",
 *     "ImageUrl": "https://example.com/image.jpg",
 *     "Description": "Rạp chiếu phim CGV tại Vincom Center",
 *     "Distance": {"text": "5.2 km", "value": 5200},
 *     "Duration": {"text": "15 phút", "value": 900},
 *     "Rooms": [...]
 * }
 */
public class Cinema implements Parcelable {

    @SerializedName("ID")
    private int id;

    @SerializedName("Name")
    private String name;

    @SerializedName("Address")
    private String address;

    @SerializedName("Latitude")
    private double latitude;

    @SerializedName("Longitude")
    private double longitude;

    @SerializedName("Phone")
    private String phone;

    @SerializedName("ImageUrl")
    private String imageUrl;

    @SerializedName("Description")
    private String description;

    @SerializedName("Distance")
    private DistanceInfo distance;

    @SerializedName("Duration")
    private DurationInfo duration;

    @SerializedName("Rooms")
    private List<RoomResponse> rooms;

    // Default constructor
    public Cinema() {}

    // Full constructor
    public Cinema(int id, String name, String address, double latitude, double longitude,
                  String phone, String imageUrl, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Parcelable constructor
    protected Cinema(Parcel in) {
        id = in.readInt();
        name = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        phone = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        distance = in.readParcelable(DistanceInfo.class.getClassLoader());
        duration = in.readParcelable(DurationInfo.class.getClassLoader());
        rooms = in.createTypedArrayList(RoomResponse.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(phone);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeParcelable(distance, flags);
        dest.writeParcelable(duration, flags);
        dest.writeTypedList(rooms);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Cinema> CREATOR = new Creator<Cinema>() {
        @Override
        public Cinema createFromParcel(Parcel in) {
            return new Cinema(in);
        }

        @Override
        public Cinema[] newArray(int size) {
            return new Cinema[size];
        }
    };

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getPhone() { return phone; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public DistanceInfo getDistance() { return distance; }
    public DurationInfo getDuration() { return duration; }
    public List<RoomResponse> getRooms() { return rooms; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setDistance(DistanceInfo distance) { this.distance = distance; }
    public void setDuration(DurationInfo duration) { this.duration = duration; }
    public void setRooms(List<RoomResponse> rooms) { this.rooms = rooms; }

    // Helper methods
    public String getDistanceText() {
        return distance != null ? distance.getText() : "";
    }

    public int getDistanceValue() {
        return distance != null ? distance.getValue() : 0;
    }

    public String getDurationText() {
        return duration != null ? duration.getText() : "";
    }

    public int getDurationValue() {
        return duration != null ? duration.getValue() : 0;
    }

    /**
     * Inner class for Distance info from Goong API
     */
    public static class DistanceInfo implements Parcelable {
        @SerializedName("text")
        private String text;

        @SerializedName("value")
        private int value; // in meters

        public DistanceInfo() {}

        public DistanceInfo(String text, int value) {
            this.text = text;
            this.value = value;
        }

        protected DistanceInfo(Parcel in) {
            text = in.readString();
            value = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeInt(value);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DistanceInfo> CREATOR = new Creator<DistanceInfo>() {
            @Override
            public DistanceInfo createFromParcel(Parcel in) {
                return new DistanceInfo(in);
            }

            @Override
            public DistanceInfo[] newArray(int size) {
                return new DistanceInfo[size];
            }
        };

        public String getText() { return text; }
        public int getValue() { return value; }
        public void setText(String text) { this.text = text; }
        public void setValue(int value) { this.value = value; }
    }

    /**
     * Inner class for Duration info from Goong API
     */
    public static class DurationInfo implements Parcelable {
        @SerializedName("text")
        private String text;

        @SerializedName("value")
        private int value; // in seconds

        public DurationInfo() {}

        public DurationInfo(String text, int value) {
            this.text = text;
            this.value = value;
        }

        protected DurationInfo(Parcel in) {
            text = in.readString();
            value = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeInt(value);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DurationInfo> CREATOR = new Creator<DurationInfo>() {
            @Override
            public DurationInfo createFromParcel(Parcel in) {
                return new DurationInfo(in);
            }

            @Override
            public DurationInfo[] newArray(int size) {
                return new DurationInfo[size];
            }
        };

        public String getText() { return text; }
        public int getValue() { return value; }
        public void setText(String text) { this.text = text; }
        public void setValue(int value) { this.value = value; }
    }
}

