package com.ashok.getweather.api;
/**
 * Created by Ashok on 6/14/2016.
 * Interface file to GET JSON data from web svc website
 */
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//import com.ashok.getweather.pojos.Model;
import com.ashok.getweather.pojos.*;
//import com.ashok.weather_local_n_us.pojos.NameAndId_pojo;

public interface RestApi_Interface {
    @GET("/v1/public/yql?format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
    Call<Model> queryIn_RestApi_Interface(@Query("q") String query);
}
