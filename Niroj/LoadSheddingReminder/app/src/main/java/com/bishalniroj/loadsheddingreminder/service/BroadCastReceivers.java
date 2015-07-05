package com.bishalniroj.loadsheddingreminder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bishalniroj.loadsheddingreminder.Utilities;

public class BroadCastReceivers extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if( intent.getAction().equals(Utilities.LOADSHEDDING_BROADCAST_RECEIVER_ACTION)) {
            Utilities.Logd("Database update Alarm successfully received");
            Intent intentNew = new Intent ( context, LoadSheddingService.class);
            context.startService(intentNew);
        }

        if( intent.getAction().equals(Utilities.REMINDER_BROADCAST_RECEIVER_ACTION)) {
            Utilities.Logd("Reminder Alarm successfully received");
            //DO the action for reminder
            onReminderReceiveAction(context);
        }
    }

    /*
    Action done as reminder. Options, Simple Toast, A Notification in bar, Vibrate, Sound, User-def
    ined functionality
     */
    public void onReminderReceiveAction(Context context) {
        //Raise a toast as reminder
        Utilities.showToast(context,"REMINDER.YOU SET IT! DONT BLAME ME");
    }



}
