package com.example.bemberweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bemberweather.databinding.ActivityMainBinding;
import com.example.bemberweather.pojo.Main;
import com.example.bemberweather.pojo.WeatherData;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;

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
    LocationManager locationManager;
    EditText searchBox;
    Button searchButton;
    double lat;
    double lon;
    boolean isSearch;
    String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        RelativeLayout view = binding.getRoot();
        setContentView(view);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isSearch = extras.getBoolean("isSearch");
        }

        if(isSearch){
            ActionBar actionBar =  getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);

            RelativeLayout searchLayout = new RelativeLayout(this);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPixels(50));
            layoutParams.setMargins(0, dpToPixels(5), 0,0);
            searchLayout.setLayoutParams(layoutParams);
            binding.linearMain.addView(searchLayout, 0);

            searchBox = new EditText(this);
            searchBox.setBackground(getDrawable(R.drawable.search));
            searchBox.setLayoutParams(new ViewGroup.LayoutParams(dpToPixels(300), dpToPixels(50)));
            searchBox.setGravity(Gravity.CENTER);
            searchBox.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            searchLayout.addView(searchBox);

            searchButton = new Button(this);
            searchButton.setBackground(getDrawable(R.drawable.loupe));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dpToPixels(50), dpToPixels(50));
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            params.setMarginEnd(dpToPixels(10));
            searchButton.setLayoutParams(params);
            searchLayout.addView(searchButton);

            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    city = searchBox.getText().toString();
                    getWeather();
                }
            });
        }

        locationTextView = binding.location;
        temperatureTextView = binding.temperature;
        descriptionTextView = binding.description;
        weatherImageView = binding.weatherImage;

        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }

        binding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra("isSearch", true);
                startActivity(intent);
            }
        });
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getLocation() {
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                lat = locationGPS.getLatitude();
                lon = locationGPS.getLongitude();

                Log.d("XXX", "LAT: " + lat + " LON: " + lon);
                getWeather();
            }
            else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getWeather(){
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<WeatherData> call;
        if(isSearch && city != null && city != ""){
            call = apiInterface.getWeatherDataCity(city);
        }
        else{
            call = apiInterface.getWeatherData((double) lat, (double) lon);
        }
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                Log.d("XXX", "onResponse: "+response.code());
                if(response.code() == 200){
                    WeatherData body = response.body();
                    locationTextView.setText(body.getName() + ", "
                            + body.getSys().getCountry());
                    temperatureTextView.setText(body.getMain().getTemp().toString() + "°C");
                    descriptionTextView.setText(body.getWeather().get(0).getDescription());

                    binding.humidity.setText(": " + body.getMain().getHumidity().toString() + "%");
                    binding.maxTemp.setText(": " + body.getMain().getTempMax().toString() + "°C");
                    binding.minTemp.setText(": " + body.getMain().getTempMin().toString() + "°C");
                    binding.pressure.setText(": " + body.getMain().getPressure().toString() + " hPa");
                    binding.windSpeed.setText(": " + body.getWind().getSpeed().toString() + " m/s");


                    String iconUrl = "http://openweathermap.org/img/wn/"+response.body().getWeather().get(0)
                            .getIcon().replace('n','d')+"@2x.png";
                    Picasso.get().load(iconUrl).into(weatherImageView);
                    Log.d("XXX", "onResponse: " + iconUrl);
                }
                else {
                    if(isSearch)
                        Toast.makeText(MainActivity.this,"Couldn't retrieve data for " + city, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String permission = permissions[0];
        int grantResult = grantResults[0];
        if(requestCode == 1){
            if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)){
                if(grantResult == PackageManager.PERMISSION_GRANTED){
                    getWeather();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int pixelsToDp(float pixels) {
        float screenPixelDensity = getResources().getDisplayMetrics().density;
        float dpValue = pixels / screenPixelDensity;
        return (int) dpValue;
    }
    private int dpToPixels(float dp) {
        float screenPixelDensity = getResources().getDisplayMetrics().density;
        float pixels = dp * screenPixelDensity;
        return (int) pixels;
    }
}