package com.example.bemberweather;

import com.example.bemberweather.pojo.WeatherData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

interface APIInterface {

    @GET("/data/2.5/weather?units=metric&appid=b46ef128398cdc3f6308de2eb8b5f588")
    Call<WeatherData> getWeatherData(@Query("lat") int lat, @Query("lon") int lon);
}