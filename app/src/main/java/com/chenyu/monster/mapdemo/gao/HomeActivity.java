package com.chenyu.monster.mapdemo.gao;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.overlay.DrivingRouteOverlay;
import com.amap.api.maps2d.overlay.WalkRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.chenyu.monster.mapdemo.R;

/**
 * Created by chenyu on 16/5/18.
 */
public class HomeActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, RouteSearch.OnRouteSearchListener {
    private Context mContext;
    //map view
    private MapView twdMap;
    //map
    private AMap map;
    //定位结果回调
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    //路线搜索
    private RouteSearch routeSearch;
    //三种出行路线
    private WalkRouteResult mWalkRouteResult;
    private BusRouteResult mTransitRouteResult;
    private DriveRouteResult mDriveRouteResult;
    //起始点
    private LatLonPoint startNode;//起点，
    private LatLonPoint endNode = new LatLonPoint(39.917636, 116.397743);//终点，
    //出行方式itemId
    private int itemId;
    //记录定位点
    private AMapLocation mLocation;
    private DrivingRouteOverlay drivingRouteOverlay;
    private WalkRouteOverlay walkRouteOverlay;

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume()，实现地图生命周期管理
        twdMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause()，实现地图生命周期管理
        twdMap.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        twdMap.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        twdMap.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //三种出行方式，未定位成功则不允许选择
        if (mLocation == null) return super.onOptionsItemSelected(item);
        itemId = item.getItemId();
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startNode, endNode);
        if (drivingRouteOverlay != null) {
            drivingRouteOverlay.removeFromMap();
        }
        if (walkRouteOverlay != null) {
            walkRouteOverlay.removeFromMap();
        }
        switch (itemId) {
            case R.id.walk:
                RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                routeSearch.calculateWalkRouteAsyn(walkRouteQuery);
                return true;
            case R.id.transit:
                RouteSearch.BusRouteQuery busRouteQuery = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusDefault, mLocation.getCity(), 1);
                routeSearch.calculateBusRouteAsyn(busRouteQuery);
                return true;
            case R.id.drive:
                RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");
                routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_home);
        mContext = this;
        initView(savedInstanceState);
        initRoute();
        registerListener();
    }

    /**
     * 初始化view
     */
    private void initView(Bundle savedInstanceState) {
        twdMap = (MapView) findViewById(R.id.map_view);
        if (twdMap != null) {
            twdMap.onCreate(savedInstanceState);//此方法必须重写
            map = twdMap.getMap();
        }
        setupMap();
    }

    /**
     * 设置起始点icon
     */
    private void setFromAndToMarker() {
        map.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(startNode))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        map.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(endNode))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 设置map
     */
    private void setupMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        map.setLocationSource(this);//设置定位监听
        map.getUiSettings().setMyLocationButtonEnabled(true);//设置定位按钮是否显示
        map.setMyLocationEnabled(true);//设置true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认false
        map.setMyLocationStyle(myLocationStyle);
    }

    /**
     * 注册监听
     */
    private void registerListener() {
//        map.setOnMapClickListener(this);
//        map.setOnMarkerClickListener(this);
//        map.setOnInfoWindowClickListener(this);
//        map.setInfoWindowAdapter(this);
    }

    /**
     * 定位成功后回调函数
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {//错误码为0表示定位正常，即NO_ERROR
                mListener.onLocationChanged(aMapLocation);
                mLocation = aMapLocation;
                double lat = mLocation.getLatitude();
                double lng = mLocation.getLongitude();
                startNode = new LatLonPoint(lat, lng);
                setFromAndToMarker();
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Toast.makeText(mContext, errText, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 激活定位
     *
     * @param onLocationChangedListener
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption
                    .setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
                    .setInterval(4000);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 设置route
     */
    private void initRoute() {
        routeSearch = new RouteSearch(mContext);
        routeSearch.setRouteSearchListener(this);//设置回调监听
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        if (busRouteResult == null || i != 1000) {//1000代表正确，我觉得高德应该弄个自己的工具类命好名
            Toast.makeText(mContext, "公交路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        if (i != 1000) {
            Toast.makeText(mContext, "公交路线，未知错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (busRouteResult.getPaths().size() <= 0) {
            Toast.makeText(mContext, "公交路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        //bus我就不写了，太长了，百度比这不知高到哪里去了
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        if (driveRouteResult == null || i != 1000) {//1000代表正确，我觉得高德应该弄个自己的工具类命好名
            Toast.makeText(mContext, "自驾路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        if (i != 1000) {
            Toast.makeText(mContext, "自驾路线，未知错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (driveRouteResult.getPaths().size() <= 0) {
            Toast.makeText(mContext, "自驾路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        DrivePath drivePath = driveRouteResult.getPaths().get(0);
        DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(mContext, map, drivePath, driveRouteResult.getStartPos(), driveRouteResult.getTargetPos());
        this.drivingRouteOverlay = drivingRouteOverlay;
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomToSpan();
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        if (walkRouteResult == null || i != 1000) {//1000代表正确，我觉得高德应该弄个自己的工具类命好名
            Toast.makeText(mContext, "自驾路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        if (i != 1000) {
            Toast.makeText(mContext, "自驾路线，未知错误", Toast.LENGTH_SHORT).show();
            return;
        }

        if (walkRouteResult.getPaths().size() <= 0) {
            Toast.makeText(mContext, "自驾路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }

        WalkPath walkPath = walkRouteResult.getPaths().get(0);
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(mContext, map, walkPath, walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());
        this.walkRouteOverlay = walkRouteOverlay;
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomToSpan();
    }
}
