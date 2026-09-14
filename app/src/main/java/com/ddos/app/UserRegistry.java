package com.ddos.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class UserRegistry {

    private static final String PREF = "premium_users";

    public static void registerUser(Context ctx) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            String id = ctx.getPackageName() + "_" + System.currentTimeMillis();
            String ts = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            sp.edit()
              .putString(id, ts + "|" + android.os.Build.MODEL + "|"
                  + android.os.Build.VERSION.RELEASE)
              .apply();
        } catch (Exception ignored) {}
    }

    public static String dump(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Map<String, ?> all = sp.getAll();
        StringBuilder sb = new StringBuilder("\uD83D\uDC65 <b>Premium Users:</b> " + all.size() + "\n\n");
        int i = 1;
        for (Map.Entry<String, ?> e : all.entrySet()) {
            String[] parts = String.valueOf(e.getValue()).split("\\|");
            sb.append(i++).append(". ").append(parts.length > 1 ? parts[1] : "?")
              .append(" \u2014 Android ").append(parts.length > 2 ? parts[2] : "?")
              .append("\n   \uD83D\uDD52 ").append(parts.length > 0 ? parts[0] : "?")
              .append("\n\n");
            if (sb.length() > 3500) { sb.append("..."); break; }
        }
        return sb.toString();
    }
}
