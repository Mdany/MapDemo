package com.chenyu.monster.mapdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.Address;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.route.BaiduMapRoutePlan;
import com.baidu.mapapi.utils.route.RouteParaOption;
import com.chenyu.monster.mapdemo.overlayutil.BikingRouteOverlay;
import com.chenyu.monster.mapdemo.overlayutil.DrivingRouteOverlay;
import com.chenyu.monster.mapdemo.overlayutil.OverlayManager;
import com.chenyu.monster.mapdemo.overlayutil.TransitRouteOverlay;
import com.chenyu.monster.mapdemo.overlayutil.WalkingRouteOverlay;

public class MainActivity extends AppCompatActivity implements
        LocationUtils.LocationListener, OnGetRoutePlanResultListener {
    private Context context;
    private MapView mapView;
    private BaiduMap baiduMap;
    //经纬度
    private double lat;
    private double lon;
    //地理信息对象：包括地址，国家，门牌号
    private Address address;
    //创建检索实例
    private RoutePlanSearch mSearch;
    //路线覆盖
    private OverlayManager routeOverlay;
    //具体哪个出行方式
    private int itemId = -1;
    //起始两点
    private PlanNode startNode;
    private PlanNode endNode;
    private LatLng startPoint;
    private LatLng endPoint;
    //测试用的经纬度偏移量及定位次数
//    private double offset = 0.005;
//    private int count = 1;

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
        //释放检索实力
        mSearch.destroy();
        //结束调启功能时调用finish方法以释放相关资源
        BaiduMapRoutePlan.finish(context);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
        LocationUtils.getInstance().startLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        initView();
        setLocationListener();
        initSearch();
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
     * 定位回调设置
     */
    private void setLocationListener() {
        LocationUtils.getInstance().setLocationListener(this);
    }

    @Override
    public void getLocation(BDLocation bdLocation) {
//        lat = bdLocation.getLatitude() + offset * count;
//        lon = bdLocation.getLongitude() + offset * count;
//        ++count;
        lat = bdLocation.getLatitude();
        lon = bdLocation.getLongitude();
        address = bdLocation.getAddress();
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
        //清除所有覆盖物和InfoWindow
        baiduMap.clear();
        //构建MarkerOption，用于在地图上添加Marker
        MarkerOptions options = new MarkerOptions()
                .position(point)//定位显示marker位置
                .icon(bitmap)//marker icon
                .zIndex(9)//marker所在层级
                .draggable(false)//marker是否是可拖拽的
                .alpha(0.5f);
        options.animateType(MarkerOptions.MarkerAnimateType.grow);//marker使用成长动画
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(options);

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
        Button go = (Button) pop.findViewById(R.id.go_baidu);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调起手机百度地图app，没有则调起百度地图webapp
                NaviParaOption naviParaOption;
                RouteParaOption routeParaOption;
                switch (itemId) {
                    case R.id.walk:
                        naviParaOption = new NaviParaOption()
                                .startPoint(startPoint)
                                .endPoint(endPoint);
                        BaiduMapNavigation.openBaiduMapWalkNavi(naviParaOption, context);
                        break;
                    case R.id.biking:
                        naviParaOption = new NaviParaOption()
                                .startPoint(startPoint)
                                .endPoint(endPoint);
                        BaiduMapNavigation.openBaiduMapBikeNavi(naviParaOption, context);
                        break;
                    case R.id.transit:
                        routeParaOption = new RouteParaOption()
                                .startPoint(startPoint)
                                .endPoint(endPoint)
                                .busStrategyType(RouteParaOption.EBusStrategyType.bus_recommend_way);//推荐线路
                        BaiduMapRoutePlan.openBaiduMapTransitRoute(routeParaOption, context);
                        break;
                    case R.id.drive:
                        routeParaOption = new RouteParaOption()
                                .startPoint(startPoint)
                                .endPoint(endPoint);
                        BaiduMapRoutePlan.openBaiduMapDrivingRoute(routeParaOption, context);
                        break;
                    default:
                        baiduMap.hideInfoWindow();
                        Toast.makeText(context,"请选择出行方式",Toast.LENGTH_SHORT).show();
                }
            }
        });
        //定义用于显示该InfoWindow的坐标点，这里直接使用point就可以
        //创建InfoWindow，传入view，地理坐标点，y偏移量
        final InfoWindow infoWindow = new InfoWindow(pop, point, -47);

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
        //点击骑行等出行方式overlay，也会激发此事件,此处return false而overlay里return true，则此事件优先级高
        //可能跟地图层级有关，暂时没有测试
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                baiduMap.showInfoWindow(infoWindow);
                return false;
            }
        });
        //地图点击事件
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {//地图其它位置点击事件
                baiduMap.hideInfoWindow();
                Toast.makeText(MainActivity.this, "click", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {//地图poi点点击事件
                baiduMap.hideInfoWindow();
                Toast.makeText(MainActivity.this, "Poi click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    /**
     * 初始化检索生成路径
     */
    private void initSearch() {
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {//步行路线结果
        if (walkingRouteResult == null
                || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "步行路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有歧义，通过以下接口获取建议查询信息
            //transitRouteResult.getSuggestAddrInfo()
            return;
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            WalkingRouteOverlay overlay = new WalkingRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(walkingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {//公共交通路线结果
        if (transitRouteResult == null
                || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "公共交通路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
        if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有歧义，通过以下接口获取建议查询信息
            //transitRouteResult.getSuggestAddrInfo()
            return;
        }
        if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            TransitRouteOverlay overlay = new TransitRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(transitRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {//驾车路线结果
        if (drivingRouteResult == null
                || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "自驾路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
        if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有歧义，通过以下接口获取建议查询信息
            //transitRouteResult.getSuggestAddrInfo()
            return;
        }
        if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(drivingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {//骑行路线结果
        if (bikingRouteResult == null
                || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "骑行路线，未找到结果", Toast.LENGTH_SHORT).show();
            return;
        }
        if (bikingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有歧义，通过以下接口获取建议查询信息
            //transitRouteResult.getSuggestAddrInfo()
            return;
        }
        if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            BikingRouteOverlay overlay = new BikingRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(bikingRouteResult.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //不同出行方式的调用
        itemId = item.getItemId();
        startPoint = new LatLng(lat, lon);
        endPoint = new LatLng(39.914935, 116.403694);
        startNode = PlanNode.withLocation(startPoint);
        endNode = PlanNode.withLocation(endPoint);
        if (routeOverlay != null) {//每次换overlay都要清除前一个
            routeOverlay.removeFromMap();
        }
        switch (itemId) {
            case R.id.walk:
                mSearch.walkingSearch((new WalkingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                return true;
            case R.id.biking:
                mSearch.bikingSearch((new BikingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                return true;
            case R.id.transit:
                if (address == null) break;
                mSearch.transitSearch((new TransitRoutePlanOption())
                        .from(startNode)
                        .city(address.city)
                        .to(endNode));
                return true;
            case R.id.drive:
                mSearch.drivingSearch((new DrivingRoutePlanOption())
                        .from(startNode)
                        .to(endNode));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
