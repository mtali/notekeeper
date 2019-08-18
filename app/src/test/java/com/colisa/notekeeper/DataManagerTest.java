package com.colisa.notekeeper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {
    private static DataManager sDataManager;

    @BeforeClass
    public static void classSetup() {
        sDataManager = DataManager.getInstance();
    }

    @Before
    public void setUp() {
        // Make sure that each test run with fresh data
        sDataManager.getNotes().clear();
        sDataManager.initializeExampleNotes();
    }

    @Test
    public void createNewNote() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body text for test note";

        int noteIndex = sDataManager.createNewNote();
        NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        // We just created a note
        // We need to confirm our new created note is on data manager
        NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
        assertEquals(compareNote.getCourse(), newNote.getCourse());
        assertEquals(compareNote.getTitle(), newNote.getTitle());
        assertEquals(compareNote.getText(), newNote.getText());
    }

    @Test
    public void findSimilarNotes() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText1 = "This is the body text for test note";
        final String noteText2 = "This is the body of my second test note";

        int noteIndex1 = sDataManager.createNewNote();
        NoteInfo note1 = sDataManager.getNotes().get(noteIndex1);
        note1.setCourse(course);
        note1.setText(noteText1);
        note1.setTitle(noteTitle);

        int noteIndex2 = sDataManager.createNewNote();
        NoteInfo note2 = sDataManager.getNotes().get(noteIndex2);
        note2.setCourse(course);
        note2.setTitle(noteTitle);
        note2.setText(noteText2);

        // We just created two notes with same course and title
        // Let's test DataManager ability to find correct note
        int foundIndex1 = sDataManager.findNote(note1);
        assertEquals(noteIndex1, foundIndex1);

        int foundIndex2 = sDataManager.findNote(note2);
        assertEquals(noteIndex2, foundIndex2);
    }


    @Test
    public void createNewNoteOneStepCreation() {
        final CourseInfo course = sDataManager.getCourse("android_async");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of my test note";

        int noteIndex = sDataManager.createNewNote(course, noteTitle, noteText);

        NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
        assertEquals(course, compareNote.getCourse());
        assertEquals(noteTitle, compareNote.getTitle());
        assertEquals(noteText, compareNote.getText());
    }
}