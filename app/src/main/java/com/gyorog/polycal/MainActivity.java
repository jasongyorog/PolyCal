package com.gyorog.polycal;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    Context context;
    public final String TAG = "com.gyorog.polycal.MainActivity";
    int[] widget_ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout layout = findViewById(R.id.main_layout);
        context = getApplicationContext();
        TextView tv = new TextView(context);
        tv.setEllipsize(null);
        tv.setHorizontallyScrolling(false);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        widget_ids = manager.getAppWidgetIds(ComponentName.unflattenFromString("com.gyorog.polycal/com.gyorog.polycal.PolyCalWidgetProvider"));

        if (widget_ids.length == 0) {
            tv.setText(getString(R.string.how_to_add_widget));
            tv.setTextSize((float)20.0);
            layout.addView(tv);

            Button accept_button = new Button(context);
            accept_button.setText("OK");
            accept_button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                });
            layout.addView(accept_button);
        } else if (widget_ids.length == 1) {
            Intent intent = new Intent(context, SettingsActivity.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_ids[0]);
            intent.putExtra("from", "MainActivity");
            startActivityForResult(intent, 0);
        } else {
            tv.setText(getString(R.string.choose_widget));
            tv.setTextSize((float)16.0);
            layout.addView(tv);
            for(int i=0; i<widget_ids.length; ++i){
                Button button = new Button(context);
                button.setText(String.format("Configure widget %d", 1 + i));
                button.setTag( widget_ids[i] );
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int widget_id = (int) v.getTag();
                        Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget_id);
                        intent.putExtra("from", "MainActivity");
                        startActivityForResult(intent, 0);
                    }
                });
                layout.addView(button);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // requestCode is always 0.
        if (resultCode == RESULT_CANCELED && widget_ids.length == 1) { // Quit app if the user clicks "back" while in the only widget's config window
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}