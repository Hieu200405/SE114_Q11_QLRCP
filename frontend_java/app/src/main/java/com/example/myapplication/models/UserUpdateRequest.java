package com.example.myapplication.models;


import com.google.gson.annotations.SerializedName;

/*
{
    "name": "Updated Name",
    "phone": "098765654321",
    "email": "updatedd@axaaample.com"
}

 */
public class UserUpdateRequest {
    @SerializedName("name")
    private String name;
    @SerializedName("phone")
    private String phone;
    @SerializedName("email")
    private String email;
    public UserUpdateRequest(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
    // Getters
    public String getName() {
        return name;
    }
    public String getPhone() {
        return phone;
    }
    public String getEmail() {
        return email;
    }
    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
