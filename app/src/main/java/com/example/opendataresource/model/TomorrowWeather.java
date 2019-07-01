package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TomorrowWeather {

    @SerializedName("list")
    List<HourlyWeather> weatherList;

    @SerializedName("city")
    TomorrowWeatherCity city;

    public List<HourlyWeather> getTomorrowWeatherList() {
        return weatherList;
    }

    public TomorrowWeatherCity getCity() {
        return city;
    }

}
