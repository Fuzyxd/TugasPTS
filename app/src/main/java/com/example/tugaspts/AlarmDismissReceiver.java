package com.example.tugaspts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.util.Log;
import android.widget.Toast;

public class AlarmDismissReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmDismissReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int alarmId = intent.getIntExtra("alarm_id", 0);

        Log.d(TAG, "Dismissing alarm ID: " + alarmId);

        // Cancel alarm di AlarmManager
        AlarmHelper.cancelAlarm(context, alarmId);

        // Cancel notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(alarmId);
            Log.d(TAG, "Notification cancelled for alarm ID: " + alarmId);
        }

        // HENTIKAN SUARA ALARM - kirim broadcast ke AlarmReceiver
        Intent stopSoundIntent = new Intent("STOP_ALARM_SOUND");
        stopSoundIntent.putExtra("alarm_id", alarmId);
        context.sendBroadcast(stopSoundIntent);

        Log.d(TAG, "Stop sound broadcast sent for alarm ID: " + alarmId);

        Toast.makeText(context, "Alarm ditutup", Toast.LENGTH_SHORT).show();
    }
}