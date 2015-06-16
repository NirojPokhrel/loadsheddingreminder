package com.bishalniroj.loadsheddingreminder;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class SelectArea extends ActionBarActivity {
    private IDummyClass mDummyClass;
    private int mNumberOfAreas;
    private String[] mAreasName;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_area);
        mDummyClass = TestDataClass.GetInstanceOfClass();
        mNumberOfAreas = mDummyClass.GetNumberOfAreas();
        mAreasName = mDummyClass.GetAreas();

        mRadioGroup = (RadioGroup) findViewById(R.id.selectAreaGroup);
        for( int i=0; i<mNumberOfAreas; i++ ) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT ));
            radioButton.setId(i+1);
            radioButton.setOnClickListener(mOnClickListener);
            radioButton.setText(mAreasName[i]);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utilities.Logd("Selected Area: " + v.getId());
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_area, menu);
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
}
