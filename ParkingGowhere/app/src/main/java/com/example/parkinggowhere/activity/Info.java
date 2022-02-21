package com.example.parkinggowhere.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alphabetik.Alphabetik;
import com.example.parkinggowhere.R;
import com.example.parkinggowhere.RecordSQLiteOpenHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.parkinggowhere.utils.Const.*;

/**
 * This class implements the Info Control Object
 * The Info Class is responsible for coordinating functions used in the Info Page
 *
 * The Info page displays the entire list of car parks from the entire car park data base.
 * The page contains an alphabetic scroll bar for faster scrolling of the entire list.
 *
 * This class calls another control class, RecordSQLiteOpenHelper,
 * to interact with the car park data bases.
 *
 * If a user selects a car park from the matched result(s),
 * the DisplayInfo class is called to display the relevant information on the selected car park.
 */
public class Info extends AppCompatActivity {

    //Implement your data as you prefer, but sort it.
    private RecordSQLiteOpenHelper helper = new RecordSQLiteOpenHelper(this);;
    private BaseAdapter adapter;
    private ListView listView;



    public String[] fromColNames = new String[]{CAR_PARK_NO, ADDRESS};
    public int[] toViewIDs = new int[]{R.id.no, R.id.address};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setTitle("Information");

        /**
         * BottomNavigationView is used to switch between the three main pages in the application
         * When the activity starts, it sets the current page as selected and waits for next user input
         * On selection of a different activity, the current activity stops and switches over
         */
        //Initialise and Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Info Selected
        bottomNavigationView.setSelectedItemId(R.id.info);

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
                        startActivity(new Intent(getApplicationContext(), Search.class));
                        finish();
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.info:
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Listview implementation, with SORTED list of DATA
        listView = findViewById(R.id.listView);
        int index = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
        queryData();

        //Implementation
        listView.setSelectionFromTop(index, top);
        Alphabetik alphabetik = (Alphabetik) findViewById(R.id.alphSectionIndex);
        alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
            @Override
            public void onItemClick(View view, int position, String character) {
                //listView.smoothScrollToPosition(getPosition(character.charAt(0)));
                listView.smoothScrollToPositionFromTop(getPosition(character.charAt(0)),0,0);
            }
        });
    }

    private int getPosition(char c){
        int position = -1;
        boolean enable = false;
        if (c == '#')
            c = 'Z';
        while (position == -1) {
            Cursor cursor = helper.getReadableDatabase().rawQuery(
                    "select ID as _id, CAR_PARK_NO from CARPARK_TABLE where CAR_PARK_NO LIKE '" + c + "%' ", null);

            if (cursor.moveToFirst()) {
                position = cursor.getInt(0)-1;
            }
            else{
                if (c == 'Z')
                    enable = true;
                if (enable)
                    c--;
                else
                    c++;
            }
            cursor.close();
        }
        return position;
    }

    /**
     * Searches for list of car parks
     */
    private void queryData() {
        Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT ID as _id, CAR_PARK_NO, ADDRESS FROM CARPARK_TABLE", null);
        // simpleAdpapter for carparks
        adapter = new SimpleCursorAdapter(this, R.layout.single_carpark, cursor, fromColNames,
                toViewIDs, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        // setAdapter
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                String no = ((TextView) view.findViewById(R.id.no)).getText().toString();
                Intent myIntent = new Intent(Info.this, DisplayInfoActivity.class);
                myIntent.putExtra("key", no); //Optional parameters
                myIntent.putExtra("from", "info");
                Info.this.startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        adapter.notifyDataSetChanged();

        //cursor.close();
    }


}
