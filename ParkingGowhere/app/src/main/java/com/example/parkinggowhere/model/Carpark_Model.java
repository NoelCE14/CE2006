package com.example.parkinggowhere.model;

public class Carpark_Model {

    private String car_park_no;
    private String address;
    private String x_coord;
    private String y_coord;
    private String car_park_type;
    private String type_of_parking_system;
    private String short_term_parking;
    private String free_parking;
    private String night_parking;
    private String car_park_decks;
    private String ID;
    private String gantry_height;
    private String car_park_basement;

    public Carpark_Model(){

    }

    // hdb carpark
//    public Carpark_Model(String car_park_no, String address, String x_coord, String y_coord, String car_park_type, String type_of_parking_system, String short_term_parking, String free_parking, String night_parking, String car_park_decks, String gantry_height, String car_park_basement) {
//        this.car_park_no = car_park_no;
//        this.address = address;
//        this.x_coord = x_coord;
//        this.y_coord = y_coord;
//        this.car_park_type = car_park_type;
//        this.type_of_parking_system = type_of_parking_system;
//        this.short_term_parking = short_term_parking;
//        this.free_parking = free_parking;
//        this.night_parking = night_parking;
//        this.car_park_decks = car_park_decks;
//        this.gantry_height = gantry_height;
//        this.car_park_basement = car_park_basement;
//    }
    public Carpark_Model(String short_term_parking, String car_park_type, String y_coord, String x_coord, String free_parking, String gantry_height, String car_park_basement, String night_parking, String address, String car_park_decks, String ID, String car_park_no, String type_of_parking_system){
        this.short_term_parking = short_term_parking;
        this.car_park_type = car_park_type;
        this.y_coord = y_coord;
        this.x_coord = x_coord;
        this.free_parking = free_parking;
        this.gantry_height = gantry_height;
        this.car_park_basement = car_park_basement;
        this.night_parking = night_parking;
        this.address = address;
        this.car_park_decks = car_park_decks;
        this.ID = ID;
        this.car_park_no = car_park_no;
        this.type_of_parking_system = type_of_parking_system;
    }

    //shopping mall carpark
    public Carpark_Model(String car_park_no, String address, String x_coord, String y_coord){
        this.car_park_no = car_park_no;
        this.address = address;
        this.x_coord = x_coord;
        this.y_coord = y_coord;
    }

    public String getCar_park_no() {
        return car_park_no;
    }

    public void setCar_park_no(String car_park_no) {
        this.car_park_no = car_park_no;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getX_coord() {
        return x_coord;
    }

    public void setX_coord(String x_coord) {
        this.x_coord = x_coord;
    }

    public String getY_coord() {
        return y_coord;
    }

    public void setY_coord(String y_coord) {
        this.y_coord = y_coord;
    }

    public String getCar_park_type() {
        return car_park_type;
    }

    public void setCar_park_type(String car_park_type) {
        this.car_park_type = car_park_type;
    }

    public String getType_of_parking_system() {
        return type_of_parking_system;
    }

    public void setType_of_parking_system(String type_of_parking_system) {
        this.type_of_parking_system = type_of_parking_system;
    }

    public String getShort_term_parking() {
        return short_term_parking;
    }

    public void setShort_term_parking(String short_term_parking) {
        this.short_term_parking = short_term_parking;
    }

    public String getFree_parking() {
        return free_parking;
    }

    public void setFree_parking(String free_parking) {
        this.free_parking = free_parking;
    }

    public String getNight_parking() {
        return night_parking;
    }

    public void setNight_parking(String night_parking) {
        this.night_parking = night_parking;
    }

    public String getCar_park_decks() {
        return car_park_decks;
    }

    public void setCar_park_decks(String car_park_decks) {
        this.car_park_decks = car_park_decks;
    }

    public String getGantry_height() {
        return gantry_height;
    }

    public void setGantry_height(String gantry_height) {
        this.gantry_height = gantry_height;
    }

    public String getCar_park_basement() {
        return car_park_basement;
    }

    public void setCar_park_basement(String car_park_basement) {
        this.car_park_basement = car_park_basement;
    }

    public String getID() {
        return ID;
    }
}
