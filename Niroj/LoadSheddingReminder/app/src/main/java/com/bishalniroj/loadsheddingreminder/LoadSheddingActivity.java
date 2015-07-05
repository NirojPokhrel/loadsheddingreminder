package com.bishalniroj.loadsheddingreminder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bishalniroj.loadsheddingreminder.database.LoadSheddingScheduleDbHelper;
import com.bishalniroj.loadsheddingreminder.service.BroadCastReceivers;
import com.bishalniroj.loadsheddingreminder.service.LoadSheddingService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;


public class LoadSheddingActivity extends Activity {
    private static Context mContext;
    private LoadSheddingScheduleDbHelper mDbHelper;
    private static PendingIntent databaseUpdateIntent;

    public static boolean sIsAlertContext = false;

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

        if (!IsAppRunningForFirstTime()) {
            mPlayerList.run();
        }
    }

    public boolean mIsOnResumeAfterOnCreate = true;

    @Override
    public void onResume() {
        super.onResume();
        Utilities.Logd("onResume Called");
        if (mIsOnResumeAfterOnCreate) {
            mIsOnResumeAfterOnCreate = false;
            return;
        }
        if (sIsAlertContext) {
            onActivityResult_test(FINAL_ACTIVITY_RESULT_WIFI, RESULT_OK, null);
            sIsAlertContext = false;
        }
    }

    //This one is for smooth running of database when application starts so that it won't delay when request is made
    private Runnable mPlayerList = new Runnable() {

        @Override
        public void run() {
            Utilities.Logd("Runnable is running");
            mDbHelper = LoadSheddingScheduleDbHelper.GetInstance(mContext, true);
            mDbHelper.open();
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LandingPageFragment extends Fragment {
        private Button mBtnAdjustReminder, mBtnViewSchedule;
        private ProgressBar mSpinner;

        public LandingPageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_landing_page, container, false);
            mBtnAdjustReminder = (Button) rootView.findViewById(R.id.adjustReminder);
            mBtnViewSchedule = (Button) rootView.findViewById(R.id.viewSchedule);

            mBtnAdjustReminder.setOnClickListener(mOnClickListener);
            mBtnViewSchedule.setOnClickListener(mOnClickListener);

            mSpinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
            mSpinner.setVisibility(View.GONE);

            return rootView;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (view.getId()) {
                    case R.id.adjustReminder: {
                        intent = new Intent(mContext, ReminderForLoadShedding.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.viewSchedule:
                        intent = new Intent(mContext, TabbedViewScheduleActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        };
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

    private boolean IsAppRunningForFirstTime() {
        SharedPreferences sharedPref = getSharedPreferences(Utilities.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        boolean isFirstTime = sharedPref.getBoolean(Utilities.SHARED_PREFERENCES_FIRST_TIME, true);
        if (isFirstTime) {
            //Check for the internet connectivity
            //Start the service and register for pending intent
            if (!hasActiveInternetConnection()) {
                //Start
                DialogFragment newFragment = new InternetConnectionChoiceDialog();
                newFragment.show(getFragmentManager(), "RepetitionDialog");
            } else {
                Utilities.Logd("Calling first time initialization");
                firstTimeInitializations();
/*                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Utilities.SHARED_PREFERENCES_FIRST_TIME, false);
                editor.commit();*/
            }
            Utilities.Logd("App running first time");
            return true;
        }
        Utilities.Logd("This is not the first time app is running");
        return false;
    }

    //Check in new thread
    public boolean hasActiveInternetConnection() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Utilities.Loge("Error checking internet connection");
            }
        } else {
            Utilities.Logd("No network available!");
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static class InternetConnectionChoiceDialog extends DialogFragment implements DialogInterface.OnCancelListener {
        public InternetConnectionChoiceDialog() {

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            sIsAlertContext = true;
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
            alertBuilder.setTitle("Connect to Internet");
            alertBuilder.setMessage("App gets load shedding schedule from internet.");
            alertBuilder.setCancelable(false);
            alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utilities.showToast(mContext, "Closing!!!NO INTERNET CONNECTION");
                    getActivity().finish();
                }
            });

            alertBuilder.setPositiveButton("Use Wifi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivityForResult(intent, FINAL_ACTIVITY_RESULT_WIFI);
                    dismiss();
                }
            });

            alertBuilder.setNeutralButton("Use Cellular", new DialogInterface.OnClickListener() {
                //TODO: How the fuck can the data usage page be launched directly ? No straight forward method needs to be checked.
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivityForResult(intent, FINAL_ACTIVITY_RESULT_CELLULAR);
                    dismiss();

                }
            });
            Dialog dialog = alertBuilder.create();
            dialog.setCanceledOnTouchOutside(false);

            return dialog;
        }
    }


    //@Override
    public void onActivityResult_test(int requestCode, int resultCode, Intent data) {
        Utilities.Logd("onActivityResult");
        if (requestCode == FINAL_ACTIVITY_RESULT_WIFI || requestCode == FINAL_ACTIVITY_RESULT_CELLULAR) {
            if (resultCode == RESULT_OK) {
                //Give sometime for internet connection
                mHandler.sendEmptyMessageDelayed(MESSAGE_START_SERVICE, 1000);
            } else {
                Utilities.showToast(mContext, "CLOSING!!!Unable to turn on internet");
                finish();
            }
        }
    }

    private Handler mHandler = new Handler() {
        //Try for 5 times
        private int mRetryCount = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_START_SERVICE:
                    if (hasActiveInternetConnection()) {
                        firstTimeInitializations();
                        break;
                    }
                    mRetryCount++;
                    if (mRetryCount > 5) {
                        Utilities.showToast(mContext, "CLOSING!!!CANNOT CONNECT TO INTERNET");
                        ((Activity) mContext).finish();
                    }
                    mHandler.sendEmptyMessageDelayed(MESSAGE_START_SERVICE, 1000);
                    break;
            }
        }
    };

    //TODO:
    public void firstTimeInitializations() {
        Utilities.showToast(mContext, "Connected to Internet");
        //Pending Intent that will broadcast for database update
        Intent alarmIntent = new Intent(mContext, BroadCastReceivers.class);
        alarmIntent.setAction(Utilities.LOADSHEDDING_BROADCAST_RECEIVER_ACTION);
        databaseUpdateIntent = PendingIntent.getBroadcast(mContext, 0, alarmIntent, 0);

        //boolean isAlarmSet = (PendingIntent.getBroadcast(mContext, 0,
        //        new Intent(Utilities.LOADSHEDDING_BROADCAST_RECEIVER_ACTION),
        //        PendingIntent.FLAG_NO_CREATE) != null);
        //if (!isAlarmSet) {
        //start the alarm
        //    Utilities.Logd("Scheduling the alarm for periodic database update");
        startLoadSheddingDownloadAlarm();

        startService(new Intent(mContext, LoadSheddingService.class));
        //}
    }

    public static final int FINAL_ACTIVITY_RESULT_WIFI = 3;
    public static final int FINAL_ACTIVITY_RESULT_CELLULAR = 4;

    public static final int MESSAGE_START_SERVICE = 5;
}
