package com.example.opendataresource.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.opendataresource.R;

import butterknife.BindView;

public class TomorrowWeatherFragment extends WeatherBaseFragment {

    @BindView(R.id.tvTemperature)
    TextView temperatureTextView;
    @BindView(R.id.tvCity)
    TextView cityTextView;
    @BindView(R.id.tvWeatherDesc)
    TextView weatherDescriptionTextView;
    @BindView(R.id.ivWeatherStatusIcon)
    ImageView weatherIcon;
    @BindView(R.id.pbTemperature)
    ProgressBar tempProgressBar;
    @BindView(R.id.tvMeasureUnit)
    TextView tempUnitView;
    @BindView(R.id.pbWeatherStatusIcon)
    ProgressBar imgViewProgress;
    String unit = "metric";

    @Override
    public TextView temperatureTextView() {
        return temperatureTextView;
    }

    @Override
    public TextView cityTextView() {
        return cityTextView;
    }

    @Override
    public TextView weatherDescriptionTextView() {
        return weatherDescriptionTextView;
    }

    @Override
    public ImageView weatherIcon() {
        return weatherIcon;
    }

    @Override
    public ProgressBar tempProgressBar() {
        return tempProgressBar;
    }

    @Override
    public TextView tempUnitView() {
        return tempUnitView;
    }

    @Override
    public ProgressBar imgViewProgress() {
        return imgViewProgress;
    }

    @Override
    public String unit() {
        return unit;
    }

    @Override
    public void getWeather() {
        getTomorrowWeather(getLocation());
    }

    @Override
    public View provideYourFragmentView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weather_layout, parent, false);

        return view;
    }

    @Override
    public void getTomorrowWeather(CharSequence title) {
        super.getTomorrowWeather(title);
    }

    @Override
    public void onClickRadio(View view) {
        super.onClickRadio(view);
    }

}
