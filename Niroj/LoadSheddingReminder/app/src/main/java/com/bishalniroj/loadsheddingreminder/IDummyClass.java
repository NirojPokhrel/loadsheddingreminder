package com.bishalniroj.loadsheddingreminder;

import java.util.ArrayList;

/**
 * Created by Niroj Pokhrel on 6/15/2015.
 */
public interface IDummyClass {
    public class LoadSheddingData {
        public LoadSheddingData() {
            start_hour = 0;
            start_min = 0;
            end_hour = 0;
            end_min = 0;
        }
        public LoadSheddingData( int a, int b, int c ,int d ) {
            start_hour = a;
            start_min = b;
            end_hour = c;
            end_min = d;
        }
        int start_hour;
        int start_min;
        int end_hour;
        int end_min;
    }
    public int GetNumberOfAreas();
    public String[] GetAreas();
    public ArrayList<LoadSheddingData> GetLoadSheddingInfoForADay( int day );
}
