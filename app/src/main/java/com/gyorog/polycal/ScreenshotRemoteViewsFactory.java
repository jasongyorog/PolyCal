package com.gyorog.polycal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ScreenshotRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "ScreenshotRemoteViewsFactory";
    private Context mContext;
    private static final String events[][] = new String[][] {
            {"#ff8888", " Jun 1 | 10:00 pm ", "Date with Sam", ""},
            {"#00aaaa", " Jun 3 | 8:00 am ", "Receive award for PolyCal", ""},
            {"#00aaaa", " Jun 6 | 2:30 pm ", "Honorary Doctorate review", ""},
            {"#00aaaa", " Jun 8 | 10:00 am ", "Nobel Awards ceremony", ""},
            {"#ffff00", " Jun 12 ", "Loving Day", ""},
            {"#ff8888", " Jun 15 | 8:00 pm ", "Date with Pat", ""},
            {"#ffff00", " Jun 19 ", "Juneteenth", ""},
            {"#00aaaa", " Jun 25 | 5:00 pm ", "Receive Medal of Honor", ""},
            {"#ffff00", " Jun 28", "Stonewall Riots Anniversary", ""},
            {"#00aaaa", " Jun 29 | 8:00 am ", "Booker Prize review", ""}
    };

    public ScreenshotRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {
        // Log.e(TAG, "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return events.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        int other_color = Color.LTGRAY;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.appwidget_item);
        rv.setTextViewText(R.id.event_time, events[position][1]);
        rv.setTextColor(R.id.event_time, other_color);

        rv.setTextViewText(R.id.event_title, events[position][2]);
        rv.setTextColor(R.id.event_title, Color.parseColor(events[position][0]) );

        rv.setTextViewText(R.id.event_location, events[position][3]);
        rv.setTextColor(R.id.event_location, other_color);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}