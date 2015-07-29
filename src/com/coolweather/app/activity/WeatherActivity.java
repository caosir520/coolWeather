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
	 * ������ʾ����ʱ��*/
	private TextView pubilshTime;
	/*
	 * ������ʾ��������*/
	private TextView weatherDespText;
	/*������ʾ�¶�1*/
	private TextView temp1Text ;
	/**������ʾ�¶�2*/
	private TextView temp2Text ;
	/*��ʾ��ǰ������*/
	private TextView currentDateText;
	private Button switchCity ;
	private Button refreshWeather ;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature (Window.FEATURE_NO_TITLE);
		setContentView (R.layout.weather_layout);
		/*���ֿؼ��İ�*/
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
			//��ʡ�����ŵ�ʱ���ȥ��ѯ������
			pubilshTime.setText("ͬ����....");
			//���ò��ɼ�
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
			pubilshTime.setText("ͬ����.....");
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
	/*��ѯʡ����������Ӧ����������*
	 **/

	private void queryWeatherCode(String countyCode){
		String address= "http://www.weather.com.cn/data/list3/city" +countyCode + ".xml";
		queryFromServer(address,"countyCode");
	}
	/*��ѯ�����������Ե�����*/
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
						pubilshTime.setText("ͬ��ʧ��");
					}
				});
			}
			
			@Override
			public void OnFinish(String resoinse) {
				//�ֿ�����
				if ("countyCode".equals(type)){
					if (!TextUtils.isEmpty(resoinse)){
						//1.��������������
						String[] array = resoinse.split("\\|");
						
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							//Log.d("WeatherActivity", weatherCode);
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)){
					//������������ص�������Ϣ
					//Log.d("WeatherActivity", resoinse);
							
					Utility.handleWeatherResponse(WeatherActivity.this, resoinse);
					//�������߳̽���UI����
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
	 * ��Sharedpreferences �ļ���ȡ����*/

	private void showWeather (){
		//1.��ȡ��SHaredPreference ����
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Log.d("WeatherActivity", prefs.getString("city_name", ""));
		Log.d("Weather", " "+prefs.getString("city_name", ""));
		//��һ���Ǽ��ڶ�����Ĭ��ֵ
		cityNameText.setText( prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		pubilshTime.setText("����" + prefs.getString("publish_time", "") + "����");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	
}
