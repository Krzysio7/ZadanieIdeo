package com.example.opendataresource.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class InternetStatusListener extends BroadcastReceiver {
    private static final String TAG = "INTERNET_STATUS";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "network status changed");
        if (InternetStatusListener.isOnline(context)) {//check if the device has an Internet connection
            //Start a service that will make your TCP Connection.

            OnlineOrOffline onlineOrOffline = (OnlineOrOffline) context;
            onlineOrOffline.onOnline();
        }
        else{
            OnlineOrOffline onlineOrOffline = (OnlineOrOffline) context;
            onlineOrOffline.onOffline();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public interface OnlineOrOffline {
        void onOnline();
        void onOffline();
    }


}