package com.bishalniroj.loadsheddingreminder;

import java.util.ArrayList;

/**
 * Created by Niroj Pokhrel on 6/15/2015.
 */
public class TestDataClass implements IDummyClass {
    private int mNumberOfAreas;
    private String[] mNameOfAreas;
    private final int MAX_NUM_OF_AREAS = 7;
    private static IDummyClass mDummyClass;
    private ArrayList<ArrayList<LoadSheddingData>> mArrayListOfData;

    private TestDataClass() {
        ArrayList<LoadSheddingData> arrayList;
        mArrayListOfData = new ArrayList<ArrayList<LoadSheddingData>>();
        for( int i=0; i<7; i++ ) {
            arrayList = new ArrayList<LoadSheddingData>();
            arrayList.add( new LoadSheddingData(3, 4, 5, 6));
            arrayList.add( new LoadSheddingData(13, 14, 23, 24));
            mArrayListOfData.add(arrayList);
        }
        mNumberOfAreas = 7;
        mNameOfAreas = new String[MAX_NUM_OF_AREAS];
        for( int i=0; i<MAX_NUM_OF_AREAS; i++ ) {
            mNameOfAreas[i]  = "Area " + (i+1);
        }
    }

    public static IDummyClass GetInstanceOfClass() {
        if( mDummyClass == null ) {
            synchronized (TestDataClass.class) {
                if( mDummyClass == null ) {
                    mDummyClass = new TestDataClass();
                }
            }
        }

        return mDummyClass;
    }

    @Override
    public int GetNumberOfAreas() {
        return mNumberOfAreas;
    }

    @Override
    public String[] GetAreas() {
        return mNameOfAreas;
    }

    @Override
    public ArrayList<LoadSheddingData> GetLoadSheddingInfoForADay( int day ) {
        return mArrayListOfData.get(day);
    }
}
