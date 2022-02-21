package com.example.parkinggowhere.model;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class AvailJsonShoppingMall {
    @SerializedName("value")
    private JsonArray value;

    public AvailJsonShoppingMall(JsonArray value) {
        this.value = value;
    }

    public JsonArray getData(){
        return value;
    }
}