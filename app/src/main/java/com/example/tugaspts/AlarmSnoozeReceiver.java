package com.example.tugaspts;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmSnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("alarm_id", 0);
        String alarmTime = intent.getStringExtra("alarm_time");
        String alarmLabel = intent.getStringExtra("alarm_label");

        // Set alarm untuk 5 menit lagi
        Calendar snoozeTime = Calendar.getInstance();
        snoozeTime.add(Calendar.MINUTE, 5);

        // Buat intent untuk alarm snooze
        Intent snoozeAlarmIntent = new Intent(context, AlarmReceiver.class);
        snoozeAlarmIntent.putExtra("alarm_time", alarmTime);
        snoozeAlarmIntent.putExtra("alarm_label", alarmLabel + " (Tunda)");
        snoozeAlarmIntent.putExtra("alarm_id", alarmId + 10000); // ID berbeda untuk snooze

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId + 10000,
                snoozeAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set snooze alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.getTimeInMillis(),
                    snoozePendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime.getTimeInMillis(),
                    snoozePendingIntent
            );
        }

        // Cancel notification yang sekarang
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(alarmId);

        Toast.makeText(context, "Alarm ditunda 5 menit", Toast.LENGTH_SHORT).show();
    }
}