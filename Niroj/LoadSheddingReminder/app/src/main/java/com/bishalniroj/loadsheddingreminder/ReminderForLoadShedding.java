package com.bishalniroj.loadsheddingreminder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bishalniroj.loadsheddingreminder.database.LoadSheddingReminderListTable;
import com.bishalniroj.loadsheddingreminder.database.LoadSheddingScheduleDbHelper;
import com.bishalniroj.loadsheddingreminder.service.BroadCastReceivers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ReminderForLoadShedding extends Activity {
    private static Activity mActivity;
    private static Spinner mSpinnerArea, mSpinnerDay, mSpinnerTime;
    private ArrayAdapter<CharSequence> mAreaAdapter, mDayAdapter, mTimeAdapter;
    private int mPositionArea, mPositionDay, mPositionTime;
    public static int mStoredArea, mStoredTime, mStoredDay;
    public static int mNHour, mNMins;
    private ArrayList<CharSequence> mListOfTime;
    private Button mButton;

    protected static List<Utilities.LoadSheddingReminderData> mListOfReminder;
    private static ListView mLvOfReminder;
    private static ListOfReminderAdapter mReminderAdapter;

    public static final int FINAL_INT_REPEAT_NONE = -1;
    public static final int FINAL_INT_REPEAT_ONCE = 0;
    public static final int FINAL_INT_REPEAT_ALWAYS = 1;

    private static int mRepeatValue = FINAL_INT_REPEAT_NONE;

    //Database
    public static LoadSheddingReminderListTable mReminderListDbTable;
    private static LoadSheddingScheduleDbHelper mSchduleDbHelper;
    private static boolean mIsReminderDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_reminder_loadshedding);


        // Set up action bar.
        final ActionBar actionBar = getActionBar();
        // Specify that the Home button should show an "Up" caret, indicating that touching the
        actionBar.setDisplayHomeAsUpEnabled(true);

        mSpinnerArea = (Spinner) findViewById(R.id.selectAreaSpinner);
        mAreaAdapter = ArrayAdapter.createFromResource(this,
                R.array.area_arrays, android.R.layout.simple_spinner_item);
        mAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerArea.setAdapter(mAreaAdapter);
        mSpinnerArea.setOnItemSelectedListener(mAreaSelectedListener);

        mSpinnerDay = (Spinner) findViewById(R.id.selectDaySpinner);
        mDayAdapter = ArrayAdapter.createFromResource(this,
                R.array.day_arrays, android.R.layout.simple_spinner_item);
        mDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDay.setAdapter(mDayAdapter);
        mSpinnerDay.setOnItemSelectedListener(mDaySelectedListener);

        mSpinnerTime = (Spinner) findViewById(R.id.selectTimeSpinner);
        mListOfTime = new ArrayList<>();
        mListOfTime.add("Select Time");
        mTimeAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item,
                mListOfTime );
        mSpinnerTime.setAdapter(mTimeAdapter);
        mSpinnerTime.setOnItemSelectedListener(mTimeSelectedListener);

        mPositionArea = -1;
        mPositionDay = -1;
        mPositionTime = -1;

        //Button to store the timings.
        mButton = (Button) findViewById(R.id.setTime);
        mButton.setOnClickListener(mDoneBtnListener);

        //DATABASE
        mReminderListDbTable = new LoadSheddingReminderListTable(this);
        mReminderListDbTable.open();
        mSchduleDbHelper = LoadSheddingScheduleDbHelper.GetInstance(this, false);
        mSchduleDbHelper.open();

        //List of Reminder
        mLvOfReminder = (ListView) findViewById(R.id.selectedReminders);
        mListOfReminder = new ArrayList<>();
        mListOfReminder.addAll(mReminderListDbTable.getAllReminders());
        Collections.sort(mListOfReminder);
        mReminderAdapter = new ListOfReminderAdapter(this, mListOfReminder);
        mLvOfReminder.setAdapter(mReminderAdapter);
    }

    @Override
    public void onDestroy() {
        if( mReminderListDbTable != null )
            mReminderListDbTable.close();
        if( mSchduleDbHelper != null )
            mSchduleDbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, LoadSheddingActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private AdapterView.OnItemSelectedListener mAreaSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mPositionArea = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mDaySelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mPositionDay = position;
            if( mPositionArea > 0 && mPositionDay > 0 ) {
                //Get Apis for the selected day and area
                mTimeAdapter.clear();
                mTimeAdapter.add("Select Time");
                ArrayList<Utilities.LoadSheddingScheduleData> dailyData;

                dailyData =  mSchduleDbHelper.GetSchedDataForADay( mPositionArea-1, mPositionDay-1);


                for( int i=0; i<dailyData.size(); i++ ) {
                    String str = "";

                    str += dailyData.get(i).mStartHour/10;
                    str += dailyData.get(i).mStartHour%10;
                    str += ":";
                    str += dailyData.get(i).mStartMins/10;
                    str += dailyData.get(i).mStartMins%10;
                    str += "-";
                    str += dailyData.get(i).mEndHour/10;
                    str += dailyData.get(i).mEndHour%10;
                    str += ":";
                    str += dailyData.get(i).mEndMins/10;
                    str += dailyData.get(i).mEndMins%10;

                    mTimeAdapter.add(str);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemSelectedListener mTimeSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mPositionTime = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private View.OnClickListener mDoneBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if( mPositionArea > 0 && mPositionTime > 0 && mPositionDay > 0 ) {
                //To do:
                //First check if this time is already registered for reminder or not ??
                DialogFragment newFragment = new SelectTimeDialog();
                newFragment.show(getFragmentManager(), "selectTime");
                //Store the area ??
                mStoredArea = mPositionArea;
                mStoredTime = mPositionTime;
                mStoredDay = mPositionDay;

            } else {
                Utilities.showToast( mActivity, "Please select proper Area, Time and Day");
            }
        }
    };

    public static class SelectTimeDialog extends DialogFragment implements
            DialogInterface.OnCancelListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle("Total Sum");
            LayoutInflater li = LayoutInflater.from(mActivity);
            View convertView = li.inflate(
                    R.layout.fragment_time_options, null);
            RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.idOnce);
            radioButton.setOnClickListener(mTimeOptionsClickListener);
            radioButton = (RadioButton) convertView.findViewById(R.id.idRepeat);
            radioButton.setOnClickListener(mTimeOptionsClickListener);

            builder.setView(convertView);
            builder.setTitle("Frequency of alarms");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if( mRepeatValue != FINAL_INT_REPEAT_NONE ) {
                        DialogFragment newFragment = new TimePickerFragment();
                        newFragment.show(getFragmentManager(), "timePicker");
                    }
                }
            });

            return builder.create();
        }
        private View.OnClickListener mTimeOptionsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.idOnce:
                        mRepeatValue = FINAL_INT_REPEAT_ONCE;
                        break;
                    case R.id.idRepeat:
                        mRepeatValue = FINAL_INT_REPEAT_ALWAYS;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        public TimePickerFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
