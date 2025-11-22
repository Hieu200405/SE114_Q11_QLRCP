package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DetailFirm implements Parcelable {

    @SerializedName("ID")
    private int id;

    @SerializedName("Name")
    private String name;

    @SerializedName("StartDate")
    private String startDate;

    @SerializedName("EndDate")
    private String endDate;

    @SerializedName("Description")
    private String description;

    @SerializedName("Rating")
    private Double rating;

    @SerializedName("RatingCount")
    private Integer ratingCount;

    @SerializedName("ThumbnailPath")
    private String thumbnailPath;

    @SerializedName("Images")
    private List<ImageFirm> images;


    @SerializedName("Runtime")
    private Integer runtime;
// Getters

    protected DetailFirm(Parcel in) {
        id = in.readInt();
        name = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        description = in.readString();
        if (in.readByte() == 0) {
            rating = null;
        } else {
            rating = in.readDouble();
        }
        if (in.readByte() == 0) {
            ratingCount = null;
        } else {
            ratingCount = in.readInt();
        }
        thumbnailPath = in.readString();
        if (in.readByte() == 0) {
            runtime = null;
        } else {
            runtime = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(description);
        if (rating == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(rating);
        }
        if (ratingCount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(ratingCount);
        }
        dest.writeString(thumbnailPath);
        if (runtime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(runtime);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DetailFirm> CREATOR = new Creator<DetailFirm>() {
        @Override
        public DetailFirm createFromParcel(Parcel in) {
            return new DetailFirm(in);
        }

        @Override
        public DetailFirm[] newArray(int size) {
            return new DetailFirm[size];
        }
    };

    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


    public String getStartDate() {
        return startDate;
    }


    public String getEndDate() {
        return endDate;
    }


    public Double getRating() {
        return rating;
    }


    public Integer getRatingCount() {
        return ratingCount;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public List<ImageFirm> getImages() {
        return images;
    }

    public Integer getRuntime() {
        return runtime;
    }

//    Setters

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public void setImages(List<ImageFirm> images) {
        this.images = images;
    }
}
