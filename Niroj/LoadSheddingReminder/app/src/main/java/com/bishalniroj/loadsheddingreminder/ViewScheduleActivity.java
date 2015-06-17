package com.bishalniroj.loadsheddingreminder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.LinearLayout;


public class ViewScheduleActivity extends
        Activity {

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
    };
}
