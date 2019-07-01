package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

public class WeatherInfo {

    @SerializedName("description")
    String description;

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    @SerializedName("icon")
    String iconId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
