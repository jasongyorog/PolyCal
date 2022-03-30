package com.gyorog.polycal;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "com.gyorog.polycal.SettingsActivity";
    private List<String> CalendarIDs = new LinkedList<>();
    private List<String> CalendarDisplayNames = new LinkedList<>();
    static public int widget_id;
    Boolean requireReturnCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if ( extras != null ){
            widget_id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Log.d(TAG, "wID " + widget_id + " (from " + AppWidgetManager.EXTRA_APPWIDGET_ID + ")");

            String intent_from = extras.getString("from");
            if (intent_from == null) {
                Log.d(TAG, "No 'from' extra. Assuming needs return code.");
                requireReturnCode = true;
            } else if ( intent_from.equals("WidgetProvider") ){
                Log.d(TAG, "Detected 'from' extra as 'WidgetProvider'");
                requireReturnCode = false;
            } else {
                Log.d(TAG, "Detected 'from' extra as '" + intent_from + "'");
                requireReturnCode = false;
            }
        } else {
            finish();
            Log.e(TAG, "COULD NOT DETERMINE WIDGET ID for SettingsActivity");
        }

        // Set up preferences screen


        if (! requireReturnCode) {
            setContentView(R.layout.settings_activity);
        } else {
            setContentView(R.layout.settings_activity_button);

            Button accept_button = findViewById(R.id.accept_button);
            accept_button.setText("Add Widget to Home Screen");
            accept_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });

        }

    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final int READ_CALENDAR_CONSTANT=1;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_screen, rootKey);

            Log.d(TAG, "wID " + widget_id + " onCreatePreferences()");

            findPreference("calendar_permission").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference){
                    requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, READ_CALENDAR_CONSTANT);
                    return true;
                }
            });

            findPreference("screenshot_mode").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newSetting) {
                    if ( (boolean) newSetting ){
                        findPreference("calendar_select").setEnabled(false);
                    } else {
                        findPreference("calendar_select").setEnabled(true);
                    }

                    Log.d(TAG, "wID " + widget_id + " onPreferenceChange(screenshotmode=" + newSetting + ")");

                    String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
                    SharedPreferences SharePref = preference.getContext().getSharedPreferences(pref_file_name , 0);
                    SharedPreferences.Editor editor = SharePref.edit();
                    editor.putBoolean("screenshot_mode", (boolean) newSetting);
                    editor.apply();

                    Intent redraw = new Intent("com.gyorog.polycal.CHANGE_SOURCE");
                    redraw.setPackage(getContext().getPackageName());
                    getContext().sendBroadcast(redraw);

                    return true;
                }
            });

            //String calendar_select_pref = String.format("%d calendar_select", widget_id);
            findPreference("calendar_select").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    Log.d(TAG, "wID " + widget_id + " onPreferenceChange(calendar_selection=" + newVal.toString() + ")");

                    String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
                    SharedPreferences SharePref = preference.getContext().getSharedPreferences(pref_file_name , 0);
                    SharedPreferences.Editor editor = SharePref.edit();
                    editor.putStringSet("calendar_selection", (Set) newVal);
                    editor.apply();

                    Intent redraw = new Intent("com.gyorog.polycal.RELOAD_EVENTS");
                    redraw.setPackage(getContext().getPackageName());
                    getContext().sendBroadcast(redraw);

                    return true;
                }
            });

            findPreference("date_format").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    Log.d(TAG, "wID " + widget_id + " onPreferenceChange(date_format=" + newVal.toString() + ")");

                    String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
                    SharedPreferences SharePref = preference.getContext().getSharedPreferences(pref_file_name , 0);
                    SharedPreferences.Editor editor = SharePref.edit();
                    editor.putString("date_format", (String) newVal);
                    editor.apply();

                    Intent redraw = new Intent("com.gyorog.polycal.RELOAD_EVENTS");
                    redraw.setPackage(getContext().getPackageName());
                    getContext().sendBroadcast(redraw);

                    return true;
                }
            });

            findPreference("date_format_allday").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    Log.d(TAG, "wID " + widget_id + " onPreferenceChange(date_format_allday=" + newVal.toString() + ")" );

                    String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
                    SharedPreferences SharePref = preference.getContext().getSharedPreferences(pref_file_name , 0);
                    SharedPreferences.Editor editor = SharePref.edit();
                    editor.putString("date_format_allday", (String) newVal);
                    editor.apply();

                    Intent redraw = new Intent("com.gyorog.polycal.RELOAD_EVENTS");
                    redraw.setPackage(getContext().getPackageName());
                    getContext().sendBroadcast(redraw);

                    return true;
                }
            });

            UpdatePreferences();
        }

        public void UpdatePreferences() {
            boolean gotCalendarPermission = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;

            String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
            SharedPreferences SharePref = getContext().getSharedPreferences(pref_file_name , 0);

            if (gotCalendarPermission) {
                Preference CalendarPermissionPref = findPreference("calendar_permission");
                CalendarPermissionPref.setEnabled(false);
                CalendarPermissionPref.setSummary("Calendar permission has been granted.");

                ((SettingsActivity) getActivity()).FillCalendarLists();
                int CalendarCount = ((SettingsActivity) getActivity()).CalendarDisplayNames.size();
                CharSequence[] entries_array = ((SettingsActivity) getActivity()).CalendarDisplayNames.toArray(new CharSequence[CalendarCount]);
                CharSequence[] values_array = ((SettingsActivity) getActivity()).CalendarIDs.toArray(new CharSequence[CalendarCount]);

                Boolean screenshot_mode = SharePref.getBoolean("screenshot_mode", true);
                SwitchPreference ScreenshotModePref = findPreference("screenshot_mode");
                ScreenshotModePref.setEnabled(true);
                ScreenshotModePref.setChecked(screenshot_mode);

                Set<String> current_calendar_selection = SharePref.getStringSet("calendar_selection", null );
                if(current_calendar_selection == null){
                    current_calendar_selection = new HashSet<>();
                    for(int i=0; i<values_array.length; i++){
                        current_calendar_selection.add((String)values_array[i]);
                    }
                    SharedPreferences.Editor editor = SharePref.edit();
                    editor.putStringSet("calendar_selection", current_calendar_selection);
                    editor.apply();
                }

                Log.d(TAG, "calendar_selection = " + current_calendar_selection);
                MultiSelectListPreference CalSelectPref = findPreference("calendar_select");

                CalSelectPref.setEntries(entries_array);
                CalSelectPref.setEntryValues(values_array);
                CalSelectPref.setValues(current_calendar_selection);

                CalSelectPref.setEnabled(true);
            }

            String date_format = SharePref.getString("date_format", new String());
            ListPreference DateFormatPref = findPreference("date_format");
            DateFormatPref.setEntries(PolyCalDateFormats.getFormatsReadable() );
            DateFormatPref.setEntryValues(PolyCalDateFormats.getFormatsParseable() );
            DateFormatPref.setValue(date_format);

            String date_format_allday = SharePref.getString("date_format_allday", new String());
            ListPreference DateFormatAlldayPref = findPreference("date_format_allday");
            DateFormatAlldayPref.setEntries(PolyCalDateFormats.getFormatsReadableAllday() );
            DateFormatAlldayPref.setEntryValues(PolyCalDateFormats.getFormatsParseableAllday() );
            DateFormatAlldayPref.setValue(date_format_allday);

            Log.d(TAG, "End of UpdatePreferences()");
        }

        @Override
        public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == READ_CALENDAR_CONSTANT) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.READ_CALENDAR)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.i(TAG, "READ_CALENDAR Permission Granted");

                            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            String pref_file_name = String.format("com.gyorog.PolyCal.prefs_for_widget_%d", widget_id);
                            SharedPreferences preferences = getContext().getSharedPreferences(pref_file_name, MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("screenshot_mode", false);
                            editor.apply();

                            Intent redraw = new Intent("com.gyorog.polycal.CHANGE_SOURCE");
                            redraw.setPackage(getContext().getPackageName());
                            getContext().sendBroadcast(redraw);

                            UpdatePreferences();
                        }
                    }
                }
            }
        }

    }

    // This is for getting the list of available calendars:
    public static final String[] CALENDAR_COLUMN_LIST = new String[] {
            Calendars._ID,                          // 0
            Calendars.CALENDAR_DISPLAY_NAME          // 1
    };
    private static final int COLUMN_INDEX_ID = 0;
    private static final int COLUMN_INDEX_DISPLAY_NAME = 1;

    private void FillCalendarLists() {
        ContentResolver cr = getContentResolver();
        Uri uri = Calendars.CONTENT_URI;

        Cursor cur = cr.query(uri, CALENDAR_COLUMN_LIST, null, null, null);
        this.CalendarIDs.clear();
        this.CalendarDisplayNames.clear();

        while (cur.moveToNext()) {
            // Log.e(TAG, "Calendar ID " + cur.getString(COLUMN_INDEX_ID) + " is " + cur.getString(COLUMN_INDEX_DISPLAY_NAME) );
            this.CalendarIDs.add(cur.getString(COLUMN_INDEX_ID));
            this.CalendarDisplayNames.add(cur.getString(COLUMN_INDEX_DISPLAY_NAME));
        }
        cur.close();
    }

}