package com.bishalniroj.loadsheddingreminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//TODO: put all the database operations in separate thread
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_reminder_loadshedding);

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
        mSchduleDbHelper = new LoadSheddingScheduleDbHelper(this);
        mSchduleDbHelper.open();

        //List of Reminder
        mLvOfReminder = (ListView) findViewById(R.id.selectedReminders);
        mListOfReminder = new ArrayList<>();
        mListOfReminder.addAll(mReminderListDbTable.getAllReminders());
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
                //TODO: Get the schedules for proper area and day
                ArrayList<Utilities.LoadSheddingScheduleData> dailyData;

                dailyData =  mSchduleDbHelper.GetSchedDataForADay( mPositionArea, mPositionDay);


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

              //  mTimeAdapter.add("03:00-05:45");
              //  mTimeAdapter.add("17:00-20:00");
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
                    Utilities.Logd("onClick for setPositiveButton");
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
                Utilities.Logd("onClick for radioGroup");
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
            if( mStoredDay >= 0 && mStoredTime >= 0 && mStoredArea >= 0 ) {
                //TODO: Check if this timing has already been stored
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
        //TODO: Proper channel to get the load shedding info
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
    }

    public static int getID() {
        Calendar cal = Calendar.getInstance();

        return (int)cal.getTimeInMillis();
    }

    public static Utilities.LoadSheddingScheduleData GetLoadSheddingInfo(int areaNum, int day, int positionTime) {

        return mSchduleDbHelper.GetSchedDataForADay(areaNum,day).get(positionTime-1);
/*        Utilities.LoadSheddingScheduleData scheduleData = new Utilities.LoadSheddingScheduleData();
        if( positionTime == 1 ) {
            scheduleData.mStartHour = 3;
            scheduleData.mStartMins = 0;
            scheduleData.mEndHour = 5;
            scheduleData.mEndMins = 45;
        } else if( positionTime == 2 ){
            scheduleData.mStartHour = 17;
            scheduleData.mStartMins = 0;
            scheduleData.mEndHour = 20;
            scheduleData.mEndMins = 0;
        }

        return scheduleData;*/
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
                    mReminderListDbTable.removeRow(getItem(pos).mID);
                    mReminderAdapter.remove(getItem(pos));
                }
            });

            return convertView;
        }
    }
    //TODO: Call apis to set alarm at the particular time
}
