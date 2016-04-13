package com.fallout.lockscreen.lockscreenusingservice.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LockScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, LockscreenService.class);
            context.startService(pushIntent);
    }
    }
}
