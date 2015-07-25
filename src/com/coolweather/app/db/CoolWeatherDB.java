package com.coolweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	/*
	 * ���ݿ�����*/
	public static final String DB_NAME = "cool_weather";
	/*���ݿ�汾*/
	public static final int VERSION = 1;
	
	private static CoolWeatherDB coolWeatherDB;
	
	private SQLiteDatabase db ;
	/*�ɹ��췽��˽�л�   */
	private CoolWeatherDB(Context context){
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,DB_NAME, null,VERSION);
		db = dbHelper.getWritableDatabase();
	}
	/*
	 * ��ȡCoolWeatherDB��ʵ��*/
	public synchronized static CoolWeatherDB getInstance(Context context){
		if (coolWeatherDB==null){
			coolWeatherDB=new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	
	/*
	 * ��Province ʵ���浽���ݿ�*/
	public void saveProvince (Province pr){
		if (pr!=null){
			ContentValues values = new ContentValues();
			values.put("province_name", pr.getProvinceName());
			values.put("province_code", pr.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	/*
	 * �����ݿ����ȡ����ʡ�ݵ���Ϣ
	 **/
	public List<Province> loadProvince(){
		List <Province> list =new ArrayList<Province>();
		Cursor cursor = db.
				query("Province",null, null, null, null, null, null);
		if (cursor.moveToFirst()){
			do{
				Province province =new Province();
				province.setId(cursor.getColumnIndex("id"));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
			}while (cursor.moveToNext());
		}
		return list;
	}
	/*
	 * ��Cityʵ���洢�����ݿ�*/
	public void saveCity(City c){
		if(c!=null){
			ContentValues values = new ContentValues();
			values.put("city_name", c.getCityName());
			values.put("city_code", c.getCityCode());
			values.put("province_id", c.getProvinceId());
			db.insert("City", null, values);
		}
	}
	/*�����ݿ��ȡcity��Ϣ*/
	
	public List<City> loadCity(int provinceId){
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null );
		
		if (cursor.moveToFirst()){
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
				.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
				.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			}while (cursor.moveToNext());
		}
		return list;
		
	}


	/*County ����*/
	public void saveCounty(County c){
		if (c!=null){
			ContentValues valuse =new ContentValues();
			valuse.put("county_name", c.getCountyName());
			valuse.put("county_code", c.getCountyCode());
			valuse.put("city_id",c.getCityId());
			db.insert("County", null, valuse);
		}
	}
	public List<County> loadCountry (int id){
		List<County> list =new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id=?", new String[]{String.valueOf(id)}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor
				.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor
				.getColumnIndex("county_code")));
				county.setCityId(id);
				list.add(county);
			}while (cursor.moveToNext());
		}
		return list;
		
	}
}

