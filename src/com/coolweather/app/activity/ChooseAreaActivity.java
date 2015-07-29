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
	
	/*�ж��Ƿ�Ϊ��ת*/
	private boolean isFromWeatherActivity;
	private ProgressDialog progressDialog;
	
	private TextView titleText;
	private ListView listView ;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	/*ʡ�б����б����б�*/
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	//ѡ�е�ʡ�ݣ�ѡ�еĳ���
	private Province selectedProvince;
	private City selectedCity;
	//ѡ�еļ���
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
		//adapter ΪListView��������
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel ==LEVEN_PROVINCE){
					//�����ȡ����ʲô��Ϣ,����ĳ���
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
		queryProvinces();//����ʡ������
	}
	
	/*
	 * ���ȫ�����е�ʡ�����������ݿ��ѯ����ȥ��������ѯ*/
	private void queryProvinces(){
		
		/*���ݿⲿ��*/
		provinceList =coolWeatherDB.loadProvince();
		if(provinceList.size()!=0){
			dataList.clear();
			
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			
			//�����ʲô��˼��
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEN_PROVINCE;
		}else {
			queryFromServer(null,"province");
		}
	}
	/*
	 * ��ѯȫʡ��������*/
	private void queryCities(){
		cityList = coolWeatherDB.loadCity(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for (City city :cityList){
				//�����ݷ���dataListz����
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			//�����Ǹ�ʡΪ��Ŀͷ
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEN_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	/*
	 * ��ѯ�е�������*/
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

	/*���ݴ��ź����ʹӷ������ϲ�ѯʡ��������
	 * �ӷ������ϻ�ȡ�����ڷ���Ա���ݿ⣬��ͨ�����ݿ���в�ѯ*/
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
				//ͨ��runOnUiThread�������߳�
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
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
					//ͨ��runOnUiThread()�����ص����߳�
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
	 * ��ʾ���ȶԻ���*/
	private void showProgressDialog (){
		if (progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*�رս��ȶԻ���*/
	private void closeProgressDialog() {
		if (progressDialog != null) {
		progressDialog.dismiss();
		}
	}
	/*����Back���������ߵ�ǰ�ļ����жϣ����ص�����*/

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
