package com.chenyu.monster.mapdemo;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by chenyu on 16/5/16.
 */
public class LocationUtils {
    public static void init() {
        SDKInitializer.initialize(MapApplication.application);
    }
}
