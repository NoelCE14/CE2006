package com.example.parkinggowhere.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.parkinggowhere.Connection;
import com.example.parkinggowhere.R;
import com.example.parkinggowhere.RecordSQLiteOpenHelper;
import com.github.opendevl.JFlat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //Initialise Handler
    Handler handler = new Handler();
    private RecordSQLiteOpenHelper helper = new RecordSQLiteOpenHelper(this);;
    LinearLayout linearLayout;
    // Will show the string "data" that holds the results
    TextView results;
    // URL of object to be parsed
    String url = "https://data.gov.sg/api/action/datastore_search?resource_id=139a3035-e624-4f56-b63f-89ae28d4ae4c";
    // This string will hold the results
    String data = "";
    // Defining the Volley request queue that handles the URL request concurrently
    RequestQueue requestQueue;
    // Total amount of queries in API
    int total;
    boolean networkConnected;
    TextView load_text;

    private static final int STORAGE_PERMISSION_CODE = 101;



    public interface VolleyCallBack{
        void onSuccess();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        super.onCreate(savedInstanceState);
        networkConnected = Connection.isConnected(getApplicationContext());
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        if(isCarparkTableEmpty()){
            if (networkConnected) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initCarparks(new VolleyCallBack(){
                            @Override
                            public void onSuccess() {
                                insertDBCarpark();
                                insertDBShoppingMall();

                            }
                        });
                        initDatamall(new VolleyCallBack(){
                            @Override
                            public void onSuccess() {
                                helper.updateAvailDatasetCarparks();
                                helper.updateAvailDatasetShoppingMall();
                            }
                        });

                        startActivity(new Intent(MainActivity.this, Favourites.class)); //Splash screen upon start up, then show homepage
                        finish(); //Prevent app from going back to splash screen when pressing back button
                    }
                }, 10000); //If new user, give 5s for data to be fetched
            }
            else{
                load_text = (TextView) findViewById(R.id.textView);
                load_text.setTextSize(20);
                load_text.setText("Network unavailable,\nplease try again when connected\nto the Internet");
                //Toast.makeText(MainActivity.this, "Network unavailable, please try again later", Toast.LENGTH_SHORT).show();
//                finish();
//                System.exit(0);
            }

        }
        else{
            networkConnected = Connection.isConnected(getApplicationContext());
            new startupOldUser().execute();
        }


    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isCarparkTableEmpty(){
        Cursor cursor =helper.getReadableDatabase().rawQuery("SELECT * FROM CARPARK_TABLE LIMIT 1", null);
        if (cursor.getCount() == 0){
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void initCarparks(final VolleyCallBack callBack){
        // Creates the Volley request queue
        requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest obreq0 = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject obj = response.getJSONObject("result");
                            total = Integer.valueOf(obj.getString("total"));
                            url = "https://data.gov.sg/api/action/datastore_search?resource_id=139a3035-e624-4f56-b63f-89ae28d4ae4c&limit=" + String.valueOf(total);

                            // Creating the JsonObjectRequest class called obreq, passing required parameters:
                            //GET is used to fetch data from the server, JsonURL is the URL to be fetched from.
                            JsonObjectRequest obreq = new JsonObjectRequest
                                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                        // Takes the response from the JSON request
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                JSONObject obj = response.getJSONObject("result");
                                                JSONArray obj2 = obj.getJSONArray("records");
                                                String str = new String(obj2.toString());

                                                JFlat flatMe = new JFlat(str);
                                                //Carpark_Model carpark = new Carpark_Model();
                                                String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator+"carpark.csv";
                                                flatMe.json2Sheet().write2csv(rootPath);

                                                callBack.onSuccess();

                                            }
                                            // Try and catch are included to handle any errors due to JSON
                                            catch (JSONException e) {
                                                // If an error occurs, this prints the error to the log
                                                e.printStackTrace();
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                            // The final parameter overrides the method onErrorResponse() and passes VolleyError
                                            //as a parameter
                                            new Response.ErrorListener() {
                                                @Override
                                                // Handles errors that occur due to Volley
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.e("Volley", "Error");
                                                }
                                            }
                                    );
                            requestQueue.add(obreq);
                        }
                        // Try and catch are included to handle any errors due to JSON
                        catch (JSONException e) {
                            // If an error occurs, this prints the error to the log
                            e.printStackTrace();
                        }
                    }
                },
                        // The final parameter overrides the method onErrorResponse() and passes VolleyError
                        //as a parameter
                        new Response.ErrorListener() {
                            @Override
                            // Handles errors that occur due to Volley
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Volley", "Error");
                            }
                        }
                );
        // Adds the JSON object request "obreq0" to the request queue
        requestQueue.add(obreq0);
    }

    public void initDatamall(VolleyCallBack callBack){
        requestQueue = Volley.newRequestQueue(this);
        url = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2";
        JsonObjectRequest obreq = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray value = response.getJSONArray("value");
                            String str = new String(value.toString());

                            JFlat flatMe = new JFlat(str);
                            String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+File.separator+"datamall.csv";
                            flatMe.json2Sheet().write2csv(rootPath);
                            callBack.onSuccess();
                        }
                        // Try and catch are included to handle any errors due to JSON
                        catch (JSONException e) {
                            // If an error occurs, this prints the error to the log
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                },
                // The final parameter overrides the method onErrorResponse() and passes VolleyError
                //as a parameter
                new Response.ErrorListener() {
                    @Override
                    // Handles errors that occur due to Volley
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AccountKey", "Vn9eKg5pSZOzBKr8S8Of8g==");
                return headers;
            }
        };

        // Adds the JSON object request "obreq" to the request queue
        requestQueue.add(obreq);
    }

    public void insertDBCarpark(){
        String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+File.separator+"carpark.csv";
        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new FileReader(rootPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            helper.insertCarparkDataset(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertDBShoppingMall(){
        String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+File.separator+"datamall.csv";
        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new FileReader(rootPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            helper.insertShoppingDataset(buffer,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class startupOldUser extends AsyncTask<String, Void, Void>
    {

        @Override
        protected Void doInBackground(String... params) {
            if(networkConnected){
                helper.updateAvailDatasetCarparksFav();
                helper.updateAvailDatasetShoppingMallFav();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(networkConnected)
                        helper.updateFavouriteAvail();
                    else
                        Toast.makeText(MainActivity.this, "Network unavailable, some functions will be unavailable", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, Favourites.class)); //Splash screen upon start up, then show homepage
                    finish(); //Prevent app from going back to splash screen when pressing back button
                }
            }, 3000);
        }
    }


}

