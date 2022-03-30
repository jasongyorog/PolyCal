package com.gyorog.polycal;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViewsService;

public class ScreenshotRemoteViewsService extends RemoteViewsService {
    private static final String TAG = "ScreenshotRemoteViewService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        //LogIntent("onGetViewFactory()", intent);
        return new ScreenshotRemoteViewsFactory(this.getApplicationContext(), intent);
    }
/*
    public IBinder onBind(Intent intent) {
        LogIntent("onBind()", intent);
        return super.onBind(intent);
    }
*/
/*
    public void LogIntent(String extra_tag, Intent intent){
        Log.d(TAG, extra_tag + " Received " + intent.toString() );
        for (String key : intent.getExtras().keySet())
            Log.d(TAG, "(extra) " + key + " = " + intent.getExtras().get(key));
    }
*/
}