package com.gyorog.polycal;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Random;

public class CalendarWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "CalendarWidgetProvider";
    private RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Log.w(TAG, "onUpdate called with array length:" + N);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int widgetId = appWidgetIds[i];
            // String number = String.format("%03d", (new Random().nextInt(900) + 100));
            // Log.w(TAG, "number: " + number);

           remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);

            if (CheckCalendarPermission(context)) {
                if (!CheckScreenshotMode(context)) {
                    Intent intent = new Intent(context, CalendarRemoteViewsService.class);
                    remoteViews.setRemoteAdapter(R.id.listview, intent);
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                } else {
                    Intent intent = new Intent(context, ScreenshotRemoteViewsService.class);
                    remoteViews.setRemoteAdapter(R.id.listview, intent);
                    appWidgetManager.updateAppWidget(widgetId, remoteViews);
                }
            } else {
                Intent intent = new Intent(context, ScreenshotRemoteViewsService.class);
                remoteViews.setRemoteAdapter(R.id.listview, intent);
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }

            // Log.w(TAG, "finishing widget update for widget ID " + widgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent){
        if( intent.getAction() == "com.gyorog.polycal.RELOAD_EVENTS"){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] allWidgetIDs = appWidgetManager.getAppWidgetIds( new ComponentName(context, this.getClass()) );
            appWidgetManager.notifyAppWidgetViewDataChanged(allWidgetIDs, R.id.listview);
        }
        if( intent.getAction() == "com.gyorog.polycal.CHANGE_SOURCE"){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] allWidgetIDs = appWidgetManager.getAppWidgetIds( new ComponentName(context, this.getClass()) );
            onUpdate(context, appWidgetManager, allWidgetIDs);
        }
        // Log.e(TAG, "Received: " + intent.toString());

        super.onReceive(context, intent);
    }

    private boolean CheckCalendarPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean CheckScreenshotMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("screenshot_mode", false);
    }

}