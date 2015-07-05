package com.bishalniroj.loadsheddingreminder.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.bishalniroj.loadsheddingreminder.HttpFetchAndParse;
import com.bishalniroj.loadsheddingreminder.R;
import com.bishalniroj.loadsheddingreminder.ReminderForLoadShedding;
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
        new HttpFetchAndParse(this).execute(mDbHelper);
    }


    //TODO: Pandey has to call this when there is change in notifications
    private void sendNotifications() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("LoadShedding Schedule Changed")
                        .setContentText("Old reminders will be useless. Click to add new reminder for the loadshedding.")
                        .setAutoCancel(true);
		/*
		 * In case setAutoCancel(true) doesn't work use following line
		 * mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
		 */
        Intent resultIntent = new Intent( this, ReminderForLoadShedding.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ReminderForLoadShedding.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.notify( 1, builder.build());
    }

}
