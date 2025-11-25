package com.example.myapplication.models;

import com.google.gson.annotations.SerializedName;


/*
    {"asset_id":"c41952d30b0edea88290e068be36b38b",
    "public_id":"fof7b2ulj6idrswvnbyq",
    "version":1750785425,
    "version_id":"e3bd23b6e7e240c42ca191d6143f74b9"
    ,"signature":"0a185e335cb896ea226b285836718c73f7320492",
    "width":225,"height":225,"format":"webp",
    "resource_type":"image","created_at":"2025-06-24T17:17:05Z",
    "tags":[],"pages":1,"bytes":23316,"type":"upload",
    "etag":"f25445b56440afa640e1fec992ac9aa8",
    "placeholder":false,
    "url":"http://res.cloudinary.com/dfu6ly3og/image/upload/v1750785425/fof7b2ulj6idrswvnbyq.webp",
    "secure_url":"https://res.cloudinary.com/dfu6ly3og/image/upload/v1750785425/fof7b2ulj6idrswvnbyq.webp",
    "asset_folder":"","display_name":"image","original_filename":"image","original_extension":"jpg"}


 */
public class ResultImageCloudinaryResponse {
    @SerializedName("url")
    private String url;

    @SerializedName("secure_url")
    private String secureUrl;

    public ResultImageCloudinaryResponse(String url, String secureUrl) {
        this.url = url;
        this.secureUrl = secureUrl;
    }
    public String getUrl() {
        return url;
    }
    public String getSecureUrl() {
        return secureUrl;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }
}
