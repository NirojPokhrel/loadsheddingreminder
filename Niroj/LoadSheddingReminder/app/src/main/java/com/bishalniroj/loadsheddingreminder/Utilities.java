package com.bishalniroj.loadsheddingreminder;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Niroj Pokhrel on 6/16/2015.
 */
public class Utilities {
    public static final String SHARED_PREFERENCES ="com.bishalniroj.loadsheddingreminder.PREFERENCE_KEY" ;
    public static final String SHARED_PREFERENCES_TAB_NUMBER = "tab_number";
    public static final String SHARED_PREFERENCES_FIRST_TIME = "first_time";
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

    public static void showToast( Context ctx, String str ) {
        Toast.makeText(ctx, str, Toast.LENGTH_SHORT ).show();
    }

    public static final int REQUEST_CODE_SELECT_AREA = 1;
    public static final int REQUEST_CODE_TIME_PICKER = 2;
    public static final String INTENT_DATA_AREA_NUMBER = "area_number";
    public static final String INTENT_DATA_HOUR = "hour";
    public static final String INTENT_DATA_MIN = "min";
    public static final String LOADSHEDDING_BROADCAST_RECEIVER_ACTION = "com.niroj.alarmmangertest.ACTION";
    public static final String REMINDER_LIST_FILE = "/data/opt/temp/reminderList";
    public static final String SCHEDULE_LIST = "/data/opt/tmp/scheduleList";

    // To do: Implement the saving and extracting the data using file operations
    public static ArrayList<LoadSheddingScheduleData> readReminderList() throws IOException {
        DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(REMINDER_LIST_FILE)));

        fis.close();
        return null;
    }

    public static void writeReminderList(LoadSheddingScheduleData loadSheddingData) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream( new FileOutputStream(REMINDER_LIST_FILE)));

        dos.close();
    }

    public static class LoadSheddingScheduleData implements Comparable {
        public int mStartHour;
        public int mStartMins;
        public int mEndHour;
        public int mEndMins;

        public LoadSheddingScheduleData() {
            mStartHour = 0;
            mStartMins = 0;
            mEndHour = 0;
            mEndMins = 0;
        }

        public LoadSheddingScheduleData( int startHour, int startMin, int endHour, int endMin ) {
            mStartHour = startHour;
            mStartMins = startMin;
            mEndHour = endHour;
            mEndMins = endMin;
        }

        @Override
        public int compareTo(Object another) {
            LoadSheddingScheduleData schedData = (LoadSheddingScheduleData)another;
            if( schedData.mStartHour == this.mStartHour ) {
                if( schedData.mStartMins == this.mStartMins )
                    return 0;
                else if( schedData.mStartMins < this.mStartMins )
                    return 1;
                else
                    return -1;
            } else if( schedData.mStartHour > this.mStartHour ) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static class LoadSheddingReminderData implements Comparable {

        public int mID;
        public int mAreaNum;
        public int mDay;
        public int mReminderFrequency;
        public LoadSheddingScheduleData mLoadsheddingInfo;
        public int mHourBefore;
        public int mMinsBefore;
        public String mDate;

        public LoadSheddingReminderData() {
            mID = 0;
            mAreaNum = -1;
            mDay  = -1;
            mReminderFrequency = -1;
            mLoadsheddingInfo = null;
            mHourBefore = -1;
            mMinsBefore = -1;
            mDate = "";
        }

        public LoadSheddingReminderData( int id, int areaNum, int day, int reminderFrequency,
                                         LoadSheddingScheduleData scheduleData, int hoursBefore,
                                         int minsBefore, String date ) {
            mID = id;
            mAreaNum = areaNum;
            mDay = day;
            mReminderFrequency = reminderFrequency;
            mLoadsheddingInfo = scheduleData;
            mHourBefore = hoursBefore;
            mMinsBefore = minsBefore;
            mDate = date;
        }

        @Override
        public int compareTo(Object another) {
            LoadSheddingReminderData reminderData = (LoadSheddingReminderData)another;

            Calendar cal = Calendar.getInstance();
            //What is the value of days of week 0 t0 6 ??
            int day = cal.get(Calendar.DAY_OF_WEEK);
            int tempDayThis, tempDayAnother;

            tempDayThis = mDay;
            tempDayAnother = reminderData.mDay;

            if( tempDayThis < day )
                tempDayThis = tempDayThis + 7;
            if( tempDayAnother < day )
                tempDayAnother = tempDayAnother + 7;

            if( tempDayThis < tempDayAnother ) {
                return -1;
            } else if( tempDayThis > tempDayAnother ) {
                return 1;
            } else {
                if( this.mLoadsheddingInfo.mStartHour < reminderData.mLoadsheddingInfo.mStartHour ) {
                    return -1;
                } else if( this.mLoadsheddingInfo.mStartHour > reminderData.mLoadsheddingInfo.mStartHour ) {
                    return 1;
                } else {
                    if( this.mLoadsheddingInfo.mStartMins < reminderData.mLoadsheddingInfo.mStartMins ) {
                        return -1;
                    } else if( this.mLoadsheddingInfo.mStartMins > reminderData.mLoadsheddingInfo.mStartMins ) {
                        return 1;
                    } else
                        return 0;
                }
            }
        }
    }

    public static class LoadSheddingCompleteSchedule {
        public int mAreaNum;
        public int mDay;
        public LoadSheddingScheduleData mLoadsheddingInfo;
        public LoadSheddingCompleteSchedule() {

        }

        public LoadSheddingCompleteSchedule( int area, int day, LoadSheddingScheduleData sched ) {
            mAreaNum = area;
            mDay = day;
            mLoadsheddingInfo = sched;
        }
    }

    public static class AreaSchedulingInfo {
        public int mAreaNum;
        public DaySchedulingInfo[] mWeekInfo;
    }

    public static class DaySchedulingInfo {
        public int mDay;
        public ArrayList<LoadSheddingScheduleData> mDailySched;
    }

    public static final int SUNDAY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 3;
    public static final int WEDNESDAY = 4;
    public static final int THURSDAY = 5;
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 7;

    //Currently 7 but it might increase in future
    public static final int MAXIMUM_NUMBER_OF_AREA = 10;

}