/*            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);*/
            int hour = 0;
            int minute = 0;

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mNHour = hourOfDay;
            mNMins = minute;
            if( !mIsReminderDialog && mStoredDay >= 0 && mStoredTime >= 0 && mStoredArea >= 0 ) {
                for( int i=0; i<mListOfReminder.size(); i++ ) {
                    Utilities.Logd("Inside with i= "+i+" size = " + mListOfReminder.size());
                    Utilities.LoadSheddingReminderData reminderData = mListOfReminder.get(i);

                    if( reminderData.mDay == mStoredDay && reminderData.mAreaNum == mStoredArea ) {
                        Utilities.Logd("mStoredArea = "+mStoredArea+" mStoredDay= " + mStoredDay+" mStoredTime= "+mStoredTime);
                        Utilities.LoadSheddingScheduleData schedData = GetLoadSheddingInfo( mStoredArea, mStoredDay, mStoredTime );
                        if( reminderData.mLoadsheddingInfo.mStartHour == schedData.mStartHour &&
                                reminderData.mLoadsheddingInfo.mStartMins == schedData.mStartMins ) {

                            if( reminderData.mHourBefore == mNHour && reminderData.mMinsBefore == mNMins ) {
                                Utilities.showToast(mActivity,"You have already set the alarm for this time.");
                            } else {
                                DialogFragment newFragment = new ReminderRepetitionDialog();
                                newFragment.show(getFragmentManager(), "RepetitionDialog");
                                mIsReminderDialog = true;
                            }
                            //Call dialog fragment
                            return;
                        }
                    }
                }
                Utilities.Logd("Before StoredData in onTimeSet()");
                StoreData();
            }
        }
    }

    private static void StoreData() {
        //Call apis to store the information
        Utilities.LoadSheddingReminderData reminderData = new Utilities.LoadSheddingReminderData();
        reminderData.mID = getID();
        reminderData.mAreaNum = mStoredArea;
        reminderData.mDay = mStoredDay;
        reminderData.mReminderFrequency = mRepeatValue;
        //In database it is stored from 0 to n-1 for area and 0 t0 6 for days.
        reminderData.mLoadsheddingInfo = GetLoadSheddingInfo( reminderData.mAreaNum, reminderData.mDay,
                mStoredTime);
        reminderData.mHourBefore = mNHour;
        reminderData.mMinsBefore = mNMins;
        Calendar cal = Calendar.getInstance();
        reminderData.mDate = "" + cal.get(Calendar.DATE);
        mReminderListDbTable.insertReminder(reminderData);

        mReminderAdapter.add(reminderData);
        mStoredArea = -1;
        mStoredDay = -1;
        mStoredTime = -1;
        mNHour = -1;
        mNMins = -1;
        mSpinnerArea.setSelection(0);
        mSpinnerDay.setSelection(0);
        mSpinnerTime.setSelection(0);

        //set the alarm
        setReminderAlarm(cal, reminderData);

    }

    //set alarm with specified request code
    //http://stackoverflow.com/questions/19441679/create-multiple-alarmmanager-for-the-broadcast-receiver
    private static void setReminderAlarm(Calendar calendar, Utilities.LoadSheddingReminderData reminderData) {
        int reqCode = reminderData.mID;

        Intent myIntent = new Intent(mActivity, BroadCastReceivers.class);
        myIntent.putExtra("Before Hours", reminderData.mHourBefore);
        myIntent.putExtra("Before Mins", reminderData.mMinsBefore);
        myIntent.setAction(Utilities.REMINDER_BROADCAST_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity,
                reqCode, myIntent, 0);
        AlarmManager manager = (AlarmManager) mActivity
                .getSystemService(Context.ALARM_SERVICE);

        //get the day for which this loadshedding reminder as requested to be scheduled
        int mDay = reminderData.mDay; //1-7 for Sunday-Saturday

        //Get current day, hour and minute to know when to schedule the
        //alarm
        int currentDay  = calendar.get(Calendar.DAY_OF_WEEK); //1-7 for Sunday-Saturday
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY); //24 hour clock
        int currentMin  = calendar.get(Calendar.MINUTE);

        //get the associated loadshedding info
        Utilities.LoadSheddingScheduleData scheduleData = reminderData.mLoadsheddingInfo;
        //Get the start time of the loadshedding against which this reminder was requested
        int startHour = scheduleData.mStartHour;
        int startMin  = scheduleData.mStartMins;

        //get the time before which the loadshedding reminder has to be delivered
        int mHoursBefore = reminderData.mHourBefore;
        int mMinsBefore  = reminderData.mMinsBefore;

        //delta between Sunday 00:00AM till current time in minutes
        int deltaCurrent = (currentDay - 1)*24*60 +
                (currentHour)*60 + currentMin;
        Utilities.Logd("CurrentDay:"+currentDay+","+currentHour+","+currentMin);
        Utilities.Logd("deltaCurrent="+deltaCurrent);
        //delta between Sunday 00:00Am till the loadsheddingschedule data
        //against which this reminder was stored
        int deltaLoadSheddingSchedule = ((mDay) -1)*24*60 +
                (startHour)*60 + startMin;
        Utilities.Logd("LoadSheddingDay:"+mDay+","+startHour+","+startMin);
        Utilities.Logd("deltaLoadSheddingSchedule="+deltaLoadSheddingSchedule);
        //calculate the interval from now until the loadshedding time
        int deltaNowToLoadSheddingSchedule = (deltaLoadSheddingSchedule -
                deltaCurrent);
        //if the loadshedding schedule is in different week add the total
        //minutes of the week to get it positive
        if(deltaNowToLoadSheddingSchedule<0) {
            deltaNowToLoadSheddingSchedule += 10080;
        }

        //Now find the actual time until next reminder in minutes
        deltaNowToLoadSheddingSchedule -= ((mHoursBefore)*60 + mMinsBefore);
        //further check if it is negative move it until next week
        if(deltaNowToLoadSheddingSchedule<0) {
            deltaNowToLoadSheddingSchedule += 10080;
        }

        //check the reminder request and accordingly set onetime/recurring alarms
        if(reminderData.mReminderFrequency == FINAL_INT_REPEAT_ONCE) {
            Utilities.Logd("Setting Reminder for min from now:" + deltaNowToLoadSheddingSchedule);
            manager.set(AlarmManager.RTC,
                    calendar.getTimeInMillis()+deltaNowToLoadSheddingSchedule*60*1000,
                    pendingIntent);
        }
        else {
            manager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis()+deltaNowToLoadSheddingSchedule*60*1000,
                    AlarmManager.INTERVAL_DAY*7, pendingIntent);
        }
    }

    //Set the alarm directly using android's api
    private static void setDirectReminderAlarm(Calendar calendar,
                                               Utilities.LoadSheddingReminderData reminderData) {
        int reqCode = reminderData.mID;

        //get the day for which this loadshedding reminder as requested to be scheduled
        int mDay = reminderData.mDay; //1-7 for Sunday-Saturday

        //get the associated loadshedding info
        Utilities.LoadSheddingScheduleData scheduleData = reminderData.mLoadsheddingInfo;
        //Get the start time of the loadshedding against which this reminder was requested
        int startHour = scheduleData.mStartHour;
        int startMin  = scheduleData.mStartMins;

        //get the time before which the loadshedding reminder has to be delivered
        int mHoursBefore = reminderData.mHourBefore;
        int mMinsBefore  = reminderData.mMinsBefore;

        //find the absolute time when the alarm has to be set for
        int absTime = ( startHour*60 + startMin ) - (mHoursBefore*60 + mMinsBefore);
        if(absTime < 0) {
            absTime += 24*60;
            mDay = mDay - 1;
            //Due to subtraction Sunday should roll back to Saturday
            if(mDay == 0) {
                mDay = 7;
            }
        }
        int absHour = absTime/60;
        int absMin  = absTime%60;

        //Message to be displayed when the alarm goes off
        String alarmMessage = "Reminder!!. There will be a power cut in " + mHoursBefore
                + " hours and " + mMinsBefore + " minutes.";

        //check the reminder request and accordingly set onetime/recurring alarms
        if(reminderData.mReminderFrequency == FINAL_INT_REPEAT_ONCE) {
            Intent setNewAlarm = new Intent(AlarmClock.ACTION_SET_ALARM);
            setNewAlarm.putExtra(AlarmClock.EXTRA_HOUR, absHour);
            setNewAlarm.putExtra(AlarmClock.EXTRA_MINUTES, absMin);
            //TODO How to enforce it go only once
            setNewAlarm.putExtra(AlarmClock.EXTRA_DAYS, mDay);
            setNewAlarm.putExtra(AlarmClock.EXTRA_MESSAGE,alarmMessage );
            mActivity.startActivity(setNewAlarm);
        }
        else {
            Intent setNewAlarm = new Intent(AlarmClock.ACTION_SET_ALARM);
            setNewAlarm.putExtra(AlarmClock.EXTRA_HOUR, absHour);
            setNewAlarm.putExtra(AlarmClock.EXTRA_MINUTES, absMin);
            setNewAlarm.putExtra(AlarmClock.EXTRA_DAYS, mDay);
            setNewAlarm.putExtra(AlarmClock.EXTRA_MESSAGE,alarmMessage );
            mActivity.startActivity(setNewAlarm);
        }
    }



    public static int getID() {
        Calendar cal = Calendar.getInstance();

        return (int)cal.getTimeInMillis();
    }

    public static Utilities.LoadSheddingScheduleData GetLoadSheddingInfo(int areaNum, int day, int positionTime) {
        return mSchduleDbHelper.GetSchedDataForADay(areaNum-1,day-1).get(positionTime-1);
    }
    //ArrayAdapter for list and it's storage
    public class ListOfReminderAdapter extends ArrayAdapter<Utilities.LoadSheddingReminderData> {

        public ListOfReminderAdapter(Context context, List<Utilities.LoadSheddingReminderData> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent ) {
            final int pos = position;
            final Utilities.LoadSheddingReminderData reminderData = getItem(position);
            if( convertView == null ) {
                LayoutInflater lp = LayoutInflater.from(mActivity);
                convertView = lp.inflate(R.layout.reminder_list_item, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(R.id.reminderInfo);
            String str = "Area " + reminderData.mAreaNum + " ";
            switch(reminderData.mDay) {
                case Utilities.SUNDAY:
                    str += " Sunday ";
                    break;
                case Utilities.MONDAY:
                    str += " Monday ";
                    break;
                case Utilities.TUESDAY:
                    str += " Tuesday ";
                    break;
                case Utilities.WEDNESDAY:
                    str += " Wednesday ";
                    break;
                case Utilities.THURSDAY:
                    str += " Thursday ";
                    break;
                case Utilities.FRIDAY:
                    str += " Friday ";
                    break;
                case Utilities.SATURDAY:
                    str += " Saturday ";
                    break;
                default:
                    str += " NONE ";
            }
            str += reminderData.mLoadsheddingInfo.mStartHour/10;
            str += reminderData.mLoadsheddingInfo.mStartHour%10;
            str += ":";
            str += reminderData.mLoadsheddingInfo.mStartMins/10;
            str += reminderData.mLoadsheddingInfo.mStartMins%10;
            str += "-";
            str += reminderData.mLoadsheddingInfo.mEndHour/10;
            str += reminderData.mLoadsheddingInfo.mEndHour%10;
            str += ":";
            str += reminderData.mLoadsheddingInfo.mEndMins/10;
            str += reminderData.mLoadsheddingInfo.mEndMins%10;
            str += "<br>";
            str += reminderData.mHourBefore+" hours " + reminderData.mMinsBefore + " minutes";
            tv.setText(Html.fromHtml(str));
            Button btn = (Button) convertView.findViewById(R.id.deleteBtnImage);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mId of the reminder
                    int mID = getItem(pos).mID;
                    mReminderListDbTable.removeRow(mID);
                    mReminderAdapter.remove(getItem(pos));
                    //remove any associated alarm
                    Intent intent = new Intent(mActivity, BroadCastReceivers.class);
                    PendingIntent pi = PendingIntent.getBroadcast(mActivity, mID, intent, 0);
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    am.cancel(pi);
                }
            });

            return convertView;
        }
    }



    public static class ReminderRepetitionDialog extends DialogFragment implements DialogInterface.OnCancelListener {

        public ReminderRepetitionDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
            dialogBuilder.setTitle("Set Reminder?");
            dialogBuilder.setMessage("You already have set reminder for this load shedding schedule. Do you want to save it again?");
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StoreData();
                    mIsReminderDialog = false;
                }
            });
            dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });

            return dialogBuilder.create();
        }
    }
    //TODO: Call apis to set alarm at a particular time


}




























