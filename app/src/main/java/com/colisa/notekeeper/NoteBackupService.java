package com.colisa.notekeeper;

import android.app.IntentService;
import android.content.Intent;


public class NoteBackupService extends IntentService {
    public static final String TAG = NoteBackupService.class.getSimpleName();
    public static final String EXTRA_COURSE_ID = "com.colisa.notekeeper.extra.COURSE_ID";

    public NoteBackupService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupCourseId = intent.getStringExtra(EXTRA_COURSE_ID);
            NoteBackup.doBackup(this, backupCourseId);
        }
    }
}
