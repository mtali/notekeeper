package com.colisa.notekeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NoteReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTE_ID = "com.colisa.notekeeper.NOTE_ID";
    public static final String EXTRA_NOTE_TITLE = "com.colisa.notekeeper.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.colisa.notekeeper.NOTE_TEXT";


    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);

        NoteReminderNotification.notify(context, noteText, noteTitle, noteId);
    }
}
