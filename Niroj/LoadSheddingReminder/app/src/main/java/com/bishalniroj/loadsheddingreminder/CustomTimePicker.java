package com.bishalniroj.loadsheddingreminder;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;


public class CustomTimePicker extends Activity {
    private Activity mActivity;
    private static TextView mTimerInfoDisplay;
    private Button mSetTimeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_time_picker);
        mTimerInfoDisplay = (TextView)findViewById(R.id.timerInfo);
        mSetTimeBtn = (Button) findViewById(R.id.setTime);
        mSetTimeBtn.setOnClickListener(mSetClickListener);
    }

    private View.OnClickListener mSetClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getFragmentManager(), "timePicker");
        }
    };

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        CustomTimePicker mCustomTimePicker;

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
            String str = "You will get alarm at "+hourOfDay+" hours"+" "+minute+" minutes"+
                    " before every load shedding";
            mTimerInfoDisplay.setText(str);
        }
    }
}
