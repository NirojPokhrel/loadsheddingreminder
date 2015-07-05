package com.bishalniroj.loadsheddingreminder.database;

import android.content.Context;
import android.database.SQLException;

import com.bishalniroj.loadsheddingreminder.Utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
    * Make it a singleton. And
 */
public class LoadSheddingScheduleDbHelper {
    private int mNumberOfAreas = -1;
    private Utilities.AreaSchedulingInfo[] mAreaInfo;

    private static LoadSheddingScheduleCompleteInfoTable mScheduleInfoTable;
    private static LoadSheddingScheduleDbHelper mDbHelper = null;
    private static int mReferenceCount = 0;

	private LoadSheddingScheduleDbHelper(Context cCtx, Boolean writeFlag) {
        if(mScheduleInfoTable == null) {
            mScheduleInfoTable = new LoadSheddingScheduleCompleteInfoTable(cCtx);
        }
        mAreaInfo = new Utilities.AreaSchedulingInfo[Utilities.MAXIMUM_NUMBER_OF_AREA];
        for( int i=0; i<Utilities.MAXIMUM_NUMBER_OF_AREA; i++ ) {
            mAreaInfo[i] = new Utilities.AreaSchedulingInfo();
            mAreaInfo[i].mWeekInfo = new Utilities.DaySchedulingInfo[7];
            for( int j=0; j<7; j++ ) {
                mAreaInfo[i].mWeekInfo[j] = new Utilities.DaySchedulingInfo();
                mAreaInfo[i].mWeekInfo[j].mDailySched = new ArrayList<>();
            }
        }

        //For testing
        mScheduleInfoTable.open();
        if(writeFlag) {
            mScheduleInfoTable.dropTable();
            mScheduleInfoTable.createTable();
        }
        //fillDatabase();
	}

    public static LoadSheddingScheduleDbHelper GetInstance(Context ctx, Boolean writeFlag ) {
        if( mDbHelper == null ) {
            synchronized(LoadSheddingScheduleDbHelper.class) {
                if( mDbHelper == null ) {
                    mDbHelper = new LoadSheddingScheduleDbHelper(ctx, writeFlag);
                }
            }
        }

        return mDbHelper;
    }
	
	public void open() throws SQLException {
        //mScheduleInfoTable.open();
        if( mReferenceCount == 0 )
            listAllSchedInfo();
        mReferenceCount++;
	}
	
	public void close() {
        mReferenceCount--;
        if( mReferenceCount == 0 ) {
            mScheduleInfoTable.close();
            mDbHelper = null;
        }
	}
	
	public long insertReminder( Utilities.LoadSheddingCompleteSchedule schedData ) {

		return mScheduleInfoTable.insertScheduleInfo(schedData);
	}
	
	private void listAllSchedInfo() {
		List<Utilities.LoadSheddingCompleteSchedule> listTask = mScheduleInfoTable.getAllScheduleInfo();
        for( int i=0; i<listTask.size(); i++ ) {
            Utilities.LoadSheddingCompleteSchedule schedData = listTask.get(i);
            if( schedData.mAreaNum > Utilities.MAXIMUM_NUMBER_OF_AREA ) {
                Utilities.Loge("Number of areas is more than ten.");

                return;
            }
            if( schedData.mAreaNum > mNumberOfAreas )
                mNumberOfAreas = schedData.mAreaNum;
            mAreaInfo[schedData.mAreaNum].mWeekInfo[schedData.mDay].mDailySched.add(schedData.mLoadsheddingInfo);
        }

        for (int i = 0; i < mNumberOfAreas; i++) {
            for (int j = 0; j < 7; j++) {
                Collections.sort(mAreaInfo[i].mWeekInfo[j].mDailySched);
            }
        }
	}

    //TODO: Check how the mAreaInfo mWeekInfo mDailySched is prepopulated for this to work
    /*
    public ArrayList<Utilities.LoadSheddingScheduleData> GetSchedDataForADay( int areaNum, int day ) {
        return mAreaInfo[areaNum].mWeekInfo[day].mDailySched;
    }
    */

    public ArrayList<Utilities.LoadSheddingScheduleData> GetSchedDataForADay( int areaNum, int day ) {
        return mScheduleInfoTable.getScheduleForADayAndArea(areaNum, day);
    }


    public List<Utilities.LoadSheddingCompleteSchedule> GetAllSchedule(  ) {
        return mScheduleInfoTable.getAllScheduleInfo();
    }

    // TODO:
    public void removeRow( int id ) {
    }

    //fill database with real data
    public void fillDatabaseReal(ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>>
                                  sD)
    {
        ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>> allScheduleData =
      ( ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>>)sD.clone();
        for (int i=0;i<allScheduleData.size();i++){
        ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>> areaScheduleData = allScheduleData.get(i);
            for (int j=0; j<areaScheduleData.size();j++){
                ArrayList<Utilities.LoadSheddingScheduleData> dayScheduleData = areaScheduleData.get(j);
                for (Utilities.LoadSheddingScheduleData indvSchedule:dayScheduleData){
                    int startHour = indvSchedule.mStartHour;
                    int startMins = indvSchedule.mStartMins;
                    int endHour   = indvSchedule.mEndHour;
                    int endMins   = indvSchedule.mEndMins;
                    mScheduleInfoTable.insertScheduleInfo(new Utilities.LoadSheddingCompleteSchedule
                            (i, j, new Utilities.LoadSheddingScheduleData(startHour, startMins,
                                    endHour, endMins)));
                }
            }
        }
    }



}
