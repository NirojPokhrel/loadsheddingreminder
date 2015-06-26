package com.bishalniroj.loadsheddingreminder.database;

import android.content.Context;
import android.database.SQLException;

import com.bishalniroj.loadsheddingreminder.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoadSheddingScheduleDbHelper {
    private int mNumberOfAreas = -1;
    private Utilities.AreaSchedulingInfo[] mAreaInfo;

    private LoadSheddingScheduleCompleteInfoTable mScheduleInfoTable;

	public LoadSheddingScheduleDbHelper(Context cCtx) {
        mScheduleInfoTable = new LoadSheddingScheduleCompleteInfoTable(cCtx);
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
        mScheduleInfoTable.dropTable();
        mScheduleInfoTable.createTable();
        fillDatabase();
	}
	
	public void open() throws SQLException {
        //mScheduleInfoTable.open();

        listAllSchedInfo();
	}
	
	public void close() {
		mScheduleInfoTable.close();
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
            //TODO: make sure to sort the time so that list has time in ascending order
        }
	}

    public ArrayList<Utilities.LoadSheddingScheduleData> GetSchedDataForADay( int areaNum, int day ) {
        return mAreaInfo[areaNum].mWeekInfo[day].mDailySched;
    }

    public Utilities.AreaSchedulingInfo GetAreaSchedInfo( int areaNum ) {
        return mAreaInfo[areaNum];
    }

    // Think of better way to do it !!!
    public void removeRow( int id ) {
    }

    //Fill database
    /*
      * Following information is for testing
      *
     */

    private void fillDatabase() {
        Random rand = new Random();

        for( int i=0; i<7; i++ ) {
            for( int j=0; j<7; j++ ) {
                for( int k=0; k<2; k++ ) {
                    mScheduleInfoTable.insertScheduleInfo(new Utilities.LoadSheddingCompleteSchedule
                            (i, j, new Utilities.LoadSheddingScheduleData(rand.nextInt(24), rand.nextInt(60),rand.nextInt(24), rand.nextInt(60))));
                }
            }
        }

    }

}
