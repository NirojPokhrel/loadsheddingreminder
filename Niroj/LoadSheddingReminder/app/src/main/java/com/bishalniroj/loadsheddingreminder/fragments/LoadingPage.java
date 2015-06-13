package com.bishalniroj.loadsheddingreminder.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bishalniroj.loadsheddingreminder.R;

/**
 * Created by Niroj Pokhrel on 6/13/2015.
 */
public class LoadingPage extends Fragment{

    public LoadingPage() {

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_load_shedding, container, false);
        return rootView;
    }
}
