package com.colisa.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.colisa.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteUploader {
    private static final String TAG = NoteUploader.class.getSimpleName();
    private final Context mContext;
    private boolean mCanceled;

    public NoteUploader(Context context) {
        mContext = context;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public void cancel() {mCanceled = true;}

    public void doUpload(Uri dataUri) {
        String[] columns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        Cursor cursor = mContext.getContentResolver().query(dataUri, columns, null, null, null);
        assert cursor != null;
        int courseIdPos = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, ">>>*** UPLOAD START - " + dataUri + "  ***<<<" );
        mCanceled = false;
        while (!mCanceled && cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);

            if (!courseId.equals("")) {
                Log.i(TAG, ">>>Uploading Note<<< " + courseId + "|" + noteTitle + "|" + noteText);
                simulateLongRunningWork();
            }
        }
        if (mCanceled) {
            Log.i(TAG, ">>>*** UPLOAD CANCELLED - " + dataUri + "  ***<<<");
        } else {
            Log.i(TAG, ">>>*** UPLOAD END - " + dataUri + "  ***<<<" );
        }
        cursor.close();
    }

    private void simulateLongRunningWork() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
