package com.example.parkinggowhere;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.example.parkinggowhere.helper.RetrofitClient;
import com.example.parkinggowhere.model.AvailDetails;
import com.example.parkinggowhere.model.AvailJson;
import com.example.parkinggowhere.model.AvailJsonShoppingMall;
import com.example.parkinggowhere.model.AvailShoppingMallDetails;
import com.example.parkinggowhere.model.Carpark_Model;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.parkinggowhere.utils.Const.*;

import androidx.annotation.RequiresApi;

public class RecordSQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String CARPARK_TABLE = "CARPARK_TABLE";
    public static final String CAR_PARK_NO = "CAR_PARK_NO";
    public static final String ADDRESS = "ADDRESS";
    public static final String X_COORD = "X_COORD";
    public static final String Y_COORD = "Y_COORD";
    public static final String CAR_PARK_TYPE = "CAR_PARK_TYPE";
    public static final String TYPE_OF_PARKING_SYSTEM = "TYPE_OF_PARKING_SYSTEM";
    public static final String SHORT_TERM_PARKING = "SHORT_TERM_PARKING";
    public static final String FREE_PARKING = "FREE_PARKING";
    public static final String NIGHT_PARKING = "NIGHT_PARKING";
    public static final String CAR_PARK_DECKS = "CAR_PARK_DECKS";
    public static final String GANTRY_HEIGHT = "GANTRY_HEIGHT";
    public static final String CAR_PARK_BASEMENT = "CAR_PARK_BASEMENT";
    public static final String HISTORY_TABLE = "HISTORY_TABLE";
    public static final String ID = "ID";
    private static String name = "carparks.db";
    private static Integer version = 1;

    public RecordSQLiteOpenHelper(Context context) {
        super(context, name, null, version);
    }
    public String[] fromColNames = new String[]{ADDRESS, LABEL, LOTS_AVAIL, CAR_PARK_NO, TOTAL_LOTS};
    public int[] toViewIDs = new int[]{R.id.address, R.id.label, R.id.img_avail, R.id.no, R.id.total_lots};


    @Override
    public void onCreate(SQLiteDatabase db) {

        String createCarparkTable = "CREATE TABLE IF NOT EXISTS " + CARPARK_TABLE +
                "(" + SHORT_TERM_PARKING + " TEXT, " + CAR_PARK_TYPE + " TEXT, " +
                Y_COORD + " TEXT, " + X_COORD + " TEXT, " + FREE_PARKING + " TEXT, " + GANTRY_HEIGHT + " TEXT, " +
                CAR_PARK_BASEMENT + " TEXT, " + NIGHT_PARKING + " TEXT, " + ADDRESS + " TEXT, " +
                CAR_PARK_DECKS + " TEXT, " + " ID INTEGER PRIMARY KEY, " + CAR_PARK_NO + " TEXT," + TYPE_OF_PARKING_SYSTEM + " TEXT)";

        db.execSQL(createCarparkTable);

        String createHistoryTable = "CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE +
                "(" + SHORT_TERM_PARKING + " TEXT, " + CAR_PARK_TYPE + " TEXT, " +
                Y_COORD + " TEXT, " + X_COORD + " TEXT, " + FREE_PARKING + " TEXT, " + GANTRY_HEIGHT + " TEXT, " +
                CAR_PARK_BASEMENT + " TEXT, " + NIGHT_PARKING + " TEXT, " + ADDRESS + " TEXT, " +
                CAR_PARK_DECKS + " TEXT, " + " ID INTEGER PRIMARY KEY AUTOINCREMENT, " + CAR_PARK_NO + " TEXT," + TYPE_OF_PARKING_SYSTEM + " TEXT)";

        db.execSQL(createHistoryTable);

        String createFavouriteTable = "CREATE TABLE " + FAVOURITE_TABLE +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + ADDRESS + " TEXT, " +
                CAR_PARK_NO + " TEXT, " + TOTAL_LOTS + " TEXT, " + LOTS_AVAIL + " TEXT, " + LABEL + " TEXT)";

//        String createFavouriteTable = "CREATE TABLE IF NOT EXISTS " + FAVOURITE_TABLE +
//                "(" + SHORT_TERM_PARKING + " TEXT, " + CAR_PARK_TYPE + " TEXT, " +
//                Y_COORD + " TEXT, " + X_COORD + " TEXT, " + FREE_PARKING + " TEXT, " + GANTRY_HEIGHT + " TEXT, " +
//                CAR_PARK_BASEMENT + " TEXT, " + NIGHT_PARKING + " TEXT, " + ADDRESS + " TEXT, " +
//                CAR_PARK_DECKS + " TEXT, " + " ID INTEGER PRIMARY KEY AUTOINCREMENT, " + CAR_PARK_NO + " TEXT," + TYPE_OF_PARKING_SYSTEM + " TEXT," + LABEL + " TEXT)";

        db.execSQL(createFavouriteTable);

        String createAvailTable = "CREATE TABLE " + AVAILABLE_TABLE +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " + CAR_PARK_NO + " TEXT, " +
                TOTAL_LOTS + " TEXT, " + LOT_TYPE + " TEXT, " + LOTS_AVAIL + " TEXT)";

        db.execSQL(createAvailTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteCarparkTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + CARPARK_TABLE);
    }

    public void deleteHistoryTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + HISTORY_TABLE);
    }

    public void deleteFavouriteTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + FAVOURITE_TABLE);
        db.execSQL("delete from sqlite_sequence where name='"+ FAVOURITE_TABLE +"'");

    }

    public void deleteAvailTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + AVAILABLE_TABLE);
        db.execSQL("delete from sqlite_sequence where name='"+ AVAILABLE_TABLE +"'");

    }

    // after every refresh, update the availDataset
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateAvailDatasetCarparks(){
        deleteAvailTable();

//        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
//        timeStamp = timeStamp.replace("_", "T");
//        timeStamp = timeStamp.replace(":", "%3A");
        ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneId.of("Singapore"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss");
        String strDate = zonedDateTimeNow.format(formatter);
        String url = availability + "date_time=" + strDate;
        Call<AvailJson> call = RetrofitClient.getInstance().getMyApi().getResponse(url);
        call.enqueue(new Callback<AvailJson>() {
            @Override
            public void onResponse(Call<AvailJson> call, Response<AvailJson> response) {
                Gson gson = new Gson();
                AvailJson json = response.body();
                JsonArray j = json.getName();
                for (int i = 0; i<j.size();i++){
                    AvailDetails a = gson.fromJson(j.get(i), AvailDetails.class);
                    addAvail(a);
                }
            }

            @Override
            public void onFailure(Call<AvailJson> call, Throwable t) {

            }

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateAvailDatasetCarparksFav(){
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT ID as _id, ADDRESS, LABEL, LOTS_AVAIL, CAR_PARK_NO, TOTAL_LOTS FROM FAVOURITE_TABLE", null);
        try{
            if (cursor.moveToFirst()) {
                do {
                    String no = cursor.getString(cursor.getColumnIndex(CAR_PARK_NO));
                    ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneId.of("Singapore"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss");
                    String strDate = zonedDateTimeNow.format(formatter);
                    String url = availability + "date_time=" + strDate;
                    Call<AvailJson> call = RetrofitClient.getInstance().getMyApi().getResponse(url);
                    call.enqueue(new Callback<AvailJson>() {
                        @Override
                        public void onResponse(Call<AvailJson> call, Response<AvailJson> response) {
                            Gson gson = new Gson();
                            AvailJson json = response.body();
                            JsonArray j = json.getName();
                            int lots_available = 0;
                            for (int i = 0; i < j.size(); i++) {
                                AvailDetails a = gson.fromJson(j.get(i), AvailDetails.class);
                                if (a.getCarpark_number().contentEquals(no)) {
                                    //addAvail(a);
                                    lots_available = Integer.valueOf(a.getLots_available());
                                    updateOneAvail(a.getCarpark_number(), lots_available);
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<AvailJson> call, Throwable t) {

                        }

                    });
                }while(cursor.moveToNext());
            }
        }finally{
            cursor.close();
        }

    }

    public void updateAvailDatasetShoppingMall(){

        String url = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2";
        Call<AvailJsonShoppingMall> call = RetrofitClient.getInstance().getMyApi().getMallResponse(url);
        call.enqueue(new Callback<AvailJsonShoppingMall>() {
            @Override
            public void onResponse(Call<AvailJsonShoppingMall> call, Response<AvailJsonShoppingMall> response) {
                Gson gson = new Gson();
                AvailJsonShoppingMall json = response.body();
                JsonArray j = json.getData();
                for (int i = 0; i<j.size();i++){
                    AvailShoppingMallDetails a = gson.fromJson(j.get(i), AvailShoppingMallDetails.class);
                    addAvailShoppingMall(a);
                }
            }

            @Override
            public void onFailure(Call<AvailJsonShoppingMall> call, Throwable t) {

            }

        });
    }

    public void updateAvailDatasetShoppingMallFav(){
        //String no = "";
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT ID as _id, ADDRESS, LABEL, LOTS_AVAIL, CAR_PARK_NO, TOTAL_LOTS FROM FAVOURITE_TABLE", null);
        try{
            if (cursor.moveToFirst()) {
                do{
                String no = cursor.getString(cursor.getColumnIndex(CAR_PARK_NO));
                String url = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2";
                Call<AvailJsonShoppingMall> call = RetrofitClient.getInstance().getMyApi().getMallResponse(url);
                call.enqueue(new Callback<AvailJsonShoppingMall>() {
                    @Override
                    public void onResponse(Call<AvailJsonShoppingMall> call, Response<AvailJsonShoppingMall> response) {
                        Gson gson = new Gson();
                        AvailJsonShoppingMall json = response.body();
                        JsonArray j = json.getData();
                        for (int i = 0; i<j.size();i++){
                            AvailShoppingMallDetails a = gson.fromJson(j.get(i), AvailShoppingMallDetails.class);
                            if(a.getCarpark_number().contentEquals(no))
                                //addAvailShoppingMall(a);
                                updateOneAvail(a.getCarpark_number(),a.getAvailableLots());
                        }
                    }

                    @Override
                    public void onFailure(Call<AvailJsonShoppingMall> call, Throwable t) {

                    }

                });
                }while(cursor.moveToNext());
            }
        }finally{
            cursor.close();
        }


    }

    public void insertCarparkDataset(BufferedReader buffer) throws IOException {

        String line = "";
        int counter = -1;
        while ((line = buffer.readLine()) != null) {
            //line = line.replace("\"", "");
            String otherThanQuote = " [^\"] ";
            String quotedString = String.format(" \" %s* \" ", otherThanQuote);
            String regex = String.format("(?x) "+ // enable comments, ignore white spaces
                            ",                         "+ // match a comma
                            "(?=                       "+ // start positive look ahead
                            "  (?:                     "+ //   start non-capturing group 1
                            "    %s*                   "+ //     match 'otherThanQuote' zero or more times
                            "    %s                    "+ //     match 'quotedString'
                            "  )*                      "+ //   end group 1 and repeat it zero or more times
                            "  %s*                     "+ //   match 'otherThanQuote'
                            "  $                       "+ // match the end of the string
                            ")                         ", // stop positive look ahead
                    otherThanQuote, quotedString, otherThanQuote);
            String[] str = line.split(regex, -1);
            for(int i = 0; i < 13; i++){
                str[i] = str[i].replace("\"","");
            }
            if (str[1].equals("/car_park_type"))
                continue;
//            Carpark_Model carpark = new Carpark_Model(str[11], str[8], str[3], str[2], str[1], str[13],
//                    str[0], str[4], str[7], str[9], str[5], str[6]);
            Carpark_Model carpark = new Carpark_Model(str[0], str[1], str[2], str[3], str[4], str[5],
                    str[6], str[7], str[8], str[9], str[10], str[11], str[12]);
            addOneCarpark(carpark);
        }

        buffer.close();
    }

    public void insertShoppingDataset(BufferedReader buffer, boolean R) throws IOException {
        String line = "";
        while ((line = buffer.readLine()) != null) {
            String[] str = line.split("\",\"");
            if (str.length <= 1)
                continue;
            if (str[1].equals(""))
                break;
            if (R) {
                String[] xy = str[3].split("\"")[0].split(" ");
                Carpark_Model carpark = new Carpark_Model(str[0].substring(1), str[2], xy[0], xy[1]);
                addOneCarpark(carpark);
            }
            else{
                String availLots = str[3].split("\"")[1];
                AvailDetails avail = new AvailDetails(str[0].substring(1), availLots.substring(1, availLots.length()-1));
                addAvail(avail);
            }
        }

        buffer.close();
    }

    public boolean addOneCarpark (Carpark_Model carpark){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(SHORT_TERM_PARKING, carpark.getShort_term_parking());
        cv.put(CAR_PARK_TYPE, carpark.getCar_park_type());
        cv.put(Y_COORD, carpark.getY_coord());
        cv.put(X_COORD, carpark.getX_coord());
        cv.put(FREE_PARKING, carpark.getFree_parking());
        cv.put(GANTRY_HEIGHT, carpark.getGantry_height());
        cv.put(CAR_PARK_BASEMENT, carpark.getCar_park_basement());
        cv.put(NIGHT_PARKING, carpark.getNight_parking());
        cv.put(ADDRESS, carpark.getAddress());
        cv.put(CAR_PARK_DECKS, carpark.getCar_park_decks());
        cv.put(ID, carpark.getID());
        cv.put(CAR_PARK_NO, carpark.getCar_park_no());
        cv.put(TYPE_OF_PARKING_SYSTEM, carpark.getType_of_parking_system());


        long insert = db.insert(CARPARK_TABLE, null, cv);

        if (insert == -1){
            return false;
        }
        return true;

    }

    public boolean addAvail(AvailDetails a){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CAR_PARK_NO, a.getCarpark_number());
        if (a.getInfo() != null) {
            cv.put(TOTAL_LOTS, a.getTotal_lots());
            cv.put(LOT_TYPE, a.getLot_type());
        }
        cv.put(LOTS_AVAIL, a.getLots_available());

        long insert = db.insert(AVAILABLE_TABLE, null, cv);

        if (insert == -1){
            return false;
        }
        return true;
    }

    public boolean addAvailShoppingMall(AvailShoppingMallDetails a){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long insert = -1;
        if (a.getAgency().contentEquals("LTA")) {
            cv.put(CAR_PARK_NO, a.getCarpark_number());
            cv.put(TOTAL_LOTS, "NA");
            cv.put(LOT_TYPE, "NA");
            cv.put(LOTS_AVAIL, a.getAvailableLots());
            insert = db.insert(AVAILABLE_TABLE, null, cv);
        }

        if (insert == -1){
            return false;
        }
        return true;
    }

    public boolean addOneHistory (Carpark_Model carpark){
        if (historyHasData(carpark.getCar_park_no())){
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CAR_PARK_NO, carpark.getCar_park_no());
        cv.put(ADDRESS, carpark.getAddress());
        cv.put(X_COORD, carpark.getX_coord());
        cv.put(Y_COORD, carpark.getY_coord());
        cv.put(CAR_PARK_TYPE, carpark.getCar_park_type());
        cv.put(TYPE_OF_PARKING_SYSTEM, carpark.getType_of_parking_system());
        cv.put(SHORT_TERM_PARKING, carpark.getShort_term_parking());
        cv.put(FREE_PARKING, carpark.getFree_parking());
        cv.put(NIGHT_PARKING, carpark.getNight_parking());
        cv.put(CAR_PARK_DECKS, carpark.getCar_park_decks());
        cv.put(GANTRY_HEIGHT, carpark.getGantry_height());
        cv.put(CAR_PARK_BASEMENT, carpark.getCar_park_basement());

        long insert = db.insert(HISTORY_TABLE, null, cv);

        if (insert == -1){
            return false;
        }
        return true;

    }

    public boolean addOneFavourite(String address, String label){
        if (favouriteHasData(address)){
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = getReadableDatabase().rawQuery(
                "select * from CARPARK_TABLE WHERE ADDRESS='" + address + "'", null);
        String no = null;
        String lots_avail = "NA";
        String total_lots = "NA";
        if (cursor.moveToFirst()) {
            no = cursor.getString(cursor.getColumnIndex(CAR_PARK_NO));
        }
        Cursor cursorAvail = getReadableDatabase().rawQuery(
                "select * from AVAILABLE_TABLE WHERE CAR_PARK_NO='" + no + "'", null);
        if (cursorAvail.moveToFirst()) {
            lots_avail = cursorAvail.getString(cursorAvail.getColumnIndex(LOTS_AVAIL));
            total_lots = cursorAvail.getString(cursorAvail.getColumnIndex(TOTAL_LOTS));

        }

        ContentValues cv = new ContentValues();
        cv.put(ADDRESS, address);
        cv.put(LABEL, label);
        cv.put(CAR_PARK_NO, no);
        cv.put(TOTAL_LOTS, total_lots);
        cv.put(LOTS_AVAIL, lots_avail);
        long insert = db.insert(FAVOURITE_TABLE, null, cv);

        if (insert == -1){
            return false;
        }
        return true;
    }

    public boolean historyHasData(String no){
        // check whether the data already in table
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM HISTORY_TABLE WHERE CAR_PARK_NO='" + no + "'", null);
        if (cursor.getCount() == 0){
            return false;
        }
        return true;
    }

    public boolean favouriteHasData(String address){
        // check the data already in table
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM FAVOURITE_TABLE WHERE ADDRESS='" + address + "'", null);
        if (cursor.getCount() == 0){
            return false;
        }
        return true;
    }

    public void removeFromHistory(String no){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM HISTORY_TABLE WHERE CAR_PARK_NO='"+ no +"'");
    }

    public void removeFromFavourite(String address){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM FAVOURITE_TABLE WHERE ADDRESS='"+ address +"'");
    }

    public void removeFromFavouriteNo(String no){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM FAVOURITE_TABLE WHERE CAR_PARK_NO='"+ no +"'");
    }

    public Cursor getData(String table, String no) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "SELECT * FROM " + table + " WHERE CAR_PARK_NO ='"+ no +"'" , null );
        return res;
    }

    public boolean checkFavourite(String no){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT 1 FROM FAVOURITE_TABLE WHERE CAR_PARK_NO ='" + no + "'", null);
        if(res.getCount() <= 0){
            res.close();
            return false;
        }
        res.close();
        return true;
    }

    public void updateFavouriteAvail(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = getReadableDatabase().rawQuery(
                "select * from FAVOURITE_TABLE", null);

        if (cursor.moveToFirst()) {
            do {
                String no = cursor.getString(cursor.getColumnIndex(CAR_PARK_NO));

                Cursor cursorAvail = getReadableDatabase().rawQuery(
                        "select * from AVAILABLE_TABLE WHERE CAR_PARK_NO='" + no + "'", null);
                if (cursorAvail.moveToFirst()) {
                    String lots_avail = cursorAvail.getString(cursorAvail.getColumnIndex(LOTS_AVAIL));
                    String total_lots = cursorAvail.getString(cursorAvail.getColumnIndex(TOTAL_LOTS));
                    db.execSQL("UPDATE FAVOURITE_TABLE SET LOTS_AVAIL = '" + lots_avail + "' WHERE CAR_PARK_NO = '" + no + "'");
                    db.execSQL("UPDATE FAVOURITE_TABLE SET TOTAL_LOTS = '" + total_lots + "' WHERE CAR_PARK_NO = '" + no + "'");
                }
            }while (cursor.moveToNext());

        }

    }

    public void updateOneFavouriteAvail(String no){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursorAvail = getReadableDatabase().rawQuery(
                "select * from AVAILABLE_TABLE WHERE CAR_PARK_NO='" + no + "'", null);

        if (cursorAvail.moveToFirst()) {
            String lots_avail = cursorAvail.getString(cursorAvail.getColumnIndex(LOTS_AVAIL));
            db.execSQL("UPDATE FAVOURITE_TABLE SET LOTS_AVAIL = '" + lots_avail + "' WHERE CAR_PARK_NO = '" + no + "'");
        }

    }

    public void updateOneAvail(String no, int lots_available){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursorAvail = getReadableDatabase().rawQuery(
                "select * from AVAILABLE_TABLE WHERE CAR_PARK_NO='" + no + "'", null);

        if(cursorAvail.moveToFirst()){
            db.execSQL("UPDATE AVAILABLE_TABLE SET LOTS_AVAIL ='" + lots_available + "' WHERE CAR_PARK_NO = '" + no +"'");
        }

    }


}
