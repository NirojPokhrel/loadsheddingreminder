package com.bishalniroj.loadsheddingreminder;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.LinearLayout;


public class ViewScheduleActivity extends
        FragmentActivity {

    private static Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_schedule);
        mActivity = this;
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SelectAreaFragment(), "Select Area")
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SelectAreaFragment extends Fragment {
        IDummyClass mTestClass;
        public SelectAreaFragment() {
            mTestClass = TestDataClass.GetInstanceOfClass();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_select_area, container, false);

            LinearLayout llayout = (LinearLayout) rootView.findViewById(R.id.selectAreaForSchedule);
            String strAreas[];
            strAreas = mTestClass.GetAreas();
            for( int i=0; i<mTestClass.GetNumberOfAreas(); i++ ) {
                Button btn = new Button(mActivity);
                btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ));
                btn.setOnClickListener(mClickListener);
                btn.setText(strAreas[i]);
                btn.setId(i+1);
                llayout.addView(btn, i);
            }
            return rootView;
        }

        private View.OnClickListener mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //Launch an activity to show the schedule !!!
                Intent intent = new Intent( mActivity, TabbedViewScheduleActivity.class);
                startActivity(intent);
            }
        };
    }

    public static class DisplayScheduleFragment extends Fragment {
        public DisplayScheduleFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_select_area, container, false);


            return rootView;
        }
    }
}
