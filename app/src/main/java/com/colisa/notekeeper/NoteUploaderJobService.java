package com.colisa.notekeeper;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.colisa.notekeeper.extras.DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {

    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public boolean onStartJob(final JobParameters params) {
        AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... jobParameters) {
                JobParameters jobParams = jobParameters[0];
                String stringDataUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);
                mNoteUploader.doUpload(dataUri);
                if (!mNoteUploader.isCanceled()) {
                    jobFinished(params, false);
                }
                return null;
            }
        };
        mNoteUploader = new NoteUploader(this);
        task.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mNoteUploader.cancel();
        return true;
    }

}
