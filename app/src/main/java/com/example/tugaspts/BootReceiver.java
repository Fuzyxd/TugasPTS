package com.example.tugaspts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "BootReceiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
                "com.htc.intent.action.QUICKBOOT_POWERON".equals(action) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {

            Log.d(TAG, "Device boot completed, preparing to restore alarms...");

            // Gunakan Handler untuk delay yang lebih aman
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                Log.d(TAG, "Restoring alarms after boot delay...");
                // Restore semua alarm yang aktif setelah reboot
                AlarmHelper.restoreAlarmsAfterBoot(context);
            }, 10000); // Delay 10 detik untuk memastikan sistem siap
        }
    }
}