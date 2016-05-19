# MapDemo
百度地图的demo
RouteOverlay使用系统默认Marker，我觉得没有必要使用自定义的，当然，这只是个demo，需要的话参考百度地图demo

坑：PlanNode endNode = PlanNode.withLocation(new LatLng(39.914935, 116.403694));
这里经纬度参数是正确的，如果传入经纬度反了，检索各种出行方式并绘制路线图时就会报result.ERROR = NOT_FOUND，前面是纬度，后面是经度，百度地图坐标拾取器获得的就是反的，小心被坑

坑：PlanNode.withCityNameAndPlaceName("北京","西单");
这个好像城市名识别不好用，我没有选择这个，因为经纬度更加准确，也更适合开发使用，反而这种字符串不好控制，易出错

高德有毒，就这样吧，不写了

