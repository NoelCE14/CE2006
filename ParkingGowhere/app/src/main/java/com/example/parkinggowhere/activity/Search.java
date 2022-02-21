package com.example.parkinggowhere.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.parkinggowhere.utils.Const.*;

import com.example.parkinggowhere.MyListView;
import com.example.parkinggowhere.R;
import com.example.parkinggowhere.RecordSQLiteOpenHelper;
import com.example.parkinggowhere.model.Carpark_Model;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * This class implements the Search Control Object
 * The Search class is responsible for coordinating functions used in the Search page
 *
 * The Search page contains a search bar for the user to input search queries,
 * thereafter the page would display the matched result(s) if any.
 * If there are no matching results,
 * an option is made available for the user to direct his query google maps instead.
 * The page also contains a search history,
 * which displays previously selected car park(s) from matched results.
 *
 * This class calls another control class, RecordSQLiteOpenHelper,
 * to interact with the history table and car park data bases.
 *
 * For the search bar, the class interacts with the EditText attribute,
 * a Boundary Object that is responsible for taking in external inputs.
 * The inputs are passed in as a string query for the helper to retrieve matching results.
 * The matched result(s) are then displayed in a list.
 *
 * If a user selects a car park from the matched result(s),
 * the DisplayInfo class is called to display the relevant information on the selected car park.
 * The helper is then invoked to store the selection(s) into the history table data base.
 * The history table data base only contains distinct car park entries.
 *
 * If a user selects the clear history button,
 * the helper is invoked to remove all or the selection(s) from the history table data base.
 */
public class Search extends AppCompatActivity {

