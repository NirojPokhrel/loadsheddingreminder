/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bishalniroj.loadsheddingreminder;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bishalniroj.loadsheddingreminder.database.LoadSheddingScheduleDbHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
/* TODO: Design part has to be improved a lot. Please do that !!!
 */
public class TabbedViewScheduleActivity extends FragmentActivity {

    private static IDummyClass mDummyDataClass;
    private static Activity mActivity;
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    ViewPager mViewPager;

    //Database
    private static LoadSheddingScheduleDbHelper mScheduleDbHelper;

    //
    private SharedPreferences.Editor mPrefEditor;
    private static int mSelectedTab;

    public void onCreate(Bundle savedInstanceState) {
        int currentItem = -1;

        super.onCreate(savedInstanceState);
        mDummyDataClass = TestDataClass.GetInstanceOfClass();
        mActivity = this;
        setContentView(R.layout.activity_collection_demo);

        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());

        // Set up action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);

        //
        SharedPreferences sharedPref = getSharedPreferences(Utilities.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        mPrefEditor = sharedPref.edit();

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        currentItem = sharedPref.getInt(Utilities.SHARED_PREFERENCES_TAB_NUMBER, -1);
        Utilities.Logd("currentItem = "+currentItem);
        currentItem = currentItem == -1? 0:currentItem;
        mViewPager.setCurrentItem(currentItem);

        //DATABASE
        mScheduleDbHelper = LoadSheddingScheduleDbHelper.GetInstance(this, false);
        mScheduleDbHelper.open();
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

    @Override
    public void onStop() {
        mPrefEditor.putInt(Utilities.SHARED_PREFERENCES_TAB_NUMBER, mSelectedTab);
        mPrefEditor.commit();
        Utilities.Logd("shared onStop called with mSelectedTab "+ mSelectedTab);
        super.onStop();
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            args.putInt(DemoObjectFragment.ARG_OBJECT, i); // Our object is just an integer :-P
            fragment.setArguments(args);
            mSelectedTab = i;
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Area " + (position+1);
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DemoObjectFragment extends Fragment {

        public static final String ARG_OBJECT = "object";
        private ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>> mLoadSheddingList;
        private ArrayAdapter<ArrayList<Utilities.LoadSheddingScheduleData>> mListAdapter;
        private ListView mListView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
            Bundle args = getArguments();

          //  mLoadSheddingList = mDummyDataClass.GetLoadSheddingInfoForADay(args.getInt(ARG_OBJECT));
            mLoadSheddingList = new ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>>();
            int areaNum = args.getInt(ARG_OBJECT);
            //For 7 days in a week
            for( int i=0; i<7; i++ ) {
                mLoadSheddingList.add(mScheduleDbHelper.GetSchedDataForADay(areaNum, i));
            }
            //Get the data based upon singleton class for info storage
            //mLoadSheddingList = DataContainer.getData(areaNum);
            mListAdapter = new ScheduleInfoAdapter( mActivity, mLoadSheddingList);

            mListView = (ListView)rootView.findViewById(R.id.listOfScheduleForEachDay);
            mListView.setAdapter(mListAdapter);
            Calendar cal = Calendar.getInstance();
            int currentDay = cal.get(Calendar.DAY_OF_WEEK);
            mListView.setSelection(currentDay-1);
            //
            return rootView;
        }


        private class ScheduleInfoAdapter extends ArrayAdapter<ArrayList<Utilities.LoadSheddingScheduleData>> {
            private Context mContext;

            public ScheduleInfoAdapter(Context context, ArrayList<ArrayList<Utilities.LoadSheddingScheduleData>> objects) {
                super(context, 0, objects);
                mContext = context;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent ) {
                ArrayList<Utilities.LoadSheddingScheduleData> loadSheddingData = getItem(position);
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView =  inflater.inflate( R.layout.each_day_schedule_list, parent, false );
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_WEEK);
                if( day == (position+1) ) {
                    convertView.setBackgroundColor(Color.LTGRAY);
                }
                TextView tv = (TextView)convertView.findViewById(R.id.nameOfDay);
                switch( position ) {
                    case 0:
                        tv.setText("Sunday");
                        break;
                    case 1:
                        tv.setText("Monday");
                        break;
                    case 2:
                        tv.setText("Tuesday");
                        break;
                    case 3:
                        tv.setText("Wednesday");
                        break;
                    case 4:
                        tv.setText("Thursday");
                        break;
                    case 5:
                        tv.setText("Friday");
                        break;
                    case 6:
                        tv.setText("Saturday");
                        break;
                    default:
                        tv.setText("NONE");
                    break;
                }
                tv = (TextView)convertView.findViewById(R.id.daySchedule);
                tv.setText(Html.fromHtml(ConvertToHtmlString(loadSheddingData)));


                return convertView;
            }

        };
    }

    private static String ConvertToHtmlString( /*int pos*/ArrayList<Utilities.LoadSheddingScheduleData> loadSheddingList ) {
        String str = "";

        for( int i=0; i<loadSheddingList.size(); i++) {
            str += "<i>";
            str += loadSheddingList.get(i).mStartHour/10;
            str += loadSheddingList.get(i).mStartHour%10;
            str += ":";
            str += loadSheddingList.get(i).mStartMins/10;
            str += loadSheddingList.get(i).mStartMins%10;
            str += "-";
            str += loadSheddingList.get(i).mEndHour/10;
            str += loadSheddingList.get(i).mEndHour%10;
            str += ":";
            str += loadSheddingList.get(i).mEndMins/10;
            str += loadSheddingList.get(i).mEndMins%10;
            str += "</i>";
            /*str += "<i>"+loadSheddingList.get(i).mStartHour+":"+loadSheddingList.get(i).mStartMins+"-"+
                    loadSheddingList.get(i).mEndHour+":"+loadSheddingList.get(i).mEndMins+"</i>";*/
            if( i != (loadSheddingList.size() - 1) )
                str += "<br>";
        }

        return str;
    }
}
