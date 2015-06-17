package com.bishalniroj.loadsheddingreminder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class SelectArea extends Activity {
    private IDummyClass mDummyClass;
    private int mNumberOfAreas;
    private String[] mAreasName;
    private RadioGroup mRadioGroup;
    private int mSelectedArea;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_select_area);
        mDummyClass = TestDataClass.GetInstanceOfClass();
        mNumberOfAreas = mDummyClass.GetNumberOfAreas();
        mAreasName = mDummyClass.GetAreas();

        mRadioGroup = (RadioGroup) findViewById(R.id.selectAreaGroup);
        for( int i=0; i<mNumberOfAreas; i++ ) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            radioButton.setId(i + 1);
            radioButton.setOnClickListener(mOnClickListener);
            radioButton.setText(mAreasName[i]);
            radioButton.setTextColor(Color.BLACK);
            radioButton.setAlpha((float)1.0);
            mRadioGroup.addView(radioButton, i);
        }

        Button doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(mDoneBtnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSelectedArea = v.getId();
        }
    };

    private View.OnClickListener mDoneBtnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            Intent data = getIntent();// = new Intent(mActivity, LoadSheddingActivity.class);
            data.putExtra(Utilities.INTENT_DATA_AREA_NUMBER, mSelectedArea);
            mActivity.setResult(RESULT_OK, data);
/*            if (getParent() == null) {
                mActivity.setResult(RESULT_OK, data);
            } else {
                mActivity.getParent().setResult(RESULT_OK, data);
            }*/
            mActivity.finish();
        }
    };
}
