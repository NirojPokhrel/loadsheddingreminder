package com.bishalniroj.loadsheddingreminder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bishalniroj.loadsheddingreminder.Utilities;

public class BroadCastReceivers extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if( intent.getAction().equals(Utilities.LOADSHEDDING_BROADCAST_RECEIVER_ACTION)) {
            Utilities.Logd("Alarm successfully received");
            Intent intentNew = new Intent ( context, LoadSheddingService.class);
            context.startService(intentNew);
        }

    }

}
