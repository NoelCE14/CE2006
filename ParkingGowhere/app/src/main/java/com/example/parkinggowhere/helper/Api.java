package com.example.parkinggowhere.helper;

import static com.example.parkinggowhere.utils.Const.api;

import com.example.parkinggowhere.model.AvailJson;
import com.example.parkinggowhere.model.AvailJsonShoppingMall;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

public interface Api {
    String BASE_URL = api;
    //@GET("transport/carpark-availability?date_time=2021-08-31T12%3A00%3A00")
    @GET
    Call<AvailJson> getResponse(@Url String url);

    @Headers({"AccountKey: Vn9eKg5pSZOzBKr8S8Of8g=="})
    @GET
    Call<AvailJsonShoppingMall> getMallResponse(@Url String url);

}
