package com.example.opendataresource.rest;

import com.example.opendataresource.model.TomorrowWeather;
import com.example.opendataresource.model.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetTomorrowWeatherEndPoint {


    @GET("forecast/")
    Call<TomorrowWeather> getWeather(@Query("q") String title, @Query("units") String unit, @Query("APPID") String apiKey);

}
