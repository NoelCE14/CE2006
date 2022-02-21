package com.example.parkinggowhere.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.example.parkinggowhere.Connection;
import com.example.parkinggowhere.MyListView;
import com.example.parkinggowhere.R;
import com.example.parkinggowhere.RecordSQLiteOpenHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.parkinggowhere.utils.Const.*;

/**
 * This class implements the Favourites Control Object
 * The Favourites Class is responsible for coordinating functions used in the Favourites Page
 *
 * The Favourites page displays a list of favourite car parks with their respective available lots.
 * The page also has a functionality to refresh and update the lot availability of all or selected car park(s).
 * If a car park in the list is long pressed, the user is prompted to delete the car park from favourites table.
 *
 * This class calls another control class, RecordSQLiteOpenHelper,
 * to interact with the favourites table and car park data bases.
 *
 * If a user selects a car park from the matched result(s),
 * the DisplayInfo class is called to display the relevant information on the selected car park.
 * The favourites table data base only contains distinct car park entries with unique labels.
 *
 *  If a user long presses an entry and chooses to remove the car park from favourites,
 *  the helper is invoked to remove the selection from the favourites table data base.
 */
public class Favourites extends AppCompatActivity {

    private RecordSQLiteOpenHelper helper = new RecordSQLiteOpenHelper(this);;
    private BaseAdapter adapter;
    private MyListView listView;
    private Menu mMenu;
    private long mLastClickTime = 0;
    final Handler handler = new Handler(Looper.getMainLooper());
    boolean networkConnected;

    public String[] fromColNames = new String[]{ADDRESS, LABEL, LOTS_AVAIL, CAR_PARK_NO, TOTAL_LOTS};
    public int[] toViewIDs = new int[]{R.id.address, R.id.label, R.id.img_avail, R.id.no, R.id.total_lots};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_favourites);

        initView();

        queryFavourite();
        setTitle("Favourites");
        TextView tv = (TextView)findViewById(R.id.networkText);
        networkConnected = Connection.isConnected(getApplicationContext());
//        if (networkConnected)
//            tv.setText("Device is online");
//        else
//            tv.setText("Network unavailable");

        /**
         * BottomNavigationView is used to switch between the three main pages in the application
         * When the activity starts, it sets the current page as selected and waits for next user input
         * On selection of a different activity, the current activity stops and switches over
         */
        //Initialise and Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Info Selected
        bottomNavigationView.setSelectedItemId(R.id.favourites);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.favourites:
                        return true;
                    case R.id.search:
                        startActivity(new Intent(getApplicationContext(), Search.class));
                        finish();
                        overridePendingTransition(0,0);
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.no);
                String no = textView.getText().toString();
                switch (view.getId()){
                    default:
                        Intent myIntent = new Intent(Favourites.this, DisplayInfoActivity.class);
                        myIntent.putExtra("key", no); //Optional parameters
                        myIntent.putExtra("from", "info");
                        Favourites.this.startActivity(myIntent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;
                }


            }
        });

        listView.setFastScrollEnabled(true);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeFavourite(view);

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar,menu);
        mMenu = menu;
        menu.findItem(R.id.info_favourite).setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
            menu.findItem(R.id.favourites_refresh).setIcon(R.drawable.refresh);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favourites_refresh:
                if(SystemClock.elapsedRealtime() - mLastClickTime < 3000){
                    return true;
                }
                networkConnected = Connection.isConnected(getApplicationContext());
                if(networkConnected){
                    mLastClickTime = SystemClock.elapsedRealtime();
                    setLoading();
                    new refreshAvailTask().execute();
                }
                else{
                    Toast.makeText(Favourites.this, "Network unavailable, please try again later", Toast.LENGTH_SHORT).show();
                }

                return true;
            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refreshes the Favourite page after returning here.
     * Ensures that an un-favourite car park entry is removed
     * after returning from DisplayInfoActivity
     */
    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    /**
     * Displays list of favourite car parks on the favourite page
     */
    private void queryFavourite(){
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT ID as _id, ADDRESS, LABEL, LOTS_AVAIL, CAR_PARK_NO, TOTAL_LOTS FROM FAVOURITE_TABLE", null);
        adapter = new SimpleCursorAdapter(this, R.layout.single_favourite, cursor, fromColNames,
                toViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view =super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(R.id.img_avail);
                String lots_avail = ((TextView) view.findViewById(R.id.img_avail)).getText().toString();
                int c = changeColor(lots_avail);
                switch (c){
                    case 0:
                        textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
                        break;
                    case 1:
                        textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
                        break;
                    case 2:
                        textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.yellow, null));
                        break;
                    case 3:
                        textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.green, null));
                        break;
                }

                TextView t = (TextView) view.findViewById(R.id.no);
                String no = t.getText().toString();
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        networkConnected = Connection.isConnected(getApplicationContext());
                        TextView item = (TextView) view.findViewById(R.id.img_avail);
                        if(!item.getText().equals("NA")){
                            if(networkConnected){
                                item.setText("...");
                                new refreshOneAvailTask(no).execute();
                            }
                            else{
                                textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
                                item.setText("NA");
                                Toast.makeText(Favourites.this, "Network unavailable, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                });
                return view;
            }
        };
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setLoading(){
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT ID as _id, ADDRESS, LABEL, LOTS_AVAIL, CAR_PARK_NO, TOTAL_LOTS FROM FAVOURITE_TABLE", null);
        adapter = new SimpleCursorAdapter(this, R.layout.single_favourite, cursor, fromColNames,
                toViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                View view =super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(R.id.img_avail);
                textView.setText("...");
                return view;
            }
        };
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private int changeColor(String lots_avail){
        if (lots_avail.equals("NA") || lots_avail.equals("")){
            return 0;
        }
        double lotsD = (double) Double.parseDouble(lots_avail);
        int lots = (int)lotsD;
        if (lots <= 20){
            return 1;
        }
        else if(lots > 20 && lots <=70){
            return 2;
        }
        else{
            return 3;
        }


    }

    private void initView() {
        listView = (com.example.parkinggowhere.MyListView) findViewById(R.id.listView);
    }

    /**
     * Asynchronous task to refresh ALL favourite carpark lot availability
     */

    private class refreshAvailTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            helper.updateAvailDatasetCarparksFav();
            helper.updateAvailDatasetShoppingMallFav();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    helper.updateFavouriteAvail();
                    queryFavourite();
                }
            }, 3000);

        }
    }

    /**
     * Asynchronous task to refresh ONE favourite carpark lot availability
     */

    private class refreshOneAvailTask extends AsyncTask<String, Void, Void>
    {
        String no = "";
        public refreshOneAvailTask(String no){
            super();
            this.no = no;
        }

        @Override
        protected Void doInBackground(String... params) {
            helper.updateAvailDatasetCarparksFav();
            helper.updateAvailDatasetShoppingMallFav();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    helper.updateOneFavouriteAvail(no);
                    queryFavourite();
                }
            }, 3000);
        }
    }

    public void removeFavourite(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove from favourites?");

        // Set up the buttons
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView textView = view.findViewById(R.id.address);
                String address = textView.getText().toString();
                helper.removeFromFavourite(address);
                queryFavourite();
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
