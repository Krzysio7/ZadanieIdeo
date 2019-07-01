package com.example.opendataresource.model;

import com.google.gson.annotations.SerializedName;

public class AvgTemp {

    public Double getTemp() {
        return temp;
    }

    @SerializedName("temp")
    Double temp;
}
