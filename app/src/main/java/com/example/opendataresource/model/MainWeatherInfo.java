package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

public class MainWeatherInfo {

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    @SerializedName("temp")
    Double temp;
}
