package com.hanwen.chinesechat.receiver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.hanwen.chinesechat.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + this);
        Log.i(TAG, "onReceive: " + intent);
        ConnectivityManager service = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.i(TAG, "onReceive: " + service.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState());
        boolean isConnected = service.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
        Log.i(TAG, "onReceive: " + isConnected);

        boolean isMobileConnected = service.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;

        if (!isConnected) {
            if (isMobileConnected) {
                Toast.makeText(context, "你已离开WiFi环境，请检查是否已出队，否则，将消耗你的数据流量", Toast.LENGTH_SHORT).show();
/*            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("你已离开WiFi环境，请检查是否已出队，否则，将消耗你的数据流量");
            builder.show();*/
            }
        }


    }
}
