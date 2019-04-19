package com.example.opendataresource;

import com.google.gson.annotations.SerializedName;

class CityCoordinates {
@SerializedName("lon")
    private double lon;
@SerializedName("lat")
    private double lat;

    public CityCoordinates(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }
}
