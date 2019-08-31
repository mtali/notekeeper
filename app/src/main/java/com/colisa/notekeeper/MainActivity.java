package com.colisa.notekeeper;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.colisa.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static com.colisa.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class MainActivity extends AppCompatActivity implements DrawerLayout.DrawerListener, NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private AppBarConfiguration mAppBarConfiguration;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String NOTE_POSITION = "com.colisa.notekeeper.NOTE_POSITION";
    private static final int NOTES_LOADER = 3;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private NoteKeeperOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        // Setting toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setting floating button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
            }
        });

        // Setting navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(this);
        toggle.syncState();


        // Navigation view
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        initializeDisplayContent();

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
    }

    private void initializeDisplayContent() {
        DataManager.loadFromDatabase(mDbOpenHelper);
        mRecyclerItems = findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.course_grid_span));

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

        displayNotes();
    }

    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);
        selectNavigationItem(R.id.nav_notes);
    }

    private void selectNavigationItem(final int id) {
        final NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                navigationView.getMenu().findItem(id)
                        .setChecked(true);
            }
        });
    }

    private void displayCourse() {
        mRecyclerItems.setLayoutManager(mCoursesLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);
        selectNavigationItem(R.id.nav_courses);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(NOTES_LOADER, null, this);
        updateNavigationHeader();
    }


    private void updateNavigationHeader() {
        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.text_user_name);
        TextView textEmail = headerView.findViewById(R.id.text_email_address);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = pref.getString(getString(R.string.pref_key_user_display_name), "");
        String email = pref.getString(getString(R.string.pref_key_email_address), "");

        textEmail.setText(email);
        textUserName.setText(userName);
    }

    @Override
    public void onDrawerSlide(@NonNull View view, float v) {

    }

    @Override
    public void onDrawerOpened(@NonNull View view) {

    }

    @Override
    public void onDrawerClosed(@NonNull View view) {

    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        final int id = menuItem.getItemId();
        if (id == R.id.nav_courses) {
            displayCourse();
        } else if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_send) {
            handleSelection(R.string.nav_share_message);
        } else if (id == R.id.nav_share) {
            handleShare();
        }
        DrawerLayout layout = findViewById(R.id.drawer_layout);
        layout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {
        View view = findViewById(R.id.list_items);
        String social = PreferenceManager.getDefaultSharedPreferences(this).getString(
                getString(R.string.pref_key_list_social), ""
        );
        Snackbar.make(view, "Share to - " + social, Snackbar.LENGTH_SHORT).show();
    }

    private void handleSelection(int id) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, id, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == NOTES_LOADER) {
            loader = new CoursesCursorLoader(this, mDbOpenHelper);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        if (id == NOTES_LOADER) {
            mNoteRecyclerAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        if (id == NOTES_LOADER)
            mNoteRecyclerAdapter.changeCursor(null);
    }

    private static class CoursesCursorLoader extends CursorLoader {
        private final NoteKeeperOpenHelper mOpenHelper;

        CoursesCursorLoader(@NonNull Context context, NoteKeeperOpenHelper openHelper) {
            super(context);
            this.mOpenHelper = openHelper;
        }

        @Override
        public Cursor loadInBackground() {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            final String[] noteColumns =
                    new String[]{
                            NoteInfoEntry.COLUMN_NOTE_TITLE,
                            NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID),
                            NoteInfoEntry.getQName(NoteInfoEntry._ID),
                            CourseInfoEntry.COLUMN_COURSE_TITLE
                    };
//            final String noteOrderBy = NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
            // note_info JOIN course_info IN note_info.course_id = course_info.course_id
            final String noteOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
            String tableWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME +
                    " ON " + NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                    CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

            return db.query(tableWithJoin,
                    noteColumns,
                    null,
                    null,
                    null,
                    null,
                    noteOrderBy
            );
        }
    }
}
