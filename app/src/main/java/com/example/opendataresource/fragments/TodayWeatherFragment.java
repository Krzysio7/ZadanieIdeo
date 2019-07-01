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

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.opendataresource.BuildConfig;
import com.example.opendataresource.R;
import com.example.opendataresource.model.Weather;
import com.example.opendataresource.rest.APIClient;
import com.example.opendataresource.rest.GetTodayWeatherEndPoint;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;

import static android.view.View.GONE;

public class TodayWeatherFragment extends Fragment {
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
    private View imgViewProgress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        return inflater.inflate(R.layout.today_weather_layout, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListener.onComplete();


        unitText = getActivity().findViewById(R.id.measureUnitView);
        temperatureTextView = getActivity().findViewById(R.id.temperatureView);
        cityTextView = getActivity().findViewById(R.id.cityView);
        weatherDescriptionTextView = getActivity().findViewById(R.id.weatherDescriptionView);
        weatherIcon = getActivity().findViewById(R.id.weatherIconView);
        tempProgressBar = getActivity().findViewById(R.id.temperatureProgressBar);
        tempUnitView = getActivity().findViewById(R.id.measureUnitView);
        imgViewProgress = getActivity().findViewById(R.id.imageViewProgress);
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


    public void getWeather(CharSequence title) {
        if (this.unit == null) {
            this.unit = "metric";
        }
        if(title!=null) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_high_score_key), title.toString());
            editor.apply();
        }


        GetTodayWeatherEndPoint api = APIClient.getClient().create(GetTodayWeatherEndPoint.class);

        Call<Weather> call = api.getWeather(title.toString(), unit, apiKey);
        call.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, retrofit2.Response<Weather> response) {
                String temp = Integer.toString((int) Math.round(response.body().getMain().getTemp()));
                String city = response.body().getCity();
                String weatherDescription = response.body().getWeatherInfo().get(0).getDescription();
                String iconId = response.body().getWeatherInfo().get(0).getIconId();

                tempProgressBar.setVisibility(GONE);
                imgViewProgress.setVisibility(GONE);
                temperatureTextView.setVisibility(View.VISIBLE);
                tempUnitView.setVisibility(View.VISIBLE);
                temperatureTextView.setText(temp);
                cityTextView.setText(city);
                weatherIcon.setVisibility(View.VISIBLE);
                weatherDescriptionTextView.setText(weatherDescription);
                Picasso.get().load("http://openweathermap.org/img/w/" + iconId + ".png").into(weatherIcon);
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                tempProgressBar.setVisibility(View.VISIBLE);
                imgViewProgress.setVisibility(View.VISIBLE);
                temperatureTextView.setVisibility(GONE);
                tempUnitView.setVisibility(GONE);
                weatherIcon.setVisibility(GONE);
            }
        });
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
                unitText.setText(R.string.celciusUnitText);
                break;
            case R.id.radioF:
                if (checked)
                    this.unit = "imperial";
                if (getCurrentData()) {

                    getWeather(location);
                }
                unitText.setText(R.string.fahrenheitUnitText);
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
