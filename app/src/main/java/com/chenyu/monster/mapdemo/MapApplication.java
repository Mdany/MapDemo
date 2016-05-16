package com.chenyu.monster.mapdemo;

import android.app.Application;

/**
 * Created by chenyu on 16/5/16.
 */
public class MapApplication extends Application {
    public static MapApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        LocationUtils.init();
    }
}
