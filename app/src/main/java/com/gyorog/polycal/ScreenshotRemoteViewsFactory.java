package com.gyorog.polycal;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.util.Log;
import android.util.TypedValue;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenshotRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "ScreenshotRemoteViewsFactory";
    private Context mContext;
    private int widget_id;
    private int text_size;
    private String date_format;
    private String date_format_allday;

    private static class EventEntry{
        public String color;
        public Boolean allday;
        public Date begin;
        public String title;
        public String location;
        EventEntry(String set_color, Boolean set_allday, Date set_begin, String set_title, String set_location){
            color = set_color;
            allday = set_allday;
            begin = set_begin;
            title = set_title;
            location = set_location;
        }
    }

    private static SimpleDateFormat formatter = new SimpleDateFormat("MMM dd hh:mm a", Locale.US);
    private static EventEntry fake_events[] = new EventEntry[0];
    static {
        try {
            fake_events = new EventEntry[]{
                            new EventEntry("#ff8888", false, formatter.parse("Jun 1 10:00 pm"), "Date with Sam", ""),
                            new EventEntry("#00aaaa", false, formatter.parse("Jun 3 8:00 am"), "Receive award for PolyCal", ""),
                            new EventEntry("#00aaaa", false, formatter.parse("Jun 6 2:30 pm"), "Honorary Doctorate review", ""),
                            new EventEntry("#00aaaa", false, formatter.parse("Jun 8 10:00 am"), "Nobel Awards ceremony", ""),
                            new EventEntry("#ffff00", true, formatter.parse("Jun 12 12:00 am"), "Loving Day", ""),
                            new EventEntry("#ff8888", false, formatter.parse("Jun 15 8:00 pm"), "Date with Pat", ""),
                            new EventEntry("#ffff00", true, formatter.parse("Jun 19 12:00 am"), "Juneteenth", ""),
                            new EventEntry("#00aaaa", false, formatter.parse("Jun 25 5:00 pm"), "Receive Medal of Honor", ""),
                            new EventEntry("#ffff00", true, formatter.parse("Jun 28 12:00 am"), "Stonewall Riots Anniversary", ""),
                            new EventEntry("#00aaaa", false, formatter.parse("Jun 29 8:00 am"), "Booker Prize review", "")
                    };
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public ScreenshotRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        //LogIntent("ScreenshotRemoteViewsFactoy()", intent);
        widget_id = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
        Log.d(TAG, "wID " + widget_id + " new ScreenshotRemoteViewsFactory()");
    }

    @Override
    public void onCreate() { }

    @Override
    public void onDataSetChanged() {
        final long identityToken = Binder.clearCallingIdentity();

        // SharedPreferences SharePref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
        SharedPreferences SharePref = mContext.getSharedPreferences(pref_file_name , 0);

        text_size = SharePref.getInt("text_size", 12);
        date_format = SharePref.getString("date_format", (String) PolyCalDateFormats.getFormatsParseable()[0]);
        date_format_allday = SharePref.getString("date_format_allday", (String) PolyCalDateFormats.getFormatsParseableAllday()[0]);
        Log.d(TAG, "wID " + widget_id + " got date_format='" + date_format + "' and date_format_allday='" + date_format_allday + "'");

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return fake_events.length;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION){
            return null;
        }

        int other_color = Color.LTGRAY;
        EventEntry my_entry = fake_events[position];

        SimpleDateFormat formatter;
        if ( my_entry.allday ) {
            formatter = new SimpleDateFormat(date_format_allday, Locale.US);
        } else {
            formatter = new SimpleDateFormat(date_format, Locale.US);
        }
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        symbols.setAmPmStrings(new String[] { "am", "pm" });
        formatter.setDateFormatSymbols(symbols);
        
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.appwidget_item);

        Intent settings_intent = new Intent(mContext, SettingsActivity.class);
        settings_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
        settings_intent.putExtra("from", "Screenshot.UserClick");
        rv.setOnClickFillInIntent(R.id.item_layout, settings_intent);

        rv.setTextViewText(R.id.event_time, formatter.format(my_entry.begin));
        rv.setTextColor(R.id.event_time, other_color);
        rv.setTextViewTextSize(R.id.event_time, TypedValue.COMPLEX_UNIT_SP, text_size);

        rv.setTextViewText(R.id.event_title, my_entry.title);
        rv.setTextColor(R.id.event_title, Color.parseColor(my_entry.color) );
        rv.setTextViewTextSize(R.id.event_title, TypedValue.COMPLEX_UNIT_SP, text_size);

        rv.setTextViewText(R.id.event_location, my_entry.location);
        rv.setTextColor(R.id.event_location, other_color);
        rv.setTextViewTextSize(R.id.event_location, TypedValue.COMPLEX_UNIT_SP, text_size);

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
/*
    public void LogIntent(String extra_tag, Intent intent){
        Log.d(TAG, extra_tag + " -> " + intent.toString() );
        for (String key : intent.getExtras().keySet())
            Log.d(TAG, "(extra) " + key + " = " + intent.getExtras().get(key));
    }
*/
}