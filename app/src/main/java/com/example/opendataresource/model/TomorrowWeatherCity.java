package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

public class TomorrowWeatherCity {

    @SerializedName("name")
    String city;

    public String getCity() {
        return city;
    }
}
