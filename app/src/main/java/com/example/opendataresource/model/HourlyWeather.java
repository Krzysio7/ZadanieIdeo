package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HourlyWeather {

    @SerializedName("main")
    AvgTemp temp;

    @SerializedName("weather")
    List<TomorrowWeatherDescription> description;

    public AvgTemp getTemp() {
        return temp;
    }

    public List<TomorrowWeatherDescription> getDescription() {
        return description;
    }


}
