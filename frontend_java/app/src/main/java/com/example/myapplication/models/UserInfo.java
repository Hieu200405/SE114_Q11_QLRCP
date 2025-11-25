package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;


/*


{
    "Email": "tesgt@example.com",
    "ID": 5,
    "Name": "Thanh huy",
    "Phone": "123456780",
    "Role": "user",
    "Username": "user123"
}

 */
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class UserInfo implements Parcelable {

    @SerializedName("Email")
    private String email;
    @SerializedName("ID")
    private int id;
    @SerializedName("Name")
    private String name;
    @SerializedName("Phone")
    private String phone;
    @SerializedName("Role")
    private String role;
    @SerializedName("Username")
    private String username;

    // Getters
    public String getEmail() { return email; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getUsername() { return username; }

    // Constructor mặc định
    public UserInfo() {}

    // Constructor từ Parcel
    protected UserInfo(Parcel in) {
        email = in.readString();
        id = in.readInt();
        name = in.readString();
        phone = in.readString();
        role = in.readString();
        username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(role);
        dest.writeString(username);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
