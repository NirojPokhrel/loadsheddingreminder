package com.bishalniroj.loadsheddingreminder.database;

import java.util.ArrayList;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.bishalniroj.loadsheddingreminder.Utilities;

public class LoadSheddingReminderListTable {

	//Table info
	public static final String TABLE_REMINDERLIST = "reminderlist";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_AREA_NUM="areanum";
    public static final String COLUMN_DAY_NUM ="daynum";
    public static final String COLUMN_REMINDER_FREQUECNY="frequency";
	public static final String COLUMN_START_HOUR="starthour";
	public static final String COLUMN_START_MINS="startmins";
	public static final String COLUMN_END_HOUR="endhour";
	public static final String COLUMN_END_MINS="endmins";
    public static final String COLUMN_NHOUR_BEFORE="nhourbefore";
    public static final String COLUMN_NMINS_BEFORE="nminsbefore";
    public static final String COLUMN_DATE="date";

	public static final String DATABASE_REMINDER_LIST_TABLE_CREATE =
	        " CREATE TABLE " + TABLE_REMINDERLIST
	        + " ( "
	        +     COLUMN_ID      + " INTEGER PRIMARY KEY, "
            +     COLUMN_AREA_NUM + " INTEGER, "
            +     COLUMN_DAY_NUM + " INTEGER, "
            +     COLUMN_REMINDER_FREQUECNY + " INTEGER, "
	        +	  COLUMN_START_HOUR + " INTEGER, "
	    	+	  COLUMN_START_MINS + " INTEGER, "
	    	+	  COLUMN_END_HOUR + " INTEGER, "
	    	+	  COLUMN_END_MINS + " INTEGER, "
            +     COLUMN_NHOUR_BEFORE + " INTEGER, "
            +     COLUMN_NMINS_BEFORE + " INTEGER, "
	        + 	  COLUMN_DATE + " TEXT "
	        + " )";
	
	//
	private LoadSheddingSQLiteHelper mSQLHelper;
	private SQLiteDatabase mDataBase;
	
	private final String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_AREA_NUM, COLUMN_DAY_NUM,
            COLUMN_REMINDER_FREQUECNY,COLUMN_START_HOUR, COLUMN_START_MINS, COLUMN_END_HOUR,
            COLUMN_END_MINS, COLUMN_NHOUR_BEFORE, COLUMN_NMINS_BEFORE,
            COLUMN_DATE };
	
	public LoadSheddingReminderListTable( Context cCtx ) {
		mSQLHelper = LoadSheddingSQLiteHelper.getInstance(cCtx);
	}
	
	public void open() throws SQLException {
		mDataBase = mSQLHelper.getWritableDatabase();
	}
	
	public void close() {
		mSQLHelper.close();
	}
	
	public long insertReminder( Utilities.LoadSheddingReminderData reminderData ) {
		ContentValues content;
		
		content = new ContentValues();
        content.put(COLUMN_ID, reminderData.mID);
		content.put(COLUMN_AREA_NUM, reminderData.mAreaNum);
        content.put(COLUMN_DAY_NUM, reminderData.mDay);
        content.put(COLUMN_REMINDER_FREQUECNY, reminderData.mReminderFrequency);
		content.put(COLUMN_START_HOUR, reminderData.mLoadsheddingInfo.mStartHour);
		content.put(COLUMN_START_MINS, reminderData.mLoadsheddingInfo.mStartMins);
		content.put(COLUMN_END_HOUR, reminderData.mLoadsheddingInfo.mEndHour);
		content.put(COLUMN_END_MINS, reminderData.mLoadsheddingInfo.mEndMins);
		content.put(COLUMN_NHOUR_BEFORE, reminderData.mHourBefore );
        content.put(COLUMN_NMINS_BEFORE, reminderData.mMinsBefore );
		content.put(COLUMN_DATE, reminderData.mDate);
		
		return mDataBase.insert(TABLE_REMINDERLIST, null, content);
	}
	
	public List<Utilities.LoadSheddingReminderData> getAllReminders() {
		List<Utilities.LoadSheddingReminderData> listTask = new ArrayList<Utilities.LoadSheddingReminderData>();
		
		Cursor cursor = mDataBase.query( TABLE_REMINDERLIST, ALL_COLUMNS, null, null, null, null, null );
		cursor.moveToFirst();
		while(!cursor.isAfterLast() ) {
            Utilities.LoadSheddingReminderData data = cursorToTaskData(cursor);
			listTask.add(data);
			cursor.moveToNext();
		}
		return listTask;
	}
	
	private Utilities.LoadSheddingReminderData cursorToTaskData( Cursor cursor ) {
        Utilities.LoadSheddingReminderData reminderData = new Utilities.LoadSheddingReminderData();
        Utilities.LoadSheddingScheduleData loadSheddingInfo = new Utilities.LoadSheddingScheduleData();

        //No need of Id for the user)
        reminderData.mID = cursor.getInt(0);
        reminderData.mAreaNum = cursor.getInt(1);
        reminderData.mDay = cursor.getInt(2);
        reminderData.mReminderFrequency = cursor.getInt(3);

        loadSheddingInfo.mStartHour = cursor.getInt(4);
        loadSheddingInfo.mStartMins = cursor.getInt(5);
        loadSheddingInfo.mEndHour = cursor.getInt(6);
        loadSheddingInfo.mEndMins = cursor.getInt(7);

        reminderData.mLoadsheddingInfo = loadSheddingInfo;

        reminderData.mHourBefore = cursor.getInt(8);
        reminderData.mMinsBefore = cursor.getInt(9);
        reminderData.mDate = cursor.getString(10);
		
		return reminderData;
	}

    // Think of better way to do it !!!
    public void removeRow( int id ) {
        mDataBase.execSQL("DELETE FROM "+TABLE_REMINDERLIST+" WHERE " + COLUMN_ID + " = " + id);
    }
}
