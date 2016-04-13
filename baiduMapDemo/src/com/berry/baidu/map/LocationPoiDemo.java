package com.berry.baidu.map;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
/**
 * 定位加周边搜索
 * 
 */
public class LocationPoiDemo extends Activity {

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;

	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI相关
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton,searchButton;
	boolean isFirstLoc = true;// 是否首次定位
	
	//搜索
	private PoiSearch mPoiSearch = null;
	//纬度，经度
	double latitude = 0;
	double longitude = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poi_location);
		requestLocButton = (Button) findViewById(R.id.button1);
		searchButton = (Button) findViewById(R.id.button2);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
		radioButtonListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.defaulticon) {
					// 传入null则，恢复默认图标
					mCurrentMarker = null;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
				}
				if (checkedId == R.id.customicon) {
					// 修改为自定义marker
					mCurrentMarker = BitmapDescriptorFactory
							.fromResource(R.drawable.icon_geo);
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
				}
			}
		};
		group.setOnCheckedChangeListener(radioButtonListener);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(5000);//5s进行一次定位
		mLocClient.setLocOption(option);
		mLocClient.start();
		Log.d("TEST", "start");
		
		
		//berry--搜索
		 // 初始化搜索模块，注册搜索事件监听
	    mPoiSearch = PoiSearch.newInstance();
	    mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
          
          @Override
          public void onGetPoiResult(PoiResult arg0) {
            if (arg0==null || arg0.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {//未找到
              Toast.makeText(LocationPoiDemo.this, "未找到", Toast.LENGTH_SHORT).show();
              return;
            }
            
            if (arg0.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回  
              mBaiduMap.clear();
              PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
//              CustomPoiOverlay overlay = new CustomPoiOverlay(mBaiduMap);
              mBaiduMap.setOnMarkerClickListener(overlay);
              overlay.setData(arg0);// 设置POI数据
              overlay.addToMap();// 将所有的overlay添加到地图上
              overlay.zoomToSpan();//以最好的视角显示结果
              List<PoiInfo> poiInfos = arg0.getAllPoi();
              //所有兴趣点数
              int totalPoi=arg0.getTotalPoiNum();
              for(int i = 0; i<totalPoi;i++){
                PoiInfo info = poiInfos.get(i);
              }
              //总分页数
              int totalPage=arg0.getTotalPageNum();
              Log.d("TEST", "总共找到："+totalPoi+"个，分为:"+totalPage+"页显示");
              return;
            }
            
            if (arg0.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {//在本市内未找到但在其他市找到，arg0中返回的是其他市的城市列表
              String strInfo = "在";
              for (CityInfo cityInfo : arg0.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
              }
              strInfo += "找到结果";
              Toast.makeText(LocationPoiDemo.this, strInfo, Toast.LENGTH_LONG).show();
            }
            
          }
          
          @Override
          public void onGetPoiDetailResult(PoiDetailResult arg0) {
            if (arg0.error != SearchResult.ERRORNO.NO_ERROR) {
              Toast.makeText(LocationPoiDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(LocationPoiDemo.this, arg0.getName() + ": " + arg0.getAddress(),
                  Toast.LENGTH_SHORT).show();
            }
            
          }
        });
		
		searchButton.setOnClickListener(new OnClickListener() {
          
          @Override
          public void onClick(View v) {
            EditText editText = (EditText) findViewById(R.id.edit_key);
            String key = editText.getText().toString();
            EditText distancEditText = (EditText) findViewById(R.id.edit_distance);
            int distance = Integer.parseInt(distancEditText.getText().toString());
            nearbySearch(0, 22.550149, 113.908398, distance, key);
          }
        });
		
	}
	
	 /** 
	   * 附近检索 
	   */  
	  private void nearbySearch(int page,double x,double y,int radius,String key) {  
	      PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();  
	      nearbySearchOption.location(new LatLng(x, y));  //22.550149, 113.908398
	      nearbySearchOption.keyword(key);  
	      nearbySearchOption.radius(radius);// 检索半径，单位是米  
	      nearbySearchOption.pageNum(page);  
//	      nearbySearchOption.pageCapacity(20);
	      nearbySearchOption.sortType(PoiSortType.distance_from_near_to_far);
	      mPoiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求  
	  }

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			Log.d("TEST", "location:radius="+location.getRadius()+",addrStr="+location.getAddrStr()+",Latitude="+location.getLatitude()+",Longitude="+location.getLongitude());
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
			Log.d("TEST", "poiLocation="+poiLocation);
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		
		mPoiSearch.destroy();
		super.onDestroy();
	}
	
	
	private class MyPoiOverlay extends PoiOverlay {

	    public MyPoiOverlay(BaiduMap baiduMap) {
	      super(baiduMap);
	    }

	    @Override
	    public boolean onPoiClick(int index) {
	      super.onPoiClick(index);
	      PoiInfo poi = getPoiResult().getAllPoi().get(index);
	      Log.d("TEST", "您点击的是第"+index+"数据"+",类型="+poi.type+",名称="+poi.name+",详细地址="+poi.address);
	      // if (poi.hasCaterDetails) {
	      // 检索poi详细信息 
	      mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
	      // }
	      return true;
	      
	    }
	  }
	
	
	//自定义mark图标
	  private class CustomPoiOverlay extends OverlayManager{
	    private PoiResult result;
	    
	    public void setData(PoiResult result) {
	            this.result = result;
	    }

	    public CustomPoiOverlay(BaiduMap baiduMap) {
	            super(baiduMap);
	    }

	    @Override
	    public boolean onMarkerClick(Marker marker) {
	            onClick(marker.getZIndex());
	            return true;
	    }
	    
	    public boolean onClick(int index) {
	            PoiInfo poi = result.getAllPoi().get(index);
	            if(poi.hasCaterDetails){
	                    mPoiSearch.searchPoiDetail(
	                                    (new PoiDetailSearchOption())
	                                    .poiUid(poi.uid));
	            }
	            return true;
	    }

	    @Override
	    public List<OverlayOptions> getOverlayOptions() {
	            List<OverlayOptions> ops = new ArrayList<OverlayOptions>();
	            List<PoiInfo> pois = result.getAllPoi();
	            Log.d("TEST1", "总数据="+pois.size());
	            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
	            for(int i = 0;i < pois.size();i++){
	                    OverlayOptions op = new MarkerOptions().position(pois.get(i).location).icon(bitmap);
	                    ops.add(op);
	                    mBaiduMap.addOverlay(op).setZIndex(i);;
	            }
	            return ops;
	    }

	    @Override
	    public boolean onPolylineClick(Polyline arg0) {
	      // TODO Auto-generated method stub
	      return false;
	    }
	    
	}

}
