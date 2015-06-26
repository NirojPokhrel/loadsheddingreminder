package com.bishalniroj.loadsheddingreminder.database;

import com.bishalniroj.loadsheddingreminder.Utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LoadSheddingSQLiteHelper extends SQLiteOpenHelper {
	
	private static LoadSheddingSQLiteHelper mReminderHelper;
	
	private static final String DATABASE_NAME = "loadsheddinginfo.db";
	private static final int DATABASE_VERSION = 1;



	private LoadSheddingSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	public static LoadSheddingSQLiteHelper getInstance( Context cContext ) {
		if( mReminderHelper == null ) {
			synchronized(LoadSheddingSQLiteHelper.class){
				if( mReminderHelper == null ) {
                    mReminderHelper = new LoadSheddingSQLiteHelper(cContext);
				}
			}
		}
		return mReminderHelper;
	}
	
	@Override
	public void onCreate( SQLiteDatabase database ) {
		database.execSQL(LoadSheddingReminderListTable.DATABASE_REMINDER_LIST_TABLE_CREATE);
		//database.execSQL(DailyActivityTable.DATABASE_TASK_LIST_TABLE_CREATE);
		//also create another table
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Utilities.Logd("Changing from " + oldVersion + " to " + newVersion
				+ " data will be lost");
		db.execSQL("DROP TABLE IF EXISTS " + LoadSheddingReminderListTable.TABLE_REMINDERLIST);
		//db.execSQL("DROP TABLE IF EXISTS " + DailyActivityTable.TABLE_TASKLIST);
		//drop another table
		onCreate(db);
	}
}
