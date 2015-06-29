package com.bishalniroj.loadsheddingreminder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bishalniroj.loadsheddingreminder.database.LoadSheddingScheduleDbHelper;
import com.bishalniroj.loadsheddingreminder.service.BroadCastReceivers;
import com.bishalniroj.loadsheddingreminder.service.LoadSheddingService;

import java.util.Calendar;


public class LoadSheddingActivity extends Activity {
    private static Context mContext;
    private LoadSheddingScheduleDbHelper mDbHelper;
    private static PendingIntent databaseUpdateIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_load_shedding);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new LandingPageFragment(), "LoadSheddingFragment")
                    .commit();
        }

        //Pending Intent that will broadcast for database update
        Intent alarmIntent = new Intent(this, BroadCastReceivers.class);
        alarmIntent.setAction(Utilities.LOADSHEDDING_BROADCAST_RECEIVER_ACTION);
        databaseUpdateIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        //From:http://stackoverflow.com/questions/4556670/how-to-check-if-alarmmanager-already-has-an-alarm-set
        boolean isAlarmSet = (PendingIntent.getBroadcast(this, 0,
                new Intent(Utilities.LOADSHEDDING_BROADCAST_RECEIVER_ACTION),
                PendingIntent.FLAG_NO_CREATE) != null);
        if (!isAlarmSet) {
            //start the alarm
            Utilities.Logd("Scheduling the alarm for periodic database update");
            startLoadSheddingDownloadAlarm();
        }
        mPlayerList.run();
    }

    //This one is for smooth running of database when application starts so that it won't delay when request is made
    private Runnable mPlayerList = new Runnable() {

        @Override
        public void run() {
            mDbHelper = LoadSheddingScheduleDbHelper.GetInstance(mContext, true);
            mDbHelper.open();

            //run the loadshedding service to start the download of data in the database
            if(!isMyServiceRunning(LoadSheddingService.class)) {
                startService(new Intent(mContext, LoadSheddingService.class));
            }
        }

    };

    @Override
    public void onDestroy() {
      //  mDbHelper.close();
        super.onDestroy();
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LandingPageFragment extends Fragment {
        private Button mBtnSelectArea, mBtnAdjustReminder, mBtnViewSchedule;

        public LandingPageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_landing_page, container, false);
           // mBtnSelectArea = (Button) rootView.findViewById(R.id.selectArea);
            mBtnAdjustReminder = (Button) rootView.findViewById(R.id.adjustReminder);
            mBtnViewSchedule = (Button) rootView.findViewById(R.id.viewSchedule);

            //mBtnSelectArea.setOnClickListener(mOnClickListener);
            mBtnAdjustReminder.setOnClickListener(mOnClickListener);
            mBtnViewSchedule.setOnClickListener(mOnClickListener);

            return rootView;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
             public void onClick(View view) {
                Intent intent;
                switch(view.getId()) {
 /*                   case R.id.selectArea:
                        Utilities.Logd("Clicked select area");
                        intent = new Intent( mContext, SelectArea.class );
                        startActivityForResult(intent, Utilities.REQUEST_CODE_SELECT_AREA);
                        break;*/
                    case R.id.adjustReminder: {
                        //intent = new Intent( mContext, CustomTimePicker.class );
                        intent = new Intent( mContext, ReminderForLoadShedding.class );
                        startActivity(intent);
                        break;
                    }
                    case R.id.viewSchedule:
                        intent = new Intent( mContext, TabbedViewScheduleActivity.class );
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent intent) {
        Utilities.Loge("requestCode: "+ requestCode+ " resultCode: " + resultCode);
        if(resultCode != RESULT_OK ) {
            Utilities.Loge("Result code is not OK. Result Code : " + resultCode);

            return;
        }
        switch(requestCode) {
            case Utilities.REQUEST_CODE_SELECT_AREA:
                int areaNumber = intent.getIntExtra(Utilities.INTENT_DATA_AREA_NUMBER, -1 );
                if( areaNumber == -1 ) {
                    Toast.makeText(this,"Proper area is not detected. Please select proper area", Toast.LENGTH_SHORT).show();
                    Utilities.Loge("Proper area not detected");

                    return;
                }
                Utilities.Logd("Area Number: "+areaNumber);
                Utilities.SaveAreaNumber(areaNumber);
                break;
            case Utilities.REQUEST_CODE_TIME_PICKER:
                int hour = intent.getIntExtra(Utilities.INTENT_DATA_HOUR, -1 );
                if( hour == -1 ) {
                    Toast.makeText(this,"Proper Hour not set", Toast.LENGTH_SHORT).show();
                    Utilities.Loge("Proper Hour not set");

                    return;
                }
                int mins = intent.getIntExtra(Utilities.INTENT_DATA_MIN, -1 );
                if( mins == -1 ) {
                Toast.makeText(this,"Proper Minutes not set", Toast.LENGTH_SHORT).show();
                Utilities.Loge("Proper Minutes not set");

                return;
            }
                Utilities.SaveHourAndMins(hour,mins );
                break;
        }
    }

    /*
    To check if a service is running or not
    Ref: http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*
    Start alarm service
     */
    private void startLoadSheddingDownloadAlarm() {
        Calendar calendar = Calendar.getInstance();
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 1000 * 60 * 60 * 24; //in milliseconds
        //Repeat every 24 hours
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                interval, databaseUpdateIntent);
    }

}
