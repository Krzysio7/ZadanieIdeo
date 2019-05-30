package com.example.opendataresource.fragments;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.opendataresource.BuildConfig;
import com.example.opendataresource.MySingleton;
import com.example.opendataresource.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.view.View.GONE;

public class TomorrowWeatherFragment extends Fragment {
    public TextView temperatureTextView;

    public JsonObjectRequest jsonObjectRequest;
    private TextView cityTextView;
    private TextView weatherDescriptionTextView;
    private ImageView weatherIcon;
    private ProgressBar tempProgressBar;
    private TextView tempUnitView;
    public String unit;
    private CharSequence location;
    private TextView unitText;
    String apiKey = BuildConfig.ApiKey;
    private ProgressBar imgViewProgress;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tomorrow_weather_layout, container, false);
    }


    public void getWeather(CharSequence title) {
        if (unit == null) {
            this.unit = "metric";
        }
        if (title != null) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_high_score_key), title.toString());
            editor.apply();

        }
        temperatureTextView = getActivity().findViewById(R.id.temperatureView2);
        cityTextView = getActivity().findViewById(R.id.cityView2);
        weatherDescriptionTextView = getActivity().findViewById(R.id.weatherDescriptionView2);
        weatherIcon = getActivity().findViewById(R.id.weatherIconView2);
        tempProgressBar = getActivity().findViewById(R.id.temperatureProgressBar2);
        tempUnitView = getActivity().findViewById(R.id.measureUnitView2);
        imgViewProgress = getActivity().findViewById(R.id.imageViewProgress2);
        if (jsonObjectRequest != null) {
            jsonObjectRequest.cancel();
        }
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + title + "&units=" + this.unit + "&APPID=" + apiKey;


        Log.i("cos", url);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray obj = response.getJSONArray("list");
                            JSONObject obj2 = response.getJSONObject("city");
                            String city = obj2.getString("name");
                            String temp = Integer.toString((int) Math.round(obj.getJSONObject(0).getJSONObject("main").getDouble("temp")));
                            String weatherDescription = obj.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("description");
                            String iconId = obj.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("icon");

                            imgViewProgress.setVisibility(View.GONE);
                            weatherIcon.setVisibility(View.VISIBLE);
                            tempProgressBar.setVisibility(View.GONE);
                            temperatureTextView.setVisibility(View.VISIBLE);
                            tempUnitView.setVisibility(View.VISIBLE);
                            temperatureTextView.setText(temp);
                            cityTextView.setText(city);
                            weatherDescriptionTextView.setText(weatherDescription);
                            Picasso.get().load("http://openweathermap.org/img/w/" + iconId + ".png").into(weatherIcon);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tempProgressBar.setVisibility(View.VISIBLE);
                temperatureTextView.setVisibility(View.GONE);
                imgViewProgress.setVisibility(View.VISIBLE);
                tempUnitView.setVisibility(View.GONE);
                weatherIcon.setVisibility(GONE);

                error.printStackTrace();
            }
        });

// Add the request to the RequestQueue.
        MySingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }


    public void onClickRadio(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioC:
                if (checked)
                    this.unit = "metric";
                if (getCurrentData()) {

                    getWeather(location);
                }
                tempUnitView.setText(R.string.celciusUnitText);
                break;
            case R.id.radioF:
                if (checked)
                    this.unit = "imperial";
                if (getCurrentData()) {

                    getWeather(location);
                }
                tempUnitView.setText(R.string.fahrenheitUnitText);
                break;
        }
    }

    private boolean getCurrentData() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        location = sharedPref.getString(getString(R.string.saved_high_score_key), null);
        if (location == null) {
            return false;
        }
        return true;
    }
}
