package com.example.parkinggowhere.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class AvailShoppingMallDetails {
    @SerializedName("CarParkID")
    private String carpark_number;
    @SerializedName("Development")
    private String address;
    @SerializedName("Location")
    private String location;
    @SerializedName("AvailableLots")
    private int availableLots;
    @SerializedName("Agency")
    private String agency;



    public AvailShoppingMallDetails(String carpark_number, String address, String availableLots, String location, String agency){
        this.carpark_number = carpark_number;
        this.address = address;
        this.location = location;
        //this.availableLots = availableLots;
        this.availableLots = Integer.getInteger(availableLots);
        this.agency = agency;
    }

    public AvailShoppingMallDetails(String carpark_number, String availableLots){
        this.carpark_number = carpark_number;
        this.availableLots = Integer.getInteger(availableLots);
    }

    public String getCarpark_number (){
        return carpark_number;
    }
    public String getAddress(){
        return address;
    }
    public String getLocation(){
        return location;
    }
    public int getAvailableLots(){
        return availableLots;
    }
    public String getAgency(){
        return agency;
    }

}
