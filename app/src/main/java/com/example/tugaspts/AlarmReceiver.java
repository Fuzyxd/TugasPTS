package com.example.tugaspts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private static final String TAG = "AlarmReceiver";
    private static final String CHANNEL_ID = "ALARM_CHANNEL";
    private static final String STOP_SOUND_ACTION = "STOP_ALARM_SOUND";

    private int currentAlarmId = 0;
    private Context context;
    private BroadcastReceiver stopSoundReceiver;

    // Tambahkan flag untuk mencegah multiple execution
    private static boolean isPlaying = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());

        // Cek action intent - hanya proses jika ini adalah alarm trigger
        String action = intent.getAction();
        if (action != null && (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON"))) {
            Log.d(TAG, "Ignoring boot-related action in AlarmReceiver: " + action);
            return;
        }

        // Cek apakah sudah ada alarm yang sedang berjalan
        if (isPlaying) {
            Log.d(TAG, "Alarm is already playing, ignoring duplicate trigger");
            return;
        }

        String alarmTime = intent.getStringExtra("alarm_time");
        String alarmLabel = intent.getStringExtra("alarm_label");
        currentAlarmId = intent.getIntExtra("alarm_id", 0);

        // Validasi data yang diperlukan
        if (alarmTime == null || alarmLabel == null || currentAlarmId == 0) {
            Log.e(TAG, "Invalid alarm data received - Time: " + alarmTime +
                    ", Label: " + alarmLabel + ", ID: " + currentAlarmId);
            return;
        }

        Log.d(TAG, "=== ALARM RECEIVER TRIGGERED ===");
        Log.d(TAG, "Alarm Time: " + alarmTime);
        Log.d(TAG, "Alarm Label: " + alarmLabel);
        Log.d(TAG, "Alarm ID: " + currentAlarmId);

        // Set flag playing
        isPlaying = true;

        // Setup receiver untuk stop sound
        setupStopSoundReceiver();

        // Buat channel notifikasi
        createNotificationChannel(context);

        // Tampilkan notifikasi
        showAlarmNotification(context, alarmTime, alarmLabel, currentAlarmId);

        // Mainkan suara
        playAlarmSound(context);

        // Stop suara setelah 30 detik (fallback)
        handler.postDelayed(() -> {
            Log.d(TAG, "Auto-stopping alarm sound after 30 seconds");
            stopAlarmSound();
            unregisterStopSoundReceiver();
            isPlaying = false;
        }, 30000);

        boolean isSnoozed = intent.getBooleanExtra("is_snoozed", false);
        if (isSnoozed) {
            Log.d(TAG, "This is a snoozed alarm");
            // Optional: Tampilkan label berbeda untuk snooze
        }
    }

    // Di AlarmReceiver.java - pastikan sudah ada ini:
    private void setupStopSoundReceiver() {
        stopSoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("STOP_ALARM_SOUND".equals(intent.getAction())) {
                    int receivedAlarmId = intent.getIntExtra("alarm_id", 0);

                    Log.d(TAG, "Stop sound received for alarm ID: " + receivedAlarmId + ", current: " + currentAlarmId);

                    // Jika alarm ID cocok atau 0 (stop semua), stop suara
                    if (receivedAlarmId == currentAlarmId || receivedAlarmId == 0) {
                        Log.d(TAG, "Stopping alarm sound by snooze/dismiss button");
                        stopAlarmSound();
                        unregisterStopSoundReceiver();

                        // Juga cancel handler auto-stop
                        if (handler != null) {
                            handler.removeCallbacksAndMessages(null);
                        }

                        // Reset flag
                        isPlaying = false;
                    }
                }
            }
        };

        // Daftarkan receiver
        try {
            IntentFilter filter = new IntentFilter("STOP_ALARM_SOUND");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(stopSoundReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(stopSoundReceiver, filter);
            }
            Log.d(TAG, "Stop sound receiver registered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error registering stop sound receiver: " + e.getMessage());
        }
    }

    private void unregisterStopSoundReceiver() {
        try {
            if (stopSoundReceiver != null && context != null) {
                context.unregisterReceiver(stopSoundReceiver);
                stopSoundReceiver = null;
                Log.d(TAG, "Stop sound receiver unregistered successfully");
            }
        } catch (Exception e) {
            // Receiver sudah di-unregister
            Log.d(TAG, "Stop sound receiver already unregistered or error: " + e.getMessage());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for alarm notifications");
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            channel.enableVibration(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }

    private void showAlarmNotification(Context context, String alarmTime, String alarmLabel, int alarmId) {
        try {
            // Intent untuk tombol "Tutup"
            Intent dismissIntent = new Intent(context, AlarmDismissReceiver.class);
            dismissIntent.putExtra("alarm_id", alarmId);

            int dismissFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dismissFlags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId * 10 + 1, // ID unik untuk dismiss
                    dismissIntent,
                    dismissFlags
            );

            // Intent untuk tombol "Tunda" (Snooze)
            Intent snoozeIntent = new Intent(context, AlarmSnoozeReceiver.class);
            snoozeIntent.putExtra("alarm_id", alarmId);
            snoozeIntent.putExtra("alarm_time", alarmTime);
            snoozeIntent.putExtra("alarm_label", alarmLabel);

            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId * 10 + 2, // ID unik untuk snooze
                    snoozeIntent,
                    dismissFlags
            );

            // Intent untuk buka app ketika notifikasi diklik
            Intent appIntent = new Intent(context, MainActivity2.class);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent appPendingIntent = PendingIntent.getActivity(
                    context,
                    alarmId,
                    appIntent,
                    dismissFlags
            );

            // Dapatkan waktu sekarang
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            String currentDay = new SimpleDateFormat("EEE", new Locale("id")).format(new Date());

            Log.d(TAG, "Creating notification - Current: " + currentTime + ", Alarm: " + alarmTime);

            // Buat notifikasi
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.alarm)
                    .setContentTitle("Alarm - " + (alarmLabel.isEmpty() ? "Alarm" : alarmLabel))
                    .setContentText(alarmTime + " - " + currentDay)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Waktu alarm: " + alarmTime + "\n" +
                                    "Hari: " + currentDay + "\n" +
                                    "Label: " + (alarmLabel.isEmpty() ? "Alarm" : alarmLabel) + "\n" +
                                    "Waktu sekarang: " + currentTime))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(appPendingIntent)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setVibrate(new long[]{0, 1000, 500, 1000})
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setTimeoutAfter(30000)
                    // Tambah tombol aksi tanpa icon, hanya teks
                    .addAction(0, "Tunda 5 Menit", snoozePendingIntent)
                    .addAction(0, "Tutup", dismissPendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.notify(alarmId, builder.build());
                Log.d(TAG, "Notification displayed successfully with ID: " + alarmId);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playAlarmSound(Context context) {
        try {
            // Stop previous sound if any
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            mediaPlayer = MediaPlayer.create(context, R.raw.notif);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.start();
                Log.d(TAG, "Alarm sound started successfully");
            } else {
                Log.e(TAG, "Failed to create MediaPlayer - sound file might be missing");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing alarm sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d(TAG, "Alarm sound stopped successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping alarm sound: " + e.getMessage());
            }
        }
        isPlaying = false; // Pastikan flag di-reset
    }

    @Override
    protected void finalize() throws Throwable {
        // Cleanup resources
        Log.d(TAG, "Finalizing AlarmReceiver - cleaning up resources");
        stopAlarmSound();
        unregisterStopSoundReceiver();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        isPlaying = false;
        super.finalize();
    }
}