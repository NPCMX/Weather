package com.weather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by CMX on 2017/3/1.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);//这里使用回调机制，在子线程中处理Http请求，在回调方法中处理事件响应
    }

}
