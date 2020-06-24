package com.gyorog.polycal;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.LinkedList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private final int READ_CALENDAR_CONSTANT=1;
    private List<String> CalendarIDs = new LinkedList<>();
    private List<String> CalendarDisplayNames = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up preferences screen
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    private boolean CheckCalendarPermission() {
        return ActivityCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final int READ_CALENDAR_CONSTANT=1;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preference_screen, rootKey);

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

                    Intent redraw = new Intent("com.gyorog.polycal.CHANGE_SOURCE");
                    redraw.setPackage(getContext().getPackageName());
                    getContext().sendBroadcast(redraw);

                    return true;
                }
            });

            findPreference("calendar_select").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newVal) {
                    Intent redraw = new Intent("com.gyorog.polycal.RELOAD_EVENTS");
                    redraw.setPackage(getContext().getPackageName());
                    getContext().sendBroadcast(redraw);

                    return true;
                }
            });

            UpdatePreferences();
        }

        public void UpdatePreferences() {
            // Log.w(TAG, "Updating Preference Screen");

            boolean gotCalendarPermission = ((SettingsActivity) getActivity()).CheckCalendarPermission();

            if (gotCalendarPermission) {
                Preference CalendarPermissionPref = findPreference("calendar_permission");
                CalendarPermissionPref.setEnabled(false);
                CalendarPermissionPref.setSummary("Calendar permission has been granted.");

                SwitchPreference ScreenshotModePref = findPreference("screenshot_mode");
                ScreenshotModePref.setEnabled(true);

                ((SettingsActivity) getActivity()).FillCalendarLists();
                int CalendarCount = ((SettingsActivity) getActivity()).CalendarDisplayNames.size();
                CharSequence[] entries_array = ((SettingsActivity) getActivity()).CalendarDisplayNames.toArray(new CharSequence[CalendarCount]);
                CharSequence[] values_array = ((SettingsActivity) getActivity()).CalendarIDs.toArray(new CharSequence[CalendarCount]);

                MultiSelectListPreference CalSelectPref = findPreference("calendar_select");
                CalSelectPref.setEntries(entries_array);
                CalSelectPref.setEntryValues(values_array);
                CalSelectPref.setEnabled(true);
            }

        }

        @Override
        public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == READ_CALENDAR_CONSTANT) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.READ_CALENDAR)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "Permission Granted");

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("screenshot_mode", false);
                            editor.apply();

                            UpdatePreferences();
                        }
                    }
                }
            }
        }
    }




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