package com.colisa.notekeeper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {

    @Before
    public void setUp() {
        // Make sure that each test run with fresh data
        DataManager dm = DataManager.getInstance();
        dm.getNotes().clear();
        dm.initializeExampleNotes();
    }

    @Test
    public void createNewNote() {
        final DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body text for test note";

        int noteIndex = dm.createNewNote();
        NoteInfo newNote = dm.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        // We just created a note
        // We need to confirm our new created note is on data manager
        NoteInfo compareNote = dm.getNotes().get(noteIndex);
        assertEquals(compareNote.getCourse(), newNote.getCourse());
        assertEquals(compareNote.getTitle(), newNote.getTitle());
        assertEquals(compareNote.getText(), newNote.getText());
    }

    @Test
    public void findSimilarNotes() {
        final DataManager dm = DataManager.getInstance();
        final CourseInfo course = dm.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText1 = "This is the body text for test note";
        final String noteText2 = "This is the body of my second test note";

        int noteIndex1 = dm.createNewNote();
        NoteInfo note1 = dm.getNotes().get(noteIndex1);
        note1.setCourse(course);
        note1.setText(noteText1);
        note1.setTitle(noteTitle);

        int noteIndex2 = dm.createNewNote();
        NoteInfo note2 = dm.getNotes().get(noteIndex2);
        note2.setCourse(course);
        note2.setTitle(noteTitle);
        note2.setText(noteText2);

        // We just created two notes with same course and title
        // Let's test DataManager ability to find correct note
        int foundIndex1 = dm.findNote(note1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = dm.findNote(note2);
        assertEquals(noteIndex2, foundIndex2);
    }
}