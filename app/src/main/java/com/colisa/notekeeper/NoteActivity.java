package com.colisa.notekeeper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.colisa.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.colisa.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.colisa.notekeeper.NoteKeeperProviderContract.Courses;
import com.colisa.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = NoteActivity.class.getSimpleName();
    public static final int NOTE_ID_NOT_SET = -1;
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.colisa.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE_ID = "com.colisa.notekeeper.ORIGINAL_NOTE_TITLE_ID";
    public static final String ORIGINAL_NOTE_TEXT_ID = "com.colisa.notekeeper.ORIGINAL_NOTE_TEXT_ID";
    public static final String NOTE_ID = "con.colisa.notekeeper.NOTE_ID";
    private static final String CURRENT_NOTE_URI = "com.colisa.notekeeper.CURRENT_COURSE_ID";
    private static final int LOAD_NOTE = 0;
    private static final int LOAD_COURSES = 1;

    //    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;
    private String mOriginalCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper mDbHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    private Uri mNoteUri;
    private boolean mNoteQueryFinished;
    private boolean mCourseQueryFinished;
    private CourseEventsReceiver mCourseEventsReceiver;
    private ModuleStatusView mViewModuleStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerCourseEventReceiver();

        mDbHelper = new NoteKeeperOpenHelper(this);
        mSpinnerCourses = findViewById(R.id.spinner_courses);

        // Creating SimpleCursor adapter and populate spinner
        mAdapterCourses = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1},
                0
        );
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);

        getLoaderManager().initLoader(LOAD_COURSES, null, this);


        if (null == savedInstanceState) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }
        readDisplayStateValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote) {
            getLoaderManager().initLoader(LOAD_NOTE, null, this);
        }

        mViewModuleStatus = findViewById(R.id.module_status_view);
        loadModuleStatusValues();

    }

    private void loadModuleStatusValues() {
        int totalNumberOfModules = 11;
        int completedModules = 7;
        boolean[] moduleStatus = new boolean[totalNumberOfModules];
        for(int i = 0; i < completedModules; i++) {
            moduleStatus[i] = true;
        }

        mViewModuleStatus.setModuleStatus(moduleStatus);
    }

    private void registerCourseEventReceiver() {
        mCourseEventsReceiver = new CourseEventsReceiver();
        IntentFilter intentFilter = new IntentFilter(CourseEventBroadcastHelper.ACTION_COURSE_EVENT);
        registerReceiver(mCourseEventsReceiver, intentFilter);
    }


    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mNoteUri = Uri.parse(savedInstanceState.getString(CURRENT_NOTE_URI));
        mOriginalCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE_ID);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT_ID);
    }

    private void saveOriginalNoteValues() {
//        if (mIsNewNote) return;
//        mOriginalCourseId = mNote.getCourse().getCourseId();
//        mOriginalNoteTitle = mNote.getTitle();
//        mOriginalNoteText = mNote.getText();
    }


    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourse(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing Note");
    }

    private int getIndexOfCourse(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        if (cursor != null) {
            int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
            int courseRowIndex = 0;
            boolean more = cursor.moveToFirst();
            while (more) {
                String cursorCourseId = cursor.getString(courseIdPos);
                if (cursorCourseId.equals(courseId))
                    break;
                courseRowIndex++;
                more = cursor.moveToNext();
            }
            return courseRowIndex;
        }
        return 0;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NoteActivity.NOTE_ID, NOTE_ID_NOT_SET);
        mIsNewNote = (mNoteId == NOTE_ID_NOT_SET);
        if (mIsNewNote) {
            createNewNote();
        }
        Log.i(TAG, "mNoteId: " + mNoteId);
    }

    private void createNewNote() {
        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        CreateNoteTask task = new CreateNoteTask(getContentResolver(), new CreateNoteTask.OnNoteCreatedListener() {
            @Override
            public void onNoteCreated(Uri noteUri) {
                mNoteUri = noteUri;
            }
        });
        task.execute(values);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        } else if (id == R.id.action_next) {
            moveNext();
        } else if (id == R.id.action_set_reminder) {
            showReminderNotification();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReminderNotification() {
        String noteText = mTextNoteText.getText().toString();
        String noteTitle = mTextNoteTitle.getText().toString();
        int rowId = (int) ContentUris.parseId(mNoteUri);

        Intent intent = new Intent(this, NoteReminderReceiver.class);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, rowId);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long currentTimeMilli = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60 * 60 * 1000;
        long TEN_SECONDS = 10 * 1000;
        long alarmTime = currentTimeMilli + TEN_SECONDS;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);
        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            Log.i(TAG, "Cancelling note at position: " + mNoteId);
            if (mIsNewNote) {
                deleteNoteFromDatabase();
            } else {
                storePreviousNoteValues();

            }
        } else {
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteNoteFromDatabase() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().delete(mNoteUri, null, null);
                return null;
            }
        };
        task.execute();
    }

    private void storePreviousNoteValues() {
//        CourseInfo course = DataManager.getInstance().getCourse(mOriginalCourseId);
//        mNote.setCourse(course);
//        mNote.setTitle(mOriginalNoteTitle);
//        mNote.setText(mOriginalNoteText);

    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {
        int position = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(position);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        return cursor.getString(courseIdPos);
    }

    @SuppressLint("StaticFieldLeak")
    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        // Update note inside the database
        // mNoteUri has row url for the current selected note
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        final ContentResolver resolver = getContentResolver();
        AsyncTask<ContentValues, Void, Void> task = new AsyncTask<ContentValues, Void, Void>() {
            @Override
            protected Void doInBackground(ContentValues... contentValues) {
                assert mNoteUri != null;
                resolver.update(mNoteUri, contentValues[0], null, null);
                return null;
            }
        };
        task.execute(values);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_NOTE_URI, mNoteUri.toString());
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TEXT_ID, mOriginalNoteText);
        outState.putString(ORIGINAL_NOTE_TITLE_ID, mOriginalNoteTitle);
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        unregisterReceiver(mCourseEventsReceiver);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == LOAD_NOTE)
            loader = createLoaderNote();
        else if (id == LOAD_COURSES)
            loader = createLoaderCourses();
        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCourseQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;
        String[] courseColumns = {
                Courses.COLUMN_COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };
        return new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);
    }

    private CursorLoader createLoaderNote() {
        mNoteQueryFinished = false;
        final String[] noteColumns = {
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == LOAD_NOTE)
            loadFinishedNote(data);
        else if (id == LOAD_COURSES) {
            mAdapterCourses.changeCursor(data);
            mCourseQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }

    }

    private void displayNoteWhenQueriesFinished() {
        if (mNoteQueryFinished && mCourseQueryFinished) {
            displayNote();
        }
    }

    private void loadFinishedNote(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToFirst();
        mNoteQueryFinished = true;
        displayNoteWhenQueriesFinished();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mNoteCursor != null && loader.getId() == LOAD_NOTE) {
            mNoteCursor.close();
        } else if (loader.getId() == LOAD_COURSES) {
            mAdapterCourses.changeCursor(null);
        }
    }

    private static class CreateNoteTask extends AsyncTask<ContentValues, Void, Uri> {
        private ContentResolver mResolver;
        private OnNoteCreatedListener mNoteCreatedListener;

        CreateNoteTask(ContentResolver resolver, OnNoteCreatedListener listener) {
            mResolver = resolver;
            mNoteCreatedListener = listener;
        }

        @Override
        protected Uri doInBackground(ContentValues... contentValues) {
            ContentValues values = contentValues[0];
            return mResolver.insert(Notes.CONTENT_URI, values);
        }


        @Override
        protected void onPostExecute(Uri uri) {
            mNoteCreatedListener.onNoteCreated(uri);
        }

        interface OnNoteCreatedListener {
            void onNoteCreated(Uri noteUri);
        }
    }
}
