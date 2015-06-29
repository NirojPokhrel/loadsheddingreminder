package com.bishalniroj.loadsheddingreminder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.bishalniroj.loadsheddingreminder.Utilities;

import java.util.ArrayList;
import java.util.List;

public class LoadSheddingScheduleCompleteInfoTable {

	//Table info
	public static final String TABLE_SCHEDULE_INFO = "schedulelist";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_AREA_NUM="areanum";
    public static final String COLUMN_DAY_NUM ="daynum";
	public static final String COLUMN_START_HOUR="starthour";
	public static final String COLUMN_START_MINS="startmins";
	public static final String COLUMN_END_HOUR="endhour";
	public static final String COLUMN_END_MINS="endmins";
    public static final String COLUMN_DATE="date";

	public static final String DATABASE_REMINDER_LIST_TABLE_CREATE =
	        " CREATE TABLE " + TABLE_SCHEDULE_INFO
	        + " ( "
	        +     COLUMN_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            +     COLUMN_AREA_NUM + " INTEGER, "
            +     COLUMN_DAY_NUM + " INTEGER, "
	        +	  COLUMN_START_HOUR + " INTEGER, "
	    	+	  COLUMN_START_MINS + " INTEGER, "
	    	+	  COLUMN_END_HOUR + " INTEGER, "
	    	+	  COLUMN_END_MINS + " INTEGER, "
	        + 	  COLUMN_DATE + " TEXT "
	        + " )";

	//
	private LoadSheddingSQLiteHelper mSQLHelper;
	private SQLiteDatabase mDataBase;

	private final String[] ALL_COLUMNS = { COLUMN_ID, COLUMN_AREA_NUM, COLUMN_DAY_NUM,
            COLUMN_START_HOUR, COLUMN_START_MINS, COLUMN_END_HOUR,
            COLUMN_END_MINS, COLUMN_DATE };

	public LoadSheddingScheduleCompleteInfoTable(Context cCtx) {
		mSQLHelper = LoadSheddingSQLiteHelper.getInstance(cCtx);
	}
	
	public void open() throws SQLException {
		mDataBase = mSQLHelper.getWritableDatabase();
	}
	
	public void close() {
		mSQLHelper.close();
	}
	
	public long insertScheduleInfo( Utilities.LoadSheddingCompleteSchedule schedData ) {
		ContentValues content;
        /*
		Utilities.Logd("Recieved Database storage request");
        Utilities.Logd("Area:" + schedData.mAreaNum + "Day: "+ schedData.mDay);
        Utilities.Logd("Data= "+ "Hour:" + schedData.mLoadsheddingInfo.mStartHour+ "Min:"+
                schedData.mLoadsheddingInfo.mStartMins);
        */
		content = new ContentValues();
		content.put(COLUMN_AREA_NUM, schedData.mAreaNum);
        content.put(COLUMN_DAY_NUM, schedData.mDay);
		content.put(COLUMN_START_HOUR, schedData.mLoadsheddingInfo.mStartHour);
		content.put(COLUMN_START_MINS, schedData.mLoadsheddingInfo.mStartMins);
		content.put(COLUMN_END_HOUR, schedData.mLoadsheddingInfo.mEndHour);
		content.put(COLUMN_END_MINS, schedData.mLoadsheddingInfo.mEndMins);
		
		return mDataBase.insert(TABLE_SCHEDULE_INFO, null, content);
	}
	
	public List<Utilities.LoadSheddingCompleteSchedule> getAllScheduleInfo() {
		List<Utilities.LoadSheddingCompleteSchedule> listTask = new ArrayList<Utilities.LoadSheddingCompleteSchedule>();
		
		Cursor cursor = mDataBase.query( TABLE_SCHEDULE_INFO, ALL_COLUMNS, null, null, null, null, null );
		cursor.moveToFirst();
		while(!cursor.isAfterLast() ) {
            Utilities.LoadSheddingCompleteSchedule data = cursorToTaskData(cursor);
			listTask.add(data);
			cursor.moveToNext();
		}
		return listTask;
	}

    //TODO: Directly query the table
    public ArrayList<Utilities.LoadSheddingScheduleData> getScheduleForADayAndArea(int area, int day) {
        ArrayList<Utilities.LoadSheddingScheduleData> daySchedule = new ArrayList<>();
        //get all the schedule info
        List<Utilities.LoadSheddingCompleteSchedule> allSchedule = getAllScheduleInfo();
        for (Utilities.LoadSheddingCompleteSchedule compSchedule:allSchedule) {
            if(compSchedule.mAreaNum == area && compSchedule.mDay == day) {
                daySchedule.add(compSchedule.mLoadsheddingInfo);
            }
        }
        return daySchedule;
    }

	private Utilities.LoadSheddingCompleteSchedule cursorToTaskData( Cursor cursor ) {
        Utilities.LoadSheddingCompleteSchedule schedData = new Utilities.LoadSheddingCompleteSchedule();
        Utilities.LoadSheddingScheduleData loadSheddingInfo = new Utilities.LoadSheddingScheduleData();

        //No need of Id for the user)
        schedData.mAreaNum = cursor.getInt(1);
        schedData.mDay = cursor.getInt(2);
        loadSheddingInfo.mStartHour = cursor.getInt(3);
        loadSheddingInfo.mStartMins = cursor.getInt(4);
        loadSheddingInfo.mEndHour = cursor.getInt(5);
        loadSheddingInfo.mEndMins = cursor.getInt(6);

        schedData.mLoadsheddingInfo = loadSheddingInfo;
		
		return schedData;
	}

    // Think of better way to do it !!!
    public void removeRow( int id ) {
        mDataBase.execSQL("DELETE FROM " + TABLE_SCHEDULE_INFO + " WHERE " + COLUMN_ID + " = " + id);
    }

    public void dropTable() {
        if( mDataBase == null ) {
            Utilities.Loge("Database is null in LoadSheddingScheduleCompleteInfoTable");
        }
        mDataBase.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE_INFO);
    }

    public void createTable() {
        mDataBase.execSQL(DATABASE_REMINDER_LIST_TABLE_CREATE);

    }
}
