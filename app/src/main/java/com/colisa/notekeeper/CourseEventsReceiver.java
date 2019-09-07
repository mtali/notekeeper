package com.colisa.notekeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.colisa.notekeeper.CourseEventBroadcastHelper.*;

public class CourseEventsReceiver extends BroadcastReceiver {

    private static final String TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_COURSE_EVENT.equals(intent.getAction())) {
            String courseId = intent.getStringExtra(EXTRA_COURSE_ID);
            String message = intent.getStringExtra(EXTRA_COURSE_MESSAGE);
            Log.i(TAG, "Received broadcast: " + message + " for course: " + courseId);
        }
    }
}
