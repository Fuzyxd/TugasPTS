package com.example.tugaspts;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmSnoozeReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmSnoozeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("alarm_id", 0);
        String alarmTime = intent.getStringExtra("alarm_time");
        String alarmLabel = intent.getStringExtra("alarm_label");

        Log.d(TAG, "Snooze triggered for alarm ID: " + alarmId);

        // 1. ⭐⭐⭐ STOP SOUND ALARM YANG SEDANG BERMAIN ⭐⭐⭐
        stopCurrentAlarmSound(context, alarmId);

        // 2. CANCEL ALARM YANG SEKARANG
        cancelCurrentAlarm(context, alarmId);

        // 3. SET ALARM UNTUK 5 MENIT LAGI
        Calendar snoozeTime = Calendar.getInstance();
        snoozeTime.add(Calendar.MINUTE, 5);

        // Gunakan ID yang sama untuk alarm
        Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
        snoozeIntent.putExtra("alarm_time", alarmTime);
        snoozeIntent.putExtra("alarm_label", alarmLabel);
        snoozeIntent.putExtra("alarm_id", alarmId);
        snoozeIntent.putExtra("is_snoozed", true); // Flag untuk snooze

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId, // PAKAI ID YANG SAMA
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        snoozeTime.getTimeInMillis(),
                        pendingIntent
                );
            }
        }

        // 4. CANCEL NOTIFICATION
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(alarmId);
        }

        Toast.makeText(context, "Alarm ditunda 5 menit", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Alarm snoozed for 5 minutes - Sound stopped");
    }

    // ⭐⭐⭐ METHOD BARU UNTUK STOP SOUND ⭐⭐⭐
    private void stopCurrentAlarmSound(Context context, int alarmId) {
        try {
            // Kirim broadcast ke AlarmReceiver untuk stop sound
            Intent stopSoundIntent = new Intent("STOP_ALARM_SOUND");
            stopSoundIntent.putExtra("alarm_id", alarmId);
            context.sendBroadcast(stopSoundIntent);

            Log.d(TAG, "Sent stop sound command for alarm ID: " + alarmId);
        } catch (Exception e) {
            Log.e(TAG, "Error stopping alarm sound: " + e.getMessage());
        }
    }

    private void cancelCurrentAlarm(Context context, int alarmId) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent cancelIntent = new Intent(context, AlarmReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            pendingIntent.cancel();

        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm: " + e.getMessage());
        }
    }
}