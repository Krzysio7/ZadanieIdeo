package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

public class TomorrowWeatherDescription {

    @SerializedName("description")
    String description;

    @SerializedName("icon")
    String icon;


    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }
}
