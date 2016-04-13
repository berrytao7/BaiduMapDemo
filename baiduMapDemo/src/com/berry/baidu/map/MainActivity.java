package com.berry.baidu.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  public void onBtnClick(View v){
    Intent intent = null;
    switch (v.getId()) {
      case R.id.btn_location:
        intent = new Intent(MainActivity.this, LocationDemo.class);
        startActivity(intent);
        break; 
      case R.id.btn_poi_city:
        intent = new Intent(MainActivity.this, PoiCityDemo.class);
        startActivity(intent);
        break;
      case R.id.btn_poi_bound:
        intent = new Intent(MainActivity.this, PoiBoundDemo.class);
        startActivity(intent);
        break;
      case R.id.btn_poi_nearby:
        intent = new Intent(MainActivity.this, PoiNearbyDemo.class);
        startActivity(intent);
        break;
      case R.id.btn_poi_location:
        intent = new Intent(MainActivity.this, LocationPoiDemo.class);
        startActivity(intent);
        break;
      case R.id.btn_test:
        intent = new Intent(MainActivity.this, LocationPoiDemo.class);
        startActivity(intent);
        break;

      default:
        break;
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
