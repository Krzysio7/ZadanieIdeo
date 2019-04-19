package com.example.opendataresource.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.opendataresource.R;

public class TodayWeatherFragment extends Fragment {



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      super.onCreateView(inflater,container,savedInstanceState);



        return inflater.inflate(R.layout.today_weather_layout,container,false);


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

        this.mListener = (OnCompleteListener)context;
    }
}
