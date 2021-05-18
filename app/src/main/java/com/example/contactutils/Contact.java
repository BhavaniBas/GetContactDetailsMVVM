package com.example.contactutils;


import android.graphics.Bitmap;
import android.net.Uri;

public class Contact {

    private String id;
    private String name;
    private String mobileNumber;
    private String nameSubString;
    private Bitmap profileBitmap;
    private Uri profileUri;

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getNameSubString() {
        return nameSubString;
    }

    void setNameSubString(String nameSubString) {
        this.nameSubString = nameSubString;
    }

    public Bitmap getProfileBitmap() {
        return profileBitmap;
    }

    void setProfileBitmap(Bitmap profileBitmap) {
        this.profileBitmap = profileBitmap;
    }

    public Uri getProfileUri() {
        return profileUri;
    }

    void setProfileUri(Uri profileUri) {
        this.profileUri = profileUri;
    }
}
