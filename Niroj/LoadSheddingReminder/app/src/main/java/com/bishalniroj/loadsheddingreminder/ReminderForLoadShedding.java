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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.List;


public class ReminderForLoadShedding extends Activity {
    private static Activity mActivity;
    private Button mSetTimeBtn;
    private Spinner mSpinnerArea, mSpinnerDay, mSpinnerTime;
    private ArrayAdapter<CharSequence> mAreaAdapter, mDayAdapter, mTimeAdapter;
    private int mPositionArea, mPositionDay, mPositionTime;
    public static int mStoredArea, mStoredTime, mStoredDay;
    public static int mNHour, mNMins;
    private ArrayList<CharSequence> mListOfTime;
    private Button mButton;

    //List of reminder information
    public static class DummyDataForDisplay {
        public DummyDataForDisplay() {
            mStartHour = 0;
            mStartMins = 0;
            mEndHour = 0;
            mEndMins = 0;
            mAreaNum = 0;
            mDay = 0;
            mNHour = 0;
            mNMins = 0;
        }
        public DummyDataForDisplay(int startHour, int startMins, int endHour, int endMins,
                                   int areaNum, int day, int nHour, int nMins ) {
            mStartHour = startHour;
            mStartMins = startMins;
            mEndHour = endHour;
            mEndMins = endMins;
            mAreaNum = areaNum;
            mDay = day;
            mNHour = nHour;
            mNMins = nMins;
        }
        public int mStartHour;
        public int mStartMins;
        public int mEndHour;
        public int mEndMins;
        public int mAreaNum;
        public int mDay;
        public int mNHour;
        public int mNMins;
    };
    private static ArrayList<DummyDataForDisplay> mListOfReminder;
    private static ListView mLvOfReminder;
    private static ListOfReminderAdapter mReminderAdapter;


    public static final int FINAL_INT_REPEAT_NONE = -1;
    public static final int FINAL_INT_REPEAT_ONCE = 0;
    public static final int FINAL_INT_REPEAT_ALWAYS = 1;

    private static int mRepeatValue = FINAL_INT_REPEAT_NONE;

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
        mListOfTime = new ArrayList<CharSequence>();
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

        //List of Reminder
        mLvOfReminder = (ListView) findViewById(R.id.selectedReminders);
        //Todo: Get the stored list of reminder first
        //Following implementation is only for testing
        mListOfReminder = new ArrayList<DummyDataForDisplay>();
        mReminderAdapter = new ListOfReminderAdapter(this, mListOfReminder);
        mReminderAdapter.add(new DummyDataForDisplay());
        mReminderAdapter.add(new DummyDataForDisplay());
        mReminderAdapter.add(new DummyDataForDisplay());
        mReminderAdapter.add(new DummyDataForDisplay());
        mReminderAdapter.add(new DummyDataForDisplay());
        mLvOfReminder.setAdapter(mReminderAdapter);

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
                mTimeAdapter.add("03:00-05:45");
                mTimeAdapter.add("17:00-20:00");
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
                mSpinnerArea.setSelection(0);
                mSpinnerDay.setSelection(0);
                mSpinnerTime.setSelection(0);

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
            View convertView = (View) li.inflate(
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
    };

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        ReminderForLoadShedding mCustomTimePicker;

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
            String str = "You will get alarm at "+hourOfDay+" hours "+minute+" minutes"+
                    " before every load shedding";
            StoreData();
        }
    }

    private static void StoreData() {
        //Call apis to store the information
        mReminderAdapter.add(new DummyDataForDisplay());
    }

    //ArrayAdapter for list and it's storage
    public class ListOfReminderAdapter extends ArrayAdapter<DummyDataForDisplay> {

        public ListOfReminderAdapter(Context context, List<DummyDataForDisplay> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent ) {
            final int pos = position;
            if( convertView == null ) {
                LayoutInflater lp = LayoutInflater.from(mActivity);
                convertView = lp.inflate(R.layout.reminder_list_item, parent, false);
            }
            TextView tv = (TextView) convertView.findViewById(R.id.reminderInfo);
            String str = "Area 1 Sunday 04:00-05:45";
            str += "<br>";
            str += "0 hours 25 minutes";
            tv.setText(Html.fromHtml(str));
            Button btn = (Button) convertView.findViewById(R.id.deleteBtnImage);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mReminderAdapter.remove(getItem(pos));
                }
            });

            return convertView;
        }
    }
}
