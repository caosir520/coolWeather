package com.coolweather.app.activity;



import com.coolweather.app.receiver.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;
import com.example.coolweather.app.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements View.OnClickListener{

	private LinearLayout weatherInfoLayout ;
	private TextView cityNameText;
	/**
	 * 用于显示发布时间*/
	private TextView pubilshTime;
	/*
	 * 用于显示天气描述*/
	private TextView weatherDespText;
	/*用于显示温度1*/
	private TextView temp1Text ;
	/**用与显示温度2*/
	private TextView temp2Text ;
	/*显示当前的日期*/
	private TextView currentDateText;
	private Button switchCity ;
	private Button refreshWeather ;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature (Window.FEATURE_NO_TITLE);
		setContentView (R.layout.weather_layout);
		/*各种控件的绑定*/
		weatherInfoLayout = (LinearLayout) findViewById (R.id.weather_info_layout);
		cityNameText= (TextView)findViewById(R.id.city_name);
		pubilshTime = (TextView)findViewById(R.id.publish_text);
		temp1Text = (TextView)findViewById(R.id.temp1);
		weatherDespText=(TextView)findViewById(R.id.weather_desp);
		temp2Text=(TextView)findViewById(R.id.temp2);
		currentDateText = (TextView)findViewById(R.id.current_date);
		switchCity= (Button)findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		String countyCode =getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)){
			//有省级代号的时候就去查询天气，
			pubilshTime.setText("同步中....");
			//设置不可见
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else {
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}


	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()){
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			pubilshTime.setText("同步中.....");
			SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode= prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	
	}
	/*查询省级代号所对应的天气代号*
	 **/

	private void queryWeatherCode(String countyCode){
		String address= "http://www.weather.com.cn/data/list3/city" +countyCode + ".xml";
		queryFromServer(address,"countyCode");
	}
	/*查询天气代号所对的天气*/
	private void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address,"weatherCode");
	}
	
	
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pubilshTime.setText("同步失败");
					}
				});
			}
			
			@Override
			public void OnFinish(String resoinse) {
				//分开类型
				if ("countyCode".equals(type)){
					if (!TextUtils.isEmpty(resoinse)){
						//1.解析出天气代号
						String[] array = resoinse.split("\\|");
						
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							//Log.d("WeatherActivity", weatherCode);
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)){
					//处理服务器返回的天气信息
					//Log.d("WeatherActivity", resoinse);
							
					Utility.handleWeatherResponse(WeatherActivity.this, resoinse);
					//返回主线程进行UI操作
					runOnUiThread(new Runnable(){
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}});
				}
			}
		});
	}

	/*
	 * 从Sharedpreferences 文件读取数据*/

	private void showWeather (){
		//1.获取到SHaredPreference 对象
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Log.d("WeatherActivity", prefs.getString("city_name", ""));
		Log.d("Weather", " "+prefs.getString("city_name", ""));
		//第一个是键第二个是默认值
		cityNameText.setText( prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		pubilshTime.setText("今天" + prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	
}
