package com.colisa.notekeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.content.SharedPreferencesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.navigation.ui.AppBarConfiguration;

import java.util.List;

import static com.colisa.notekeeper.NoteKeeperDatabaseContract.*;

public class MainActivity extends AppCompatActivity implements DrawerLayout.DrawerListener, NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String NOTE_POSITION = "com.colisa.notekeeper.NOTE_POSITION";
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
        loadNotes();
        updateNavigationHeader();
    }

    private void loadNotes() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        final String[] noteColumns =
                new String[]{
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry._ID
                };
        final String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME,
                noteColumns,
                null,
                null,
                null,
                null,
                noteOrderBy
        );
        mNoteRecyclerAdapter.changeCursor(noteCursor);
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

    private void handleSelection(int messageId) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, messageId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();

    }
}
