package com.chenyu.monster.mapdemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity implements LocationUtils.LocationListener {
    private MapView mapView;
    private BaiduMap baiduMap;
    //经纬度
    private double lat;
    private double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setLocationListener();
    }

    /**
     * 初始化view
     */
    private void initView() {
        mapView = (MapView) findViewById(R.id.map_view);
        if (mapView == null) return;
        baiduMap = mapView.getMap();
    }

    /**
     * 定位
     */
    private void setLocationListener() {
        LocationUtils.getInstance().setLocationListener(this);
    }

    @Override
    public void getLocation(BDLocation bdLocation) {
        lat = bdLocation.getLatitude();
        lon = bdLocation.getLongitude();
        initMapMarker();
    }

    /**
     * marker初始化
     */
    private void initMapMarker() {
        //定义Maker坐标点
        LatLng point = new LatLng(lat, lon);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.logo);
        //构建MarkerOption，用于在地图上添加Marker
        MarkerOptions option = new MarkerOptions()
                .position(point)//定位显示marker位置
                .icon(bitmap)//marker icon
                .zIndex(9)//marker所在层级
                .draggable(true)//marker是否是可拖拽的
                .alpha(0.5f);//marker透明度
        option.animateType(MarkerOptions.MarkerAnimateType.grow);//marker使用成长动画

        //构建文字覆盖物
//        TextOptions option = new TextOptions()
//                .bgColor(Color.GREEN)
//                .fontSize(60)
//                .fontColor(Color.WHITE)
//                .position(point)
//                .rotate(-30)
//                .text("狗仔");

        //弹窗覆盖物
        //创建InfoWindow展示的view
        View pop = View.inflate(this, R.layout.map_pop_window, null);
        //定义用于显示该InfoWindow的坐标点，这里直接使用point就可以
        //创建InfoWindow，传入view，地理坐标点，y偏移量
        final InfoWindow infoWindow = new InfoWindow(pop, point, -47);

        //在地图上添加Marker，并显示
        baiduMap.addOverlay(option);
        baiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
                //拖拽中
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //拖拽结束
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                //拖拽开始
            }
        });
        //覆盖物marker点击事件中显示infoWindow
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                baiduMap.showInfoWindow(infoWindow);
                return false;
            }
        });
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                baiduMap.hideInfoWindow();
                Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                baiduMap.hideInfoWindow();
                Toast.makeText(MainActivity.this, "Poi click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause()，实现地图生命周期管理
        mapView.onPause();
        LocationUtils.getInstance().stopLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView. onDestroy ()，实现地图生命周期管理
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
        LocationUtils.getInstance().startLocation();
    }
}
