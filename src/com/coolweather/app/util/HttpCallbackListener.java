package com.coolweather.app.util;

public interface HttpCallbackListener {
	
	void OnFinish (String resoinse);
	void onError (Exception e);
}
