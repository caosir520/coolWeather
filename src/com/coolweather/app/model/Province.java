package com.coolweather.app.model;

public class Province {
	private int id ;
	private String provinceName=null;
	private String provinceCode=null;
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id=id;
	}
	
	
	public String getProvinceName(){
		return provinceName;
	}

	public void setProvinceName(String s){
		this.provinceName=s;
	}

	
	public String getProvinceCode(){
		return provinceCode;
	}

	public void setProvinceCode(String s){
		this.provinceCode=s;
	}
}
