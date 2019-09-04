package com.colisa.notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
    public static final int POSITION_NOT_SET = -1;
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.colisa.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE_ID = "com.colisa.notekeeper.ORIGINAL_NOTE_TITLE_ID";
    public static final String ORIGINAL_NOTE_TEXT_ID = "com.colisa.notekeeper.ORIGINAL_NOTE_TEXT_ID";
    public static final String NOTE_POSITION = "con.colisa.notekeeper.NOTE_POSITION";
    private static final int LOAD_NOTE = 0;
    private static final int LOAD_COURSES = 1;

    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        readDisplayStateValues();
        if (null == savedInstanceState) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }


        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote) {
            getLoaderManager().initLoader(LOAD_NOTE, null, this);
        }
    }


    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE_ID);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT_ID);
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote) return;
        mOriginalCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }


    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourse(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
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
        mNoteId = intent.getIntExtra(NoteActivity.NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = (mNoteId == POSITION_NOT_SET);
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
        mNoteUri = getContentResolver().insert(Notes.CONTENT_URI, values);
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
        NoteReminderNotification.notify(getApplicationContext(), "Hello emmanuel", 0);
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
        mNote = DataManager.getInstance().getNotes().get(mNoteId);
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
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);

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

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {
        // Update note inside the database
        // mNoteUri has row url for the current selected note
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        getContentResolver().update(mNoteUri, values, null, null);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TEXT_ID, mOriginalNoteText);
        outState.putString(ORIGINAL_NOTE_TITLE_ID, mOriginalNoteTitle);
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
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
}
