package com.example.parkinggowhere.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.example.parkinggowhere.model.Carpark_Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the DisplayInfo Control Object
 * The DisplayInfo class is responsible for coordinating functions used in the DisplayInfo page
 *
 * The DisplayInfo page displays the relevant information on the selected car park.
 * If the selected car park has electronic parking, the available lots are displayed in the page as well.
 * The page contains a favourite button, when pressed,
 * it prompts the user to add the car park to the favourites data base.
 * The page also contains a navigation button, when pressed,
 * directs the application to google maps navigation, with map route set to the car park's location.
 *
 * This class calls another control class, RecordSQLiteOpenHelper,
 * to interact with the favourites table and car park data bases.
 *
 *  When the user selects the "Add to Favourites" button,
 *  if the car park is already exists within the favourites data base,
 *  the helper is invoked to remove the selection from the favourites data base.
 *  Else, the helper is invoked to add the selection to the favourites data base.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class DisplayInfoActivity extends AppCompatActivity {

    private RecordSQLiteOpenHelper helper = new RecordSQLiteOpenHelper(this);;
    private Menu mMenu;
    String value = "";
    int lots_available = 0, carpark_data_index = 0;
    String from = "", no = "";
    TextView features, lotsAvailable, description;
    ConstraintLayout lotsContainer, descContainer;
    boolean isFavourite;
    boolean isMall = false;

    Carpark_Model carpark;

    RequestQueue requestQueue;
    ZonedDateTime zonedDateTimeNow = ZonedDateTime.now(ZoneId.of("Singapore"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss");
    String strDate = zonedDateTimeNow.format(formatter);
    Boolean found = false;
    boolean networkConnected;


    String url = "https://api.data.gov.sg/v1/transport/carpark-availability?date_time=" + strDate;

    public interface VolleyCallBack{
        void onSuccess();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        no = intent.getStringExtra("key");
        from = intent.getStringExtra("from");
        networkConnected = Connection.isConnected(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displayinfo);

        isFavourite = helper.checkFavourite(no);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);

        initData();
        printData();

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);
        mMenu = menu;
        menu.findItem(R.id.favourites_refresh).setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isFavourite)
            menu.findItem(R.id.info_favourite).setIcon(R.drawable.ic_favourites);
        else
            menu.findItem(R.id.info_favourite).setIcon(R.drawable.ic_favourites_unfilled);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = getIntent();
        no = intent.getStringExtra("key");
        from = intent.getStringExtra("from");
        switch (item.getItemId()) {
            case R.id.info_favourite:
                if(isFavourite){
                    removeFavourite(no);
                }
                else {
                    whetherFavourite(carpark.getAddress());
                }
                return true;
            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialises the carpark data by receiving the parsed data from the previous activity and going through the carpark database to find the corresponding entry
     * If the carpark does not support the getting lots availability function, the description will be extended to compensate for the extra screen space
     * API calls to the respective urls for carparks under HDB or LTA will be made depending on if they are a shopping mall or not
     */
    public void initData(){
        Intent intent = getIntent();
        no = intent.getStringExtra("key");
        from = intent.getStringExtra("from");
        lotsContainer = (ConstraintLayout) findViewById(R.id.lotsContainer);
        descContainer = (ConstraintLayout) findViewById(R.id.descContainer);
        lotsAvailable = (TextView) findViewById(R.id.lotsAvailable);
        networkConnected = Connection.isConnected(getApplicationContext());
        if(!no.contentEquals("")){
            Cursor rs = helper.getData("CARPARK_TABLE", no);
            rs.moveToFirst();
            try{
                if(Integer.valueOf(no) > 0)
                    isMall = true;
            }catch(Exception e){

            }
            String[] data = new String[13];
            for(int i = 0; i < 13; i++){
                data[i] = rs.getString(i);
            }
            if(isMall){
                carpark = new Carpark_Model(data[11], data[8], data[2], data[3]);
            }
            else{
                carpark = new Carpark_Model(data[0],data[1],data[2],data[3],data[4],data[5],data[6],
                        data[7],data[8],data[9],data[10],data[11],data[12]);
            }
            setTitle(carpark.getAddress());
            if(!isMall){
                if(carpark.getType_of_parking_system().contentEquals("COUPON PARKING")){
                    ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) descContainer.getLayoutParams();
                    lotsContainer.setVisibility(View.GONE);
                    lp.height = 550;
                    descContainer.setLayoutParams(lp);
                }
                else{
                    if(networkConnected){
                        requestQueue = Volley.newRequestQueue(this);
                        JsonObjectRequest obreq = new JsonObjectRequest
                                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                    // Takes the response from the JSON request
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONArray items = response.getJSONArray("items");
                                            JSONObject items1 = items.getJSONObject(0);
                                            JSONArray carpark_data = items1.getJSONArray("carpark_data");
                                            String carpark_number = "";
                                            while(!carpark_data.isNull(carpark_data_index)){
                                                JSONObject data = carpark_data.getJSONObject(carpark_data_index);
                                                carpark_number = data.getString("carpark_number");
                                                String test = carpark.getCar_park_no();
                                                if(carpark_number.equals(test)) {
                                                    JSONArray carpark_infoA = data.getJSONArray("carpark_info");
                                                    JSONObject carpark_info = carpark_infoA.getJSONObject(0);
                                                    lots_available = carpark_info.getInt("lots_available");
                                                    found = true;
                                                    break;
                                                }
                                                carpark_data_index++;
                                            }
                                            lotsAvailable = (TextView) findViewById(R.id.lotsAvailable);
                                            if(found){
                                                lotsAvailable.setText(String.valueOf(lots_available));
                                                helper.updateOneAvail(no, lots_available);
                                                if(isFavourite)
                                                    helper.updateFavouriteAvail();
                                            }
                                            else
                                                lotsAvailable.setText("Error loading!");


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
                        requestQueue.add(obreq);
                    }
                    else{
                        lotsAvailable.setText("Offline");
                        Toast.makeText(DisplayInfoActivity.this, "Network unavailable, please try again later", Toast.LENGTH_SHORT).show();
                    }

                }
                rs.close();
            }
            else{
                if(networkConnected){
                    requestQueue = Volley.newRequestQueue(this);
                    url = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2";
                    JsonObjectRequest obreq = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>()
                            {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONArray value = response.getJSONArray("value");
                                        String carpark_number = "";
                                        while(!value.isNull(carpark_data_index)){
                                            JSONObject data = value.getJSONObject(carpark_data_index);
                                            carpark_number = data.getString("CarParkID");
                                            String test = carpark.getCar_park_no();
                                            if(carpark_number.equals(test)) {
                                                lots_available = data.getInt("AvailableLots");
                                                found = true;
                                                break;
                                            }
                                            carpark_data_index++;
                                        }
                                        lotsAvailable = (TextView) findViewById(R.id.lotsAvailable);
                                        if(found){
                                            lotsAvailable.setText(String.valueOf(lots_available));
                                            helper.updateOneAvail(no, lots_available);
                                            if(isFavourite)
                                                helper.updateFavouriteAvail();
                                        }

                                        else
                                            lotsAvailable.setText("Error loading!");
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
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            //headers.put("Content-Type", "application/json; charset=utf-8");
                            headers.put("AccountKey", "Vn9eKg5pSZOzBKr8S8Of8g==");
                            //headers.put("Host", "localhost");


                            return headers;
                        }
                    };

                    // Adds the JSON object request "obreq" to the request queue
                    requestQueue.add(obreq);
                }
                else{
                    lotsAvailable.setText("Offline");
                    Toast.makeText(DisplayInfoActivity.this, "Network unavailable, please try again later", Toast.LENGTH_SHORT).show();
                }

            }


        }
    }

    public void printData(){
        features = (TextView) findViewById(R.id.features);
        description = (TextView) findViewById(R.id.description);
        if(!isMall){
            features.setText(carpark.getCar_park_type() + " • " + carpark.getType_of_parking_system());
            String descriptionString = "<b>Car Park No:</b><br>" + carpark.getCar_park_no()
                    + "<br><br> <b>Free parking:</b> <br>" + carpark.getFree_parking()
                    + "<br><br> <b>Short-term parking:</b><br>" + carpark.getShort_term_parking()
                    + "<br><br> <b>Night parking:</b><br>" + carpark.getNight_parking();
            description.setText(Html.fromHtml(descriptionString));
        }
        else{
            features.setText("Shopping Mall");
            String descriptionString = "<b>Car Park No:</b><br>" + carpark.getCar_park_no();
            description.setText(Html.fromHtml(descriptionString));
        }




    }

    public void getLots(final VolleyCallBack callBack){
        if(!isMall){
            requestQueue = Volley.newRequestQueue(this);
            lotsAvailable = (TextView) findViewById(R.id.lotsAvailable);
            JsonObjectRequest obreq = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        // Takes the response from the JSON request
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray itemsA = response.getJSONArray("items");
                                JSONObject items = itemsA.getJSONObject(0);
                                JSONArray carpark_data = items.getJSONArray("carpark_data");
                                JSONObject data = carpark_data.getJSONObject(carpark_data_index);
                                JSONArray carpark_infoA = data.getJSONArray("carpark_info");
                                JSONObject carpark_info = carpark_infoA.getJSONObject(0);
                                lots_available = carpark_info.getInt("lots_available");
                                lotsAvailable.setText(String.valueOf(lots_available));
                                callBack.onSuccess();
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
            // Adds the JSON object request "obreq" to the request queue
            requestQueue.add(obreq);
        }
        else{
            requestQueue = Volley.newRequestQueue(this);
            url = "http://datamall2.mytransport.sg/ltaodataservice/CarParkAvailabilityv2";
            JsonObjectRequest obreq = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray value = response.getJSONArray("value");
                                JSONObject data = value.getJSONObject(carpark_data_index);
                                lots_available = data.getInt("AvailableLots");
                                lotsAvailable.setText(String.valueOf(lots_available));
                                callBack.onSuccess();
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
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    //headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("AccountKey", "Vn9eKg5pSZOzBKr8S8Of8g==");
                    //headers.put("Host", "localhost");


                    return headers;
                }
            };

            // Adds the JSON object request "obreq" to the request queue
            requestQueue.add(obreq);
        }

    }

    public void refresh(View view) {
        networkConnected = Connection.isConnected(getApplicationContext());
        lotsAvailable = (TextView) findViewById(R.id.lotsAvailable);
        if(networkConnected){
            lotsAvailable.setText("Loading...");
            getLots(new VolleyCallBack(){
                @Override
                public void onSuccess() {
                    helper.updateOneAvail(no, lots_available);
                    if(isFavourite)
                        helper.updateFavouriteAvail();

                }
            });
        }
        else{
            lotsAvailable.setText("Offline");
            Toast.makeText(DisplayInfoActivity.this, "Network unavailable, please try again later", Toast.LENGTH_SHORT).show();
        }



    }

    public void getDirections(View view){
        String address = carpark.getAddress();
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q="+address+"&mode=d")); //For modes, d = driver, b = bicycle, w = walking
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    // query for whether add into favourite
    public void whetherFavourite(String address){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to favourite?");

        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addLabel(address);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Add get Label
    public void addLabel(String address){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please set a label (e.g work)");
        View view = getLayoutInflater().inflate(R.layout.label_dialog, null);
        // Set up the input
        final EditText input = view.findViewById(R.id.input);
        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                helper.addOneFavourite(address, m_Text);
                mMenu.findItem(R.id.info_favourite).setIcon(R.drawable.ic_favourites);
                isFavourite = true;
                helper.updateFavouriteAvail();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void removeFavourite(String no){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove from favourites?");

        // Set up the buttons
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                helper.removeFromFavouriteNo(no);
                mMenu.findItem(R.id.info_favourite).setIcon(R.drawable.ic_favourites_unfilled);
                isFavourite = false;
//                queryFavourite();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }
}
