package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model for Goong Places Autocomplete response
 */
public class PlaceAutocomplete {

    @SerializedName("predictions")
    private List<Prediction> predictions;

    @SerializedName("status")
    private String status;

    public List<Prediction> getPredictions() { return predictions; }
    public String getStatus() { return status; }

    /**
     * Prediction item from autocomplete
     */
    public static class Prediction implements Parcelable {
        @SerializedName("description")
        private String description;

        @SerializedName("place_id")
        private String placeId;

        @SerializedName("structured_formatting")
        private StructuredFormatting structuredFormatting;

        public Prediction() {}

        protected Prediction(Parcel in) {
            description = in.readString();
            placeId = in.readString();
            structuredFormatting = in.readParcelable(StructuredFormatting.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(description);
            dest.writeString(placeId);
            dest.writeParcelable(structuredFormatting, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Prediction> CREATOR = new Creator<Prediction>() {
            @Override
            public Prediction createFromParcel(Parcel in) {
                return new Prediction(in);
            }

            @Override
            public Prediction[] newArray(int size) {
                return new Prediction[size];
            }
        };

        public String getDescription() { return description; }
        public String getPlaceId() { return placeId; }
        public StructuredFormatting getStructuredFormatting() { return structuredFormatting; }

        public String getMainText() {
            return structuredFormatting != null ? structuredFormatting.getMainText() : "";
        }

        public String getSecondaryText() {
            return structuredFormatting != null ? structuredFormatting.getSecondaryText() : "";
        }
    }

    /**
     * Structured formatting for prediction
     */
    public static class StructuredFormatting implements Parcelable {
        @SerializedName("main_text")
        private String mainText;

        @SerializedName("secondary_text")
        private String secondaryText;

        public StructuredFormatting() {}

        protected StructuredFormatting(Parcel in) {
            mainText = in.readString();
            secondaryText = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mainText);
            dest.writeString(secondaryText);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<StructuredFormatting> CREATOR = new Creator<StructuredFormatting>() {
            @Override
            public StructuredFormatting createFromParcel(Parcel in) {
                return new StructuredFormatting(in);
            }

            @Override
            public StructuredFormatting[] newArray(int size) {
                return new StructuredFormatting[size];
            }
        };

        public String getMainText() { return mainText; }
        public String getSecondaryText() { return secondaryText; }
    }
}

