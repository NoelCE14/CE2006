package com.example.parkinggowhere.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class AvailDetails {
    @SerializedName("carpark_info")
    private JsonArray info;
    @SerializedName("carpark_number")
    private String carpark_number;
    @SerializedName("update_datetime")
    private String up_time;

    private String total_lots;
    private String lot_type;
    private String lots_available;


    public AvailDetails (JsonArray info, String carpark_number, String up_time){
        this.carpark_number = carpark_number;
        this.info = info;
        this.up_time = up_time;
    }

    public AvailDetails (String carpark_number, String lots_available){
        this.carpark_number = carpark_number;
        this.lots_available = lots_available;
    }

    public String getCarpark_number (){
        return carpark_number;
    }
    public String getUp_time(){
        return up_time;
    }
    public JsonArray getInfo(){
        return info;
    }

    public String getTotal_lots(){
        return ((JsonObject)info.get(0)).get("total_lots").getAsString();
    }
    public String getLot_type(){
        return ((JsonObject)info.get(0)).get("lot_type").getAsString();
    }
    public String getLots_available(){
        if (this.info == null){
            return lots_available;
        }
        return ((JsonObject)info.get(0)).get("lots_available").getAsString();
    }
}
