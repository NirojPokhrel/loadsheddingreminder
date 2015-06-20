package com.bishalniroj.loadsheddingreminder;

import android.util.Log;

/**
 * Created by Niroj Pokhrel on 6/16/2015.
 */
public class Utilities {
    private static int sAreaNumber;
    private static int sHour, sMins;

    public static final String TAG = "niroj";

    public static void Logd( String str ) {
        Log.d(TAG, str);
    }

    public static void Loge(String str ) {
        Log.e(TAG, str);
    }

    public static void SaveAreaNumber( int areaNumber ) {
        sAreaNumber = areaNumber;
    }

    public static void SaveHourAndMins( int hour, int mins ) {
        sHour = hour;
        sMins = mins;
    }

    public static final int REQUEST_CODE_SELECT_AREA = 1;
    public static final int REQUEST_CODE_TIME_PICKER = 2;
    public static final String INTENT_DATA_AREA_NUMBER = "area_number";
    public static final String INTENT_DATA_HOUR = "hour";
    public static final String INTENT_DATA_MIN = "min";
    public static final String LOADSHEDDING_BROADCAST_RECEIVER_ACTION = "com.niroj.alarmmangertest.ACTION";
}
