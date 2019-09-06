package com.colisa.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.colisa.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteBackup {
    public static final String ALL_COURSES = "ALL_COURSES";
    private static final String TAG = NoteBackup.class.getSimpleName();

    public static void doBackup(Context context, String backupCourseId) {
        String[] columns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        String selection = null;
        String[] selectionArgs = null;
        if (!backupCourseId.equals(ALL_COURSES)) {
            selection = Notes._ID + " = ?";
            selectionArgs = new String[]{backupCourseId};
        }

        Cursor cursor = context.getContentResolver().query(Notes.CONTENT_URI, columns, selection, selectionArgs, null);
        assert cursor != null;
        int courseIdPos = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int courseTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int courseTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);
        Log.i(TAG, ">>>***   BACKUP START - Thread: " + Thread.currentThread().getId() + "   ***<<<");
        while (cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(courseTitlePos);
            String noteText = cursor.getString(courseTextPos);
            if (!noteTitle.equals("")) {
                Log.i(TAG, ">>>Backing Up Note<<<" + courseId + "|" + noteTitle + "|" + noteText);
                simulateLongRunningWork();
            }
        }
        Log.i(TAG, ">>>***   BACKUP COMPLETE    ***<<<");
        cursor.close();
    }

    private static void simulateLongRunningWork() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
