package com.example.parkinggowhere.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class AvailJson {
    @SerializedName("items")
    private JsonArray items;


    public AvailJson(JsonArray item) {
        this.items = items;
    }

    public JsonArray getName() {
        return (JsonArray) ((JsonObject) items.get(0)).get("carpark_data");
    }
}
