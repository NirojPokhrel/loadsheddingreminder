package com.bishalniroj.loadsheddingreminder;

/**
 * Created by Niroj Pokhrel on 6/15/2015.
 */
public class TestDataClass implements IDummyClass {
    private int mNumberOfAreas;
    private String[] mNameOfAreas;
    private final int MAX_NUM_OF_AREAS = 7;

    public TestDataClass() {
        mNumberOfAreas = 7;
        mNameOfAreas = new String[MAX_NUM_OF_AREAS];
        for( int i=0; i<MAX_NUM_OF_AREAS; i++ ) {
            mNameOfAreas[i]  = "Area " + (i+1);
        }
    }
    @Override
    public int GetNumberOfAreas() {
        return mNumberOfAreas;
    }

    @Override
    public String[] GetAreas() {
        return mNameOfAreas;
    }
}
