package com.example.bemberweather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bemberweather.databinding.ActivityMainBinding;
import com.example.bemberweather.pojo.WeatherData;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextView locationTextView;
    TextView temperatureTextView;
    TextView descriptionTextView;
    ImageView weatherImageView;
    ActivityMainBinding binding;
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        LinearLayout view = binding.getRoot();
        setContentView(view);

        locationTextView = binding.location;
        temperatureTextView = binding.temperature;
        descriptionTextView = binding.description;
        weatherImageView = binding.weatherImage;

        apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<WeatherData> call = apiInterface.getWeatherData(60, 60);
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                Log.d("XXX", "onResponse: "+response.code());
                locationTextView.setText(response.body().getName() + ", "
                        + response.body().getSys().getCountry());
                temperatureTextView.setText(response.body().getMain().getTemp().toString());
                descriptionTextView.setText(response.body().getWeather().get(0).getDescription());

                Picasso.get().load("http://openweathermap.org/img/wn/04d@2x.png").into(weatherImageView);

                Log.d("XXX", "icon path: " + "http://openweathermap.org/img/wn/"+response.body().getWeather().get(0)
                        .getIcon()+"@2x.png");
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {

            }
        });
    }
}