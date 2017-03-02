package com.weather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by CMX on 2017/3/1.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public Condition condition;

    public class Condition{

        @SerializedName("txt")
        public String info;
    }
}
