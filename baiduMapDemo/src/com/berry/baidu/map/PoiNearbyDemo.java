package com.berry.baidu.map;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
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
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 在给定范围内检索
 * 
 * @author berry
 * 
 */
public class PoiNearbyDemo extends FragmentActivity
    implements
      OnGetPoiSearchResultListener
      {

  private PoiSearch mPoiSearch = null;
  private BaiduMap mBaiduMap = null;
  private int load_Index = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    setContentView(R.layout.activity_poi_nearby);
    // 初始化搜索模块，注册搜索事件监听
    mPoiSearch = PoiSearch.newInstance();
 // 设置检索监听器 
    mPoiSearch.setOnGetPoiSearchResultListener(this);
    mBaiduMap =
        ((SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map)))
            .getBaiduMap();
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
    nearbySearch(load_Index, editCity.getText().toString(),editSearchKey.getText().toString());
  }

  /** 
   * 附近检索 
   */  
  private void nearbySearch(int page,String radius,String key) {  
      PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption();  
      nearbySearchOption.location(new LatLng(22.550149, 113.908398));  //智恒产业园纬度经度
      nearbySearchOption.keyword(key);  
      nearbySearchOption.radius(Integer.parseInt(radius));// 检索半径，单位是米  
      nearbySearchOption.pageNum(page);  
      nearbySearchOption.pageCapacity(7);
      nearbySearchOption.sortType(PoiSortType.distance_from_near_to_far);//排序方式
      mPoiSearch.searchNearby(nearbySearchOption);// 发起附近检索请求  
  } 
  
  public void goToNextPage(View v) {
    load_Index++;
    searchButtonProcess(null);
  }
//POI检索结果监听
  public void onGetPoiResult(PoiResult result) {
    if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果  
      Toast.makeText(PoiNearbyDemo.this, "未找到结果", Toast.LENGTH_LONG).show();
      return;
    }
    if (result.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回  
      mBaiduMap.clear();
      PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
      mBaiduMap.setOnMarkerClickListener(overlay);
      overlay.setData(result);// 设置POI数据
      overlay.addToMap();// 将所有的overlay添加到地图上
      overlay.zoomToSpan();
      
      //所有兴趣点数
      int totalPoi=result.getTotalPoiNum();
      //总分页数
      int totalPage=result.getTotalPageNum();
      Log.d("TEST", "总共找到："+totalPoi+"个，分为:"+totalPage+"页显示");
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
      Toast.makeText(PoiNearbyDemo.this, strInfo, Toast.LENGTH_LONG).show();
    }
  }
//POI检索结果监听
  public void onGetPoiDetailResult(PoiDetailResult result) {
    if (result.error != SearchResult.ERRORNO.NO_ERROR) {
      Toast.makeText(PoiNearbyDemo.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(PoiNearbyDemo.this, result.getName() + ": " + result.getAddress(),
          Toast.LENGTH_SHORT).show();
    }
  }

  private class MyPoiOverlay extends PoiOverlay {

    public MyPoiOverlay(BaiduMap baiduMap) {
      super(baiduMap);
    }

    @Override
    public boolean onPoiClick(int index) {
      super.onPoiClick(index);
      PoiInfo poi = getPoiResult().getAllPoi().get(index);
      switch (poi.type) {
        case POINT:
          Toast.makeText(PoiNearbyDemo.this, "名称:"+poi.name+",地址:"+poi.address,Toast.LENGTH_LONG).show();
          break;
        case BUS_STATION:
          Toast.makeText(PoiNearbyDemo.this, "站点:"+poi.name+",路线:"+poi.address,Toast.LENGTH_LONG).show();
          break;
        case BUS_LINE:
          Toast.makeText(PoiNearbyDemo.this, poi.address,Toast.LENGTH_LONG).show();
          break;
        case SUBWAY_STATION:
          Toast.makeText(PoiNearbyDemo.this, "站点:"+poi.name+",路线:"+poi.address,Toast.LENGTH_LONG).show();
          break;
        case SUBWAY_LINE:
          Toast.makeText(PoiNearbyDemo.this, poi.address,Toast.LENGTH_LONG).show();
          break;

        default:
          break;
      }
      // if (poi.hasCaterDetails) {
      // 检索poi详细信息 
     // mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
      // }
      return true;
    }
  }
  
  
}
