package com.bishalniroj.loadsheddingreminder.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.bishalniroj.loadsheddingreminder.HttpFetchAndParse;
import com.bishalniroj.loadsheddingreminder.Utilities;
import com.bishalniroj.loadsheddingreminder.database.LoadSheddingScheduleDbHelper;

public class LoadSheddingService extends Service {

    private static LoadSheddingScheduleDbHelper mDbHelper;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Utilities.Logd("Service onCreate()");
        mDbHelper = LoadSheddingScheduleDbHelper.GetInstance(this,true);
        //When the service is first created launch the data download thread
        //start the thread to download the loadshedding schedule activity
        //provide the instance of the database where the data can be stored

        startDownloadThread(mDbHelper);

        //Check if data has been modified
        //Register for alarm service
	}
	
	@Override
	public void onDestroy() {
		Utilities.Logd("Service onDestroy()");
	}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utilities.Logd("Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    private void startDownloadThread(LoadSheddingScheduleDbHelper mDbHelper) {
        new HttpFetchAndParse().execute(mDbHelper);


    }

}
