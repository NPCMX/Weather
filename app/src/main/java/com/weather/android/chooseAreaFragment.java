package com.weather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weather.android.db.City;
import com.weather.android.db.County;
import com.weather.android.db.Province;
import com.weather.android.util.HttpUtil;
import com.weather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by CMX on 2017/3/1.
 */

public class chooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView mListView;

    private ArrayAdapter<String> mAdapter;

    private List<String> dataList = new ArrayList<>();

    private List<Province> mProvinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    private Province selectedProvince;
    private City selectedCity;

    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text_view);
        backButton = (Button)view.findViewById(R.id.back_button);
        mListView = (ListView)view.findViewById(R.id.list_view);

        mAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(mAdapter);
        Log.d("NPCMX", "onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (currentLevel == LEVEL_PROVINCE){
//                    selectedProvince = mProvinceList.get(position);
//                    queryCities();
//                }else if (currentLevel == LEVEL_CITY){
//                    selectedCity = mCityList.get(position);
//                    queryCounties();
//                }
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        selectedProvince = mProvinceList.get(position);
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        selectedCity = mCityList.get(position);
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        String weatherId = mCountyList.get(position).getWeatherId();
                        if (getActivity() instanceof MainActivity){
                            Intent intent = new Intent(getActivity(),WeatherActivity.class);
                            intent.putExtra("weather_id",weatherId);
                            startActivity(intent);
                            getActivity().finish();
                        }else if (getActivity() instanceof WeatherActivity){
                            WeatherActivity activity = (WeatherActivity) getActivity();
                            activity.drawerLayout.closeDrawers();
                            activity.swipeRefresh.setRefreshing(true);
                            activity.requestWeather(weatherId);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
//        Log.d("NPCMX", "queryProvince");
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);

        mProvinceList = DataSupport.findAll(Province.class);
        if (mProvinceList.size() > 0){
            dataList.clear();
            for (Province province : mProvinceList){
                dataList.add(province.getProvinceName());
            }
            refreshListView();
            currentLevel = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromInternet(address,"province");
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);

        mCityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (mCityList.size()>0){
            dataList.clear();
            for (City city:mCityList){
                dataList.add(city.getCityName());
            }
            refreshListView();
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromInternet(address,"city");
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);

        mCountyList = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (mCountyList.size()>0){
            dataList.clear();
            for (County county:mCountyList){
                dataList.add(county.getCountyName());
            }
            refreshListView();
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromInternet(address,"county");
        }
    }

    private void queryFromInternet(String address,final String type){

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(final Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
//                if ("province".equals(type)){
//                    result = Utility.handleProvinceResponse(responseText);
//                }else if ("city".equals(type)){
//                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
//                }else if ("county".equals(type)){
//                    result = Utility.handleCountryResponse(responseText,selectedCity.getId());
//                }
                switch (type){
                    case "province":
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                        break;
                    case "county":
                        result = Utility.handleCountryResponse(responseText,selectedCity.getId());
                        break;
                    default:
                        break;
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type){
                                case "province":
                                    queryProvinces();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    private void refreshListView(){
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(0);
    }

    private void showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载..");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }

}