    private EditText et_search;
    private Button tx_clear;
    private TextView tv_tip;
    private MyListView listView;
    private TextView tv_clear, tv_notFound;
    private RecordSQLiteOpenHelper helper = new RecordSQLiteOpenHelper(this);;
    private BaseAdapter adapter;
    private Button to_favourite;
    public String[] fromColNames = new String[]{CAR_PARK_NO, ADDRESS};
    public int[] toViewIDs = new int[]{R.id.no, R.id.address};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        queryHistory();
        setTitle("Search");
        // clear history
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.deleteHistoryTable();
                queryHistory();
                Toast.makeText(Search.this, "Successfully cleared history", Toast.LENGTH_SHORT).show();
            }
        });

        // clear input text
        tx_clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TextView notFound = (TextView) findViewById(R.id.notFoundTV);
                notFound.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                et_search.getText().clear();
            }
        });

        // clicking search key to recycle keyboard
        et_search.setOnKeyListener(new View.OnKeyListener() {// clicking search on keyboard

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {// enter
                    // hide keyboard
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                            getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    // save history history

                    addToHistory(et_search.getText().toString().trim());

                    Toast.makeText(Search.this, "clicked!", Toast.LENGTH_SHORT).show();

                }
                return false;
            }
        });

        /**
         * addTextChangedListener is implemented to keep track of the text in the search bar
         * Using a timer, we wait for 1s for user to finish typing to start querying for data related to the user's search query
         * If there is no text in the search bar, the list will show the user's search history instead
         */
        // real-time get input text
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            private Timer timer = new Timer();
            private final long delay = 1000;

            @Override
            public void afterTextChanged(Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                    new TimerTask(){
                        @Override
                        public void run(){
                            TextView notFound = (TextView) findViewById(R.id.notFoundTV);
                            if (s.toString().trim().length() == 0) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        notFound.setVisibility(View.GONE);
                                        listView.setVisibility(View.VISIBLE);
                                        tv_tip.setText("search history");
                                        tx_clear.setVisibility(View.INVISIBLE);
                                        queryHistory();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        tv_tip.setText("search result");
                                        String tempName = et_search.getText().toString();
                                        tx_clear.setVisibility(View.VISIBLE);
                                        queryData(tempName);
                                    }
                                });

                            }
                        }
                    },
                    delay
                );

            }
        });

        /**
         * Upon clicking on one of the search item suggestions, data will be parsed into the DisplayInfoActivity to show the correct carpark info and
         * the carpark will be added into the history database for future reference
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView notFound = (TextView) findViewById(R.id.notFoundTV);
                TextView textView = (TextView) view.findViewById(R.id.no);
                String no = textView.getText().toString();
                String address = ((TextView) view.findViewById(R.id.address)).getText().toString();
                //et_search.setText(address);
                Intent myIntent = new Intent(Search.this, DisplayInfoActivity.class);
                myIntent.putExtra("key", no); //Optional parameters
                myIntent.putExtra("from", "search");
                Search.this.startActivity(myIntent);
                //whetherFavourite(address);
                addToHistory(textView.getText().toString().trim());
                notFound.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                //addLabel(address);
                //helper.addOneFavourite(address, "label");
                //Toast.makeText(Search.this, no, Toast.LENGTH_SHORT).show();
            }
        });

        if (isCarparkTableEmpty()){
            BufferedReader buffer = getCSV();
            try {
                helper.insertCarparkDataset(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        to_favourite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Search.this, Favourites.class);
//                startActivity(intent);
//            }
//        });


        /**
         * BottomNavigationView is used to switch between the three main pages in the application
         * When the activity starts, it sets the current page as selected and waits for next user input
         * On selection of a different activity, the current activity stops and switches over
         */
        //Initialise and Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Search Selected
        bottomNavigationView.setSelectedItemId(R.id.search);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.favourites:
                        startActivity(new Intent(getApplicationContext(), Favourites.class));
                        finish();
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.search:
                        return true;
                    case R.id.info:
                        startActivity(new Intent(getApplicationContext(), Info.class));
                        finish();
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    public BufferedReader getCSV(){
        return new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.carpark)));
    }

    public BufferedReader getShoppingCSV(){
        return new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.datamall)));
    }

    // Remove one history (name corresponding to that in single_history)
    public void Remove(View view){
        LinearLayout parentRow = (LinearLayout) view.getParent().getParent();
        TextView textView = (TextView) parentRow.findViewById(R.id.no);
        String no = textView.getText().toString();
        helper.removeFromHistory(no);
        queryHistory();
    }

    // query for whether add into favourite
    public void whetherFavourite(String address){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add it to favourite?");

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
        builder.setTitle("Set label for it");
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
    /**
     * Display list of selected car parks in the search history
     */
    private void queryHistory(){
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT ID as _id, CAR_PARK_NO, ADDRESS FROM HISTORY_TABLE ORDER BY _id DESC", null);
        adapter = new SimpleCursorAdapter(this, R.layout.single_history, cursor, fromColNames,
                toViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Search for the car park(s) with the given input search query and display matching results
     */
    private void queryData(String tempName) {
        TextView notFound = (TextView) findViewById(R.id.notFoundTV);
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT ID as _id, CAR_PARK_NO, ADDRESS FROM CARPARK_TABLE WHERE ADDRESS LIKE '%"+ tempName + "%' LIMIT 30 ", null);
        // simpleAdpapter for carparks
        if(cursor.getCount() <= 0){
            listView.setVisibility(View.GONE);
            notFound.setVisibility(View.VISIBLE);
            notFound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0, 0?q="+tempName)); //For modes, d = driver, b = bicycle, w = walking
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
            });
        }
        else{
            notFound.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter = new SimpleCursorAdapter(this, R.layout.single_carpark, cursor, fromColNames,
                    toViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            // setAdapter
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * Adds a car park to the history data base
     */
    private void addToHistory(String no){
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "select * from CARPARK_TABLE where CAR_PARK_NO=?", new String[]{no});

        if (cursor.moveToFirst()) {
            Carpark_Model carpark = new Carpark_Model();
            carpark.setCar_park_no(cursor.getString(cursor.getColumnIndex(CAR_PARK_NO)));
            carpark.setAddress(cursor.getString(cursor.getColumnIndex(ADDRESS)));
            carpark.setX_coord(cursor.getString(cursor.getColumnIndex(X_COORD)));
            carpark.setY_coord(cursor.getString(cursor.getColumnIndex(Y_COORD)));
            carpark.setCar_park_type(cursor.getString(cursor.getColumnIndex(CAR_PARK_TYPE)));
            carpark.setType_of_parking_system(cursor.getString(cursor.getColumnIndex(TYPE_OF_PARKING_SYSTEM)));
            carpark.setShort_term_parking(cursor.getString(cursor.getColumnIndex(SHORT_TERM_PARKING)));
            carpark.setFree_parking(cursor.getString(cursor.getColumnIndex(FREE_PARKING)));
            carpark.setNight_parking(cursor.getString(cursor.getColumnIndex(NIGHT_PARKING)));
            carpark.setCar_park_decks(cursor.getString(cursor.getColumnIndex(CAR_PARK_DECKS)));
            carpark.setGantry_height(cursor.getString(cursor.getColumnIndex(GANTRY_HEIGHT)));
            carpark.setCar_park_basement(cursor.getString(cursor.getColumnIndex(CAR_PARK_BASEMENT)));

            helper.addOneHistory(carpark);
        }

    }


    /**
     * Checks whether the record exist in History Table
     */
    private boolean hasData(String no) {
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "select CAR_PARK_NO from HISTORY_TABLE where CAR_PARK_NO=?", new String[]{no});
        //whether next or not
        return cursor.moveToNext();
    }

    public boolean isHistoryTableEmpty(){
        Cursor cursor =helper.getReadableDatabase().rawQuery("SELECT * FROM HISTORY_TABLE LIMIT 1", null);
        if (cursor.getCount() == 0){
            return true;
        }
        return false;
    }

    public boolean isCarparkTableEmpty(){
        Cursor cursor =helper.getReadableDatabase().rawQuery("SELECT * FROM CARPARK_TABLE LIMIT 1", null);
        if (cursor.getCount() == 0){
            return true;
        }
        return false;
    }


    private void initView() {
        et_search = (EditText) findViewById(R.id.et_search);
        tv_tip = (TextView) findViewById(R.id.tv_tip);
        listView = (com.example.parkinggowhere.MyListView) findViewById(R.id.listView);
        tv_clear = (TextView) findViewById(R.id.tv_clear);
        tx_clear = (Button) findViewById(R.id.text_clear);

        to_favourite = (Button) findViewById(R.id.to_favourite);

    }

}