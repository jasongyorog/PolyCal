package com.gyorog.polycal;

import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class CalendarRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String EVENT_ID = "com.gyorog.polycal.EVENT_ID";
    public static final String EVENT_BEGIN = "com.gyorog.polycal.EVENT_BEGIN";
    private static final String TAG = "CalendarRemoteViewsFactory";
    private Context mContext;
    private Cursor mCursor;
    private int widget_id;
    private String date_format;
    private String date_format_allday;

    public CalendarRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        //widget_id = Integer.parseInt(intent.getData().getSchemeSpecificPart());
        widget_id = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        Log.d(TAG, "wID " + widget_id + " created.");
    }

    @Override
    public void onCreate() { }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();
        Log.d(TAG, "wID " + widget_id + " got identityToken " + identityToken);

        // SharedPreferences SharePref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
        SharedPreferences SharePref = mContext.getSharedPreferences(pref_file_name , 0);

        date_format = SharePref.getString("date_format", (String) PolyCalDateFormats.getFormatsParseable()[0]);
        date_format_allday = SharePref.getString("date_format_allday", (String) PolyCalDateFormats.getFormatsParseableAllday()[0]);
        Log.d(TAG, "wID " + widget_id + " got date_format='" + date_format + "' and date_format_allday='" + date_format_allday + "'");

        Set<String> EnabledCalendarIDs = SharePref.getStringSet("calendar_selection", new HashSet<String>() );
        Log.d(TAG, "wID " + widget_id + " got calendar_selection='" + EnabledCalendarIDs.toString() + "'" );

        GetCalendarEvents(EnabledCalendarIDs);
        Log.d(TAG, "wID " + widget_id + " found " + getCount() + " calendar events.");

        Binder.restoreCallingIdentity(identityToken);
    }


    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG, "RemoteViews getViewAt(" + position + ")");
        if (position == AdapterView.INVALID_POSITION || mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }

        SimpleDateFormat formatter;
        if ( 1 == mCursor.getInt(EVENT_INDEX_ALLDAY) ) {
            formatter = new SimpleDateFormat(date_format_allday, Locale.US);
        } else {
            formatter = new SimpleDateFormat(date_format, Locale.US);
        }
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        symbols.setAmPmStrings(new String[] { "am", "pm" });
        formatter.setDateFormatSymbols(symbols);
        Date StartDate = new Date( mCursor.getLong(EVENT_INDEX_BEGIN) );
        formatter.setTimeZone(TimeZone.getTimeZone( mCursor.getString(EVENT_INDEX_EVENT_TIMEZONE) ));

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.appwidget_item);

        Bundle extras = new Bundle();
        //extras.putLong(EVENT_ID, mCursor.getLong(EVENT_INDEX_EVENTID));
        extras.putLong(EVENT_BEGIN, mCursor.getLong(EVENT_INDEX_BEGIN));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.item_layout, fillInIntent);

        int other_color = Color.LTGRAY;

        rv.setTextViewText(R.id.event_time, formatter.format(StartDate) );
        rv.setTextColor(R.id.event_time, other_color);

        rv.setTextViewText(R.id.event_title, mCursor.getString(EVENT_INDEX_TITLE).replaceAll("[\\t\\n\\r]+"," ") );
        rv.setTextColor(R.id.event_title, mCursor.getInt(EVENT_INDEX_DISPLAY_COLOR));

        rv.setTextViewText(R.id.event_location, mCursor.getString(EVENT_INDEX_LOCATION).replaceAll("[\\t\\n\\r]+"," ") );
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
        return mCursor.moveToPosition(position) ? mCursor.getLong(0) : position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    public static final String[] EVENT_COLUMN_LIST = new String[] {
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_TIMEZONE,
            CalendarContract.Events.DISPLAY_COLOR,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Instances.END
    };
    private static final int EVENT_INDEX_EVENTID = 0;
    private static final int EVENT_INDEX_BEGIN = 1;
    private static final int EVENT_INDEX_ALLDAY = 2;
    private static final int EVENT_INDEX_EVENT_TIMEZONE = 3;
    private static final int EVENT_INDEX_DISPLAY_COLOR = 4;
    private static final int EVENT_INDEX_TITLE = 5;
    private static final int EVENT_INDEX_LOCATION = 6;
    private static final int EVENT_INDEX_END = 7;

    private void GetCalendarEvents(Set<String> EnabledCalendarIDs) {
        long now_ms = System.currentTimeMillis();

        Calendar cal_end = Calendar.getInstance();
        cal_end.add(Calendar.YEAR, 1);
        long end_ms = cal_end.getTimeInMillis();

        Uri.Builder instancesUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(instancesUriBuilder, now_ms );
        ContentUris.appendId(instancesUriBuilder, end_ms );
        Uri instancesUri = instancesUriBuilder.build();

        String[] selectionArgs = new String[0];
        String selectionString = "";
        if( EnabledCalendarIDs.isEmpty() ) {
            selectionString = "( " + CalendarContract.Instances.CALENDAR_ID + " != " + CalendarContract.Instances.CALENDAR_ID + " )";
        } else {
            // selectionArgs = EnabledCalendarIDs.toArray(new String[EnabledCalendarIDs.size()]);
            selectionArgs = EnabledCalendarIDs.toArray(new String[0]);

            String[] query_list = new String[selectionArgs.length];
            for(int i=0; i<selectionArgs.length; ++i){
                query_list[i] = "( " + CalendarContract.Instances.CALENDAR_ID + " = ? )";
            }
            selectionString = TextUtils.join(" OR ", query_list);
        }

        // Log.d(TAG, "Query: " + selectionString);
        // Log.d(TAG, "Args: " + TextUtils.join(",", selectionArgs));
        mCursor = mContext.getContentResolver().query(instancesUri, EVENT_COLUMN_LIST, selectionString,selectionArgs, CalendarContract.Instances.BEGIN + " ASC");
    }
}