package com.bishalniroj.loadsheddingreminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

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
            mScheduleData = null;
            Utilities.Logd("IO Exception Thrown!");
        }

        return mScheduleData;
    }

    public void onPostExecute(ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>> sData) {

        if( sData == null ) {
            Utilities.Logd("Data failed to update. Exiting the service.");
            return;
        }
        SharedPreferences sharedPref = mContext.getSharedPreferences(Utilities.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        boolean isFirstTime = sharedPref.getBoolean(Utilities.SHARED_PREFERENCES_FIRST_TIME, true);
        if( isFirstTime ) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Utilities.SHARED_PREFERENCES_FIRST_TIME, false);
            editor.commit();
            mScheduleDbHelper.fillDatabaseReal(sData);
        }
        else {
            //save check if the schedule has changed
            boolean scheduleEqual = isEqualSchedule(mScheduleDbHelper, sData);
            System.out.println("Schedule Changed: " + scheduleEqual);
            if (!scheduleEqual) {
                mScheduleDbHelper.fillDatabaseReal(sData);
                Utilities.sendNotifications(mContext,"LoadShedding Schedule Changed",
                 "Old reminders will be useless. Click to add new reminder for the loadshedding.",
                        false,true, ReminderForLoadShedding.class);
            }
        }
    };

    //check if existing schedule and newly parsed schedule differ
    public boolean isEqualSchedule(LoadSheddingScheduleDbHelper mScheduleDbHelper,
                                   ArrayList<ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>> newList) {

        //for each area
        for (int i=0; i<newList.size(); i++) {
            ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>> areaSched = newList.get(i);
            //for each day
            for(int j=0; j<areaSched.size(); j++) {
                ArrayList<Utilities.LoadSheddingScheduleData> areaDaySched = areaSched.get(j);
                //get the existing data for given area and day
                ArrayList<Utilities.LoadSheddingScheduleData> existingAreaDaySched =
                        mScheduleDbHelper.GetSchedDataForADay(i,j);

                if( (existingAreaDaySched.size() == 0) ||
                        (existingAreaDaySched.size() != areaDaySched.size()) ) {
                    //no data exists or unequal data exist so the equality must be false
                    return false;
                }
                else {
                    for (int k = 0; k < areaDaySched.size(); k++) {
                        Utilities.LoadSheddingScheduleData oldData = existingAreaDaySched.get(k);
                        Utilities.LoadSheddingScheduleData newData = areaDaySched.get(k);
                        if (!isEqualLoadSheddingSchedule(newData, oldData)) {
                            return false;
                        }

                    }
                }
            }
        }
        return true;
    }

    /*
    Returns true/false if two Utilities.LoadsheddingSchedule are equal or not
     */
    public boolean isEqualLoadSheddingSchedule (Utilities.LoadSheddingScheduleData newData,
                                                Utilities.LoadSheddingScheduleData oldData) {
        if ((newData.mStartHour == oldData.mStartHour) &&
                (newData.mStartMins == oldData.mStartMins) &&
                (newData.mEndHour == oldData.mEndHour) &&
                (newData.mEndMins == oldData.mEndMins)) {
            return true;
        }
        else {
            return false;
        }
    }

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

    private void sendNotifications() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("LoadShedding Schedule Changed")
                        .setContentText("Old reminders will be useless. Click to add new reminder for the loadshedding.")
                        .setAutoCancel(true);
		/*
		 * In case setAutoCancel(true) doesn't work use following line
		 * mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
		 */
        Intent resultIntent = new Intent( mContext, ReminderForLoadShedding.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(ReminderForLoadShedding.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notifyMgr = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.notify( 1, builder.build());
    }
}
