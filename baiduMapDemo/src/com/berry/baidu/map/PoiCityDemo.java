package com.berry.baidu.map;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 在城市内检索
 * 
 * @author berry
 * 
 */
public class PoiCityDemo extends FragmentActivity
    implements
      OnGetPoiSearchResultListener,
      OnGetSuggestionResultListener {

  private PoiSearch mPoiSearch = null;
  private SuggestionSearch mSuggestionSearch = null;
  private BaiduMap mBaiduMap = null;
  /**
   * 搜索关键字输入窗口
   */
  private AutoCompleteTextView keyWorldsView = null;
  private ArrayAdapter<String> sugAdapter = null;
  private int load_Index = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    setContentView(R.layout.activity_poi_city);
    // 初始化搜索模块，注册搜索事件监听
    mPoiSearch = PoiSearch.newInstance();
    mPoiSearch.setOnGetPoiSearchResultListener(this);
    mSuggestionSearch = SuggestionSearch.newInstance();
    mSuggestionSearch.setOnGetSuggestionResultListener(this);
    keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
    sugAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
    keyWorldsView.setAdapter(sugAdapter);
    mBaiduMap =
        ((SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map)))
            .getBaiduMap();

    /**
     * 设置地图初始化城市深圳
     * 深圳市政府经纬度值：22.5485150000,114.0661120000
     * 在线查询经纬度地址:http://www.gpsspg.com/maps.htm
     */
    //设置中心点坐标
    LatLng latLng = new LatLng(22.5485150000,114.0661120000);
    //定义地图状态,zoom--地图缩放级别 3~19
    MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(12).build();
    MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
    //改变地图状态
    mBaiduMap.setMapStatus(statusUpdate);
        
    
    /**
     * 当输入关键字变化时，动态更新建议列表
     */
    keyWorldsView.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged(Editable arg0) {

      }

      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

      }

      @Override
      public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        if (cs.length() <= 0) {
          return;
        }
        String city = ((EditText) findViewById(R.id.city)).getText().toString();
        /**
         * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
         */
        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(cs.toString())
            .city(city));
      }
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    mPoiSearch.destroy();
    mSuggestionSearch.destroy();
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
  }

  /**
   * 影响搜索按钮点击事件
   * 
   * @param v
   */
  public void searchButtonProcess(View v) {
    EditText editCity = (EditText) findViewById(R.id.city);
    EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
    // 城市检索
    // city--检索城市,keyword--关键字，pageNum--分页编号，pageCapacity--每页容量，默认每页10条
    citySearch(load_Index,editCity.getText().toString(),editSearchKey.getText().toString());
  }
  /** 
   * 城市内搜索 
   */  
  private void citySearch(int page,String city,String key) {  
      // 设置检索参数  
      PoiCitySearchOption citySearchOption = new PoiCitySearchOption();  
      citySearchOption.city(city);// 城市  
      citySearchOption.keyword(key);// 关键字  
      citySearchOption.pageCapacity(7);// 默认每页10条  
      citySearchOption.pageNum(page);// 分页编号  
      // 发起检索请求  
      mPoiSearch.searchInCity(citySearchOption);  
  }  

  public void goToNextPage(View v) {
    load_Index++;
    searchButtonProcess(null);
  }

  public void onGetPoiResult(PoiResult result) {
    if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
      Toast.makeText(PoiCityDemo.this, "未找到结果", Toast.LENGTH_LONG).show();
      return;
    }
    if (result.error == SearchResult.ERRORNO.NO_ERROR) {
      mBaiduMap.clear();
//      PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);//使用系统默认mark图标
      CustomPoiOverlay overlay = new CustomPoiOverlay(mBaiduMap);//使用自定义mark图标
      mBaiduMap.setOnMarkerClickListener(overlay);
      overlay.setData(result);
      overlay.addToMap();
      overlay.zoomToSpan();
      return;
    }
    if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

      // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
      String strInfo = "在";
      for (CityInfo cityInfo : result.getSuggestCityList()) {
        strInfo += cityInfo.city;
        strInfo += ",";
      }
      strInfo += "找到结果";
      Toast.makeText(PoiCityDemo.this, strInfo, Toast.LENGTH_LONG).show();
    }
  }

  public void onGetPoiDetailResult(PoiDetailResult result) {
    if (result.error != SearchResult.ERRORNO.NO_ERROR) {
      Toast.makeText(PoiCityDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(PoiCityDemo.this, result.getName() + ": " + result.getAddress(),
          Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onGetSuggestionResult(SuggestionResult res) {
    if (res == null || res.getAllSuggestions() == null) {
      return;
    }
    sugAdapter.clear();
    for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
      if (info.key != null) sugAdapter.add(info.key);
    }
    sugAdapter.notifyDataSetChanged();
  }

  //使用系统默认mark图标
  private class MyPoiOverlay extends PoiOverlay {

    public MyPoiOverlay(BaiduMap baiduMap) {
      super(baiduMap);
    }

    @Override
    public boolean onPoiClick(int index) {
      super.onPoiClick(index);
      PoiInfo poi = getPoiResult().getAllPoi().get(index);
      // if (poi.hasCaterDetails) {
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
