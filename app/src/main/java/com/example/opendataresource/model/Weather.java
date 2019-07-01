package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    @SerializedName("main")
    MainWeatherInfo main;

    @SerializedName("name")
    String city;


    @SerializedName("weather")
    List<WeatherInfo> weatherInfo;


    public MainWeatherInfo getMain() {
        return main;
    }

    public void setMain(MainWeatherInfo main) {
        this.main = main;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<WeatherInfo> getWeatherInfo() {
        return weatherInfo;
    }
    public void setWeatherInfo(List<WeatherInfo> weatherInfo) {
        this.weatherInfo = weatherInfo;
    }


}
