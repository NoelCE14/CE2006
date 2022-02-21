package com.example.parkinggowhere;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class implements the Connection Entity
 * The class checks whether the device is connected to a network and online
 */
public class Connection {
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context){
        NetworkInfo networkInfo = Connection.getNetworkInfo(context);
        return (networkInfo != null && networkInfo.isConnected());
    }
}
