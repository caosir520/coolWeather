package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;
import com.example.coolweather.app.R;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEN_PROVINCE = 0;
	public static final int LEVEN_CITY =1;
	public static final int LEVEN_COUNTY =2;
	
	/*判断是否为跳转*/
	private boolean isFromWeatherActivity;
	private ProgressDialog progressDialog;
	
	private TextView titleText;
	private ListView listView ;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	/*省列表，市列表，县列表*/
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	//选中的省份，选中的城市
	private Province selectedProvince;
	private City selectedCity;
	//选中的级别
	private int currentLevel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false)&& !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		titleText = (TextView)findViewById(R.id.title_text);
		listView =(ListView)findViewById(R.id.list_view);
		//adapter 为ListView的适配器
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel ==LEVEN_PROVINCE){
					//这里获取的是什么信息,点击的城市
					Log.d("MY", "onItemClick"+arg2);
					selectedProvince =provinceList.get(arg2);
					queryCities();
				}else if (currentLevel==LEVEN_CITY){
					selectedCity=cityList.get(arg2);
					queryCounties();
				}else if (currentLevel==LEVEN_COUNTY){
					String countyCode =countyList.get(arg2).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
			
		}
		);
		queryProvinces();//加载省级数据
	}
	
	/*
	 * 查村全国所有的省，优先在数据库查询，在去服务器查询*/
	private void queryProvinces(){
		
		/*数据库部分*/
		provinceList =coolWeatherDB.loadProvince();
		if(provinceList.size()!=0){
			dataList.clear();
			
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			
			//这个是什么意思？
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEN_PROVINCE;
		}else {
			queryFromServer(null,"province");
		}
	}
	/*
	 * 查询全省的所有市*/
	private void queryCities(){
		cityList = coolWeatherDB.loadCity(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for (City city :cityList){
				//把数据放入dataListz组里
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			//设置那个省为题目头
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEN_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	/*
	 * 查询市的所有县*/
	private void queryCounties(){
		countyList =coolWeatherDB.loadCountry(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county :countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel =LEVEN_COUNTY;
		}else {
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}

	/*根据代号和类型从服务器上查询省县市数据
	 * 从服务器上获取数据在方人员数据库，在通过数据库进行查询*/
	private void queryFromServer(final String code ,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else {
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				//通过runOnUiThread返回主线程
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
					
				});
			}
			
			@Override
			public void OnFinish(String resoinse) {
				// TODO Auto-generated method stub
				boolean result =false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(coolWeatherDB, resoinse);
				}else if ("city".equals(type)){
					result=Utility.handleCitiesResponse(coolWeatherDB, resoinse, selectedProvince.getId());
				}else if ("county".equals(type)){
					result=Utility.handleCountiesResponse(coolWeatherDB, resoinse, selectedCity.getId());
				}
				if (result){
					//通过runOnUiThread()方法回到主线程
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if ("city".equals(type)){
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
						
					});
				}
			}
		});

	}
	/*
	 * 显示进度对话框*/
	private void showProgressDialog (){
		if (progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*关闭进度对话框*/
	private void closeProgressDialog() {
		if (progressDialog != null) {
		progressDialog.dismiss();
		}
	}
	/*捕获Back按键，更具当前的级别判断，返回的类型*/

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(currentLevel==LEVEN_COUNTY){
			queryCities();
		}
		else if(currentLevel==LEVEN_CITY){
			queryProvinces();
		}else if (currentLevel==LEVEN_PROVINCE){
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
				}
			finish();
		}
	}
	
}
