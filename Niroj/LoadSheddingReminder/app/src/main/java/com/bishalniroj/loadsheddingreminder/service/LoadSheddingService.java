package com.bishalniroj.loadsheddingreminder.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.bishalniroj.loadsheddingreminder.Utilities;

public class LoadSheddingService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Utilities.Logd("Service onCreate()");
        //Check if data has been modified
        //Register for alarm service
	}
	
	@Override
	public void onDestroy() {
		Utilities.Logd("Service onDestroy()");
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
        Utilities.Logd("Service onStart()");
		super.onStart(intent, startId);
		
	}
}
