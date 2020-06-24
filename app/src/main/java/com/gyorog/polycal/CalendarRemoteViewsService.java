package com.gyorog.polycal;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CalendarRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CalendarRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}