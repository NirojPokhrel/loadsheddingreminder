package com.bishalniroj.loadsheddingreminder;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.bishalniroj.loadsheddingreminder.database.LoadSheddingScheduleDbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class HttpFetchAndParse extends AsyncTask<LoadSheddingScheduleDbHelper,
       Void,
        ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>>> {
    //Hashmap to store the loadshedding shedule
    //Key: Group Number, Value: List of Strings, with each string for each day
    static Map<Integer, ArrayList<String>> mapSchedule = new HashMap<Integer, ArrayList<String>>();
    //String for the currentGroup e.g. Group 1, Group 2
    static String currentGroup;
    static int keyGroup;
    //Fixed Url string from where the loadshedding schedule is read for parsing
    static String urlString = "http://battigayo.com/schedule";

    //Database
    private static LoadSheddingScheduleDbHelper mScheduleDbHelper;

    Context mContext;
    public HttpFetchAndParse(Context ctx) {
        mContext = ctx;
    }

    public ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>>
    doInBackground(LoadSheddingScheduleDbHelper... params) {
        //save the reference to the passed database
        mScheduleDbHelper =  params[0];
        //create the list to store the loadshedding data schedule
        ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>> mScheduleData =
                new ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>>();
        try

        {
            readSiteBattiGayo();
            for (int areaId=1;areaId<8;areaId++){
                ArrayList<String> areaList = mapSchedule.get(areaId);

                //area schedule
                ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>> mAreaData =
                        new ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>();

                //This is the list for each day: Sunday-Saturday
                for (String eachString : areaList) {
                    ArrayList<Utilities.LoadSheddingScheduleData> daySchedule =
                            new ArrayList<Utilities.LoadSheddingScheduleData>();
                    String[] splitSS = eachString.split(" ");
                    //This is the list for all loadshedding this particular day
                    for (int i = 2; i < splitSS.length; i++) {
                        int[] intSchedule = stringScheduleToIntSchedule(splitSS[i]);
                        //add to the list of daily schedule
                        daySchedule.add(new Utilities.LoadSheddingScheduleData(intSchedule[0],
                                intSchedule[1], intSchedule[2], intSchedule[3]));

                    }
                    //add the day's schedule in the schedule for this particular area
                    mAreaData.add(daySchedule);
                }
                mScheduleData.add(mAreaData);
                }
        }
        catch(Exception e )
        {
            Utilities.Logd("IO Exception Thrown!");
        }

        return mScheduleData;
    }

    public void onPostExecute(ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>> sData) {
        //debug print
        /*
        Utilities.Logd("Parsed Http Data");
        for (ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>> areaData : sData) {
            for (ArrayList<Utilities.LoadSheddingScheduleData> dayData : areaData) {
                for (Utilities.LoadSheddingScheduleData sD : dayData) {
                    printLoadSheddingSchedule(sD);
                }
            }
        }
        */
        //save to the database
        mScheduleDbHelper.fillDatabaseReal(sData);

        //TODO: Pandey sorry for hacking around your codes !!!Please check if not appropriate please find some other places
        SharedPreferences sharedPref = mContext.getSharedPreferences(Utilities.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        boolean isFirstTime = sharedPref.getBoolean(Utilities.SHARED_PREFERENCES_FIRST_TIME, true);
        if( isFirstTime ) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Utilities.SHARED_PREFERENCES_FIRST_TIME, false);
                editor.commit();
        }

    };

    /*
     * Parser for loadshedding schedule available from battigayo.com
     * Build up parsers from other websites for reliability
     * @ returns: Map <String, List<String>>
     */
    public static void readSiteBattiGayo() throws IOException {
        try {
            Document doc = Jsoup.connect(urlString).get();
            Elements scheduleBlock = doc.select("div.schedule-block-2").first().select("ul li");
            //Get the first group
            currentGroup = scheduleBlock.get(0).text();
            String ss = currentGroup.substring(5, currentGroup.length() - 1).trim();
            keyGroup = Integer.parseInt(ss);
            ArrayList<String> currentSchedule = new ArrayList<String>();
            for (int i = 1; i < scheduleBlock.size(); i++) {
                if (i % 8 == 0) {
                    //save the existing list to the hashmap
                    mapSchedule.put(keyGroup, (ArrayList<String>)currentSchedule.clone());
                    //get the new group name
                    currentGroup = scheduleBlock.get(i).text();
                    ss = currentGroup.substring(5, currentGroup.length() - 1).trim();
                    keyGroup = Integer.parseInt(ss);
                    //clear the schedule list
                    currentSchedule.clear();
                } else {
                    currentSchedule.add(scheduleBlock.get(i).text());
                }
            }
            //save the last key-pair and the values
            mapSchedule.put(keyGroup, (ArrayList<String>)currentSchedule.clone());

        } catch (Exception e) {
            Utilities.Logd(e.toString());
        }

    }
    /*
     * Convert passed string in the format HH:MM-HH:MM of the schedule to an array
     * of int [startHour, start Min, endHour, endMin]
     */
    public static int[] stringScheduleToIntSchedule(String sSchedule) {
        String[] indvTime  = sSchedule.split("-");
        String[] startTime = indvTime[0].split(":");
        int startHour      = Integer.parseInt(startTime[0]);
        int startMin       = Integer.parseInt(startTime[1]);
        String[] endTime   = indvTime[1].split(":");
        int endHour        = Integer.parseInt(endTime[0]);
        int endMin         = Integer.parseInt(endTime[1]);
        int[] intSchedule  = new int[]{startHour, startMin, endHour, endMin};
        return intSchedule;
    }

    /*
     * Debug code to print the loadsheddingscheduledata
     */
    public static void printLoadSheddingSchedule(Utilities.LoadSheddingScheduleData sD) {
        Utilities.Logd(Arrays.toString(new int[]{sD.mStartHour, sD.mStartMins, sD.mEndHour, sD.mEndMins}));
    }
}
