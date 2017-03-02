package com.weather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by CMX on 2017/3/1.
 */

public class Forecast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public Condition condition;

    public class Temperature {

        public String max;

        public String min;

    }

    public class Condition {

        @SerializedName("txt_d")
        public String info;

    }



}
