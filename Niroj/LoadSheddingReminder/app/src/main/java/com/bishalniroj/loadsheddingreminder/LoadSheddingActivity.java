package com.bishalniroj.loadsheddingreminder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;


public class LoadSheddingActivity extends ActionBarActivity {
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_load_shedding);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new LandingPageFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load_shedding, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class LandingPageFragment extends Fragment {
        private Button mBtnSelectArea, mBtnAdjustReminder, mBtnViewSchedule;

        public LandingPageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_landing_page, container, false);
            mBtnSelectArea = (Button) rootView.findViewById(R.id.selectArea);
            mBtnAdjustReminder = (Button) rootView.findViewById(R.id.adjustReminder);
            mBtnViewSchedule = (Button) rootView.findViewById(R.id.viewSchedule);

            mBtnViewSchedule.setOnClickListener(mOnClickListener);
            mBtnAdjustReminder.setOnClickListener(mOnClickListener);
            mBtnViewSchedule.setOnClickListener(mOnClickListener);

            return rootView;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
             public void onClick(View view) {
                switch(view.getId()) {
                    case R.id.selectArea:
                        Intent intent = new Intent( mContext, SelectArea.class );
                        startActivity(intent);
                        break;
                    case R.id.adjustReminder:
                        break;
                    case R.id.viewSchedule:
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
