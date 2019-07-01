package com.example.opendataresource.rest;

import com.example.opendataresource.BuildConfig;
import com.example.opendataresource.model.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetTodayWeatherEndPoint {

    @GET("weather/")
    Call<Weather> getWeather(@Query("q") String title, @Query("units") String unit, @Query("APPID") String apiKey);

}
