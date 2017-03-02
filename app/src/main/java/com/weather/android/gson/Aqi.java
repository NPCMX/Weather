package com.weather.android.gson;

/**
 * Created by CMX on 2017/3/1.
 */

public class Aqi {

    public AQICity city;

    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
