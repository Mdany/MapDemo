package com.chenyu.monster.mapdemo.bai;

import android.content.Context;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by chenyu on 16/5/16.
 */
public class LocationUtils {
    private static LocationUtils instance;

    public LocationUtils() {
        init();
    }

    /**
     * 单例
     *
     * @return
     */
    public static LocationUtils getInstance() {
        if (instance == null) {
            instance = new LocationUtils();
        }
        return instance;
    }

    /**
     * ====================定位部分========================
     **/
    private LocationClient locationClient;
    /**
     * 定位数据回调
     */
    private LocationListener locationListener;

    /**
     * 初始化
     */
    public static void init() {
        SDKInitializer.initialize(MapApplication.application);
    }

    /**
     * 定位初始化
     *
     * @param context
     */
    public void initLocationClient(final Context context) {
        locationClient = new LocationClient(context);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//高精度
        option.setCoorType("bd09ll");
//        int span = 3000;
//        option.setScanSpan(span);//定时扫描定位，>=1000才会生效
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                int code = bdLocation.getLocType();
                if (code == 61 || code == 161 || code == 66 || code == 65) {
                    locationListener.getLocation(bdLocation);
                } else {
                    Toast.makeText(context, "定位失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 开始定位
     */
    public void startLocation() {
        locationClient.start();
    }

    /**
     * 停止定位
     */
    public void stopLocation() {
        if (locationClient.isStarted()) {
            locationClient.stop();
        }
    }

    public void setLocationListener(LocationListener listener) {
        locationListener = listener;
    }

    public interface LocationListener {
        void getLocation(BDLocation bdLocation);
    }

    /**====================检索部分========================**/

}
