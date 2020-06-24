package com.gyorog.polycal;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ScreenshotRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScreenshotRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}