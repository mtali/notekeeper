package com.colisa.notekeeper;


import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.view.Gravity;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerActions.open;
import static androidx.test.espresso.contrib.NavigationViewActions.navigateTo;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;


/**
 * This class tests menu navigation of notes from current to the next
 * Steps
 * 1. Launch MainActivity
 * 2. Open navigation drawer
 * 3. Select notes menu item
 * 4. Select one of the note
 * 5. On NoteActivity click next menu and validate
 */
@RunWith(AndroidJUnit4.class)
public class NextThroughNoteTest {

    private static DataManager sDataManager;


    @BeforeClass
    public static void classSetUp() {
        sDataManager = DataManager.getInstance();
    }

    // Add rule for activity you want to test
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityRule = new ActivityTestRule<>(MainActivity.class);


    // Actual test method
    @Test
    public void nextThroughNotes() {
        onView(withId(R.id.drawer_layout)).perform(open(Gravity.LEFT));
        onView(withId(R.id.navigation_view)).perform(navigateTo(R.id.nav_notes));
        onView(withId(R.id.list_items)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        List<NoteInfo> notes = sDataManager.getNotes();
        for (int i = 0; i < notes.size(); i++) {
            NoteInfo note = notes.get(i);
            onView(withId(R.id.spinner_courses)).check(matches(withSpinnerText(note.getCourse().getTitle())));
            onView(withId(R.id.text_note_title)).check(matches(withText(note.getTitle())));
            onView(withId(R.id.text_note_text)).check(matches(withText(note.getText())));

            if (i < notes.size() - 1) {
                onView(allOf(withId(R.id.action_next), isEnabled())).perform(click());
            }
        }
        onView(withId(R.id.action_next)).check(matches(not(isEnabled())));
        ViewActions.pressBack();
    }

}