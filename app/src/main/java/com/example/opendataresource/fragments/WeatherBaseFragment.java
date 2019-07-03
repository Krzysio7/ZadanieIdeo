package com.example.opendataresource.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.opendataresource.BuildConfig;
import com.example.opendataresource.R;
import com.example.opendataresource.model.TomorrowWeather;
import com.example.opendataresource.model.Weather;
import com.example.opendataresource.rest.APIClient;
import com.example.opendataresource.rest.GetWeatherEndPoint;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;


import static android.view.View.GONE;

public abstract class WeatherBaseFragment extends Fragment {

    public abstract TextView temperatureTextView();

    public abstract TextView cityTextView();

    public abstract TextView weatherDescriptionTextView();

    public abstract ImageView weatherIcon();

    public abstract ProgressBar tempProgressBar();

    public abstract TextView tempUnitView();

    public abstract ProgressBar imgViewProgress();

    public abstract void getWeather();

    public abstract String unit();


    private CharSequence location;
    String apiKey = BuildConfig.ApiKey;
    String unit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = provideYourFragmentView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        return view;

    }

    public abstract View provideYourFragmentView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState);


    public void onClickRadio(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioC:
                if (checked)
                    this.unit = "metric";
                if (getCurrentData()) {

                    getWeather();
                }
                tempUnitView().setText(R.string.celciusUnitText);
                break;
            case R.id.radioF:
                if (checked)
                    this.unit = "imperial";
                if (getCurrentData()) {

                    getWeather();

                }
                tempUnitView().setText(R.string.fahrenheitUnitText);
                break;
        }
    }


    public CharSequence getLocation() {
        return location;
    }

    private boolean getCurrentData() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        location = sharedPref.getString(getString(R.string.saved_high_score_key), null);
        if (location == null) {
            return false;
        }
        return true;
    }


    public void getTodayWeather(CharSequence title) {

        addRecentLocationToSharedPref(title);

        GetWeatherEndPoint api = APIClient.getClient().create(GetWeatherEndPoint.class);

        Call<Weather> call = api.getTodayWeather(title.toString(), unit, apiKey);
        call.enqueue(new Callback<Weather>() {

            @Override
                public void onResponse(Call<Weather> call, retrofit2.Response<Weather> response) {

                String temp = Integer.toString((int) Math.round(response.body().getMain().getTemp()));
                String city = response.body().getCity();
                String weatherDescription = response.body().getWeatherInfo().get(0).getDescription();
                String iconId = response.body().getWeatherInfo().get(0).getIconId();

                setWeatherInfo(temp, city, weatherDescription, iconId);

            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {

                hideWeatherInfo();

            }
        });
    }

    private void hideWeatherInfo() {

        tempProgressBar().setVisibility(View.VISIBLE);
        imgViewProgress().setVisibility(View.VISIBLE);
        temperatureTextView().setVisibility(GONE);
        tempUnitView().setVisibility(GONE);
        weatherIcon().setVisibility(GONE);
    }

    public void getTomorrowWeather(CharSequence title) {

        addRecentLocationToSharedPref(title);

        GetWeatherEndPoint api = APIClient.getClient().create(GetWeatherEndPoint.class);

        Call<TomorrowWeather> call = api.getTomorrowWeather(title.toString(), unit, apiKey);

        call.enqueue(new Callback<TomorrowWeather>() {
            @Override
            public void onResponse(Call<TomorrowWeather> call, retrofit2.Response<TomorrowWeather> response) {

                String temp = Integer.toString((int) Math.round(response.body().getTomorrowWeatherList().get(0).getTemp().getTemp()));
                String city = response.body().getCity().getCity();
                String weatherDescription = response.body().getTomorrowWeatherList().get(0).getDescription().get(0).getDescription();
                String iconId = response.body().getTomorrowWeatherList().get(0).getDescription().get(0).getIcon();

                setWeatherInfo(temp, city, weatherDescription, iconId);

            }

            @Override
            public void onFailure(Call<TomorrowWeather> call, Throwable t) {

                hideWeatherInfo();

            }
        });

    }

    private void addRecentLocationToSharedPref(CharSequence title) {
        if (this.unit == null) {
            this.unit = "metric";
        }
        if (title != null) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_high_score_key), title.toString());
            editor.apply();

        }
    }


    private void setWeatherInfo(String temp, String city, String weatherDescription, String iconId) {

        imgViewProgress().setVisibility(View.GONE);
        weatherIcon().setVisibility(View.VISIBLE);
        tempProgressBar().setVisibility(View.GONE);
        temperatureTextView().setVisibility(View.VISIBLE);
        tempUnitView().setVisibility(View.VISIBLE);
        temperatureTextView().setText(temp);
        cityTextView().setText(city);
        weatherDescriptionTextView().setText(weatherDescription);
        Picasso.get().load("http://openweathermap.org/img/w/" + iconId + ".png").into(weatherIcon());
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onComplete();

    }

    public interface OnCompleteListener {
        void onComplete();
    }

    private OnCompleteListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mListener = (OnCompleteListener) context;

    }
}
