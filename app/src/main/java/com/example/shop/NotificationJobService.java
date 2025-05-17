package com.example.shop;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class NotificationJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new NotificationHandler(getApplicationContext()).send("A PecaPont most hihetetlen ajánlatokkal vár!");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
