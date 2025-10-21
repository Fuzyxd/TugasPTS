package com.example.tugaspts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class AlarmHelper {
    private static final String TAG = "AlarmHelper";

    public static void setAlarm(Context context, Alarm alarm) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }

            // Parse waktu (format: "HH:mm")
            String[] timeParts = alarm.getTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Set waktu alarm
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, hour);
            alarmTime.set(Calendar.MINUTE, minute);
            alarmTime.set(Calendar.SECOND, 0);
            alarmTime.set(Calendar.MILLISECOND, 0);

            // Jika waktu sudah lewat hari ini, set untuk besok
            if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
                alarmTime.add(Calendar.DAY_OF_YEAR, 1);
                Log.d(TAG, "Alarm set for tomorrow: " + alarmTime.getTime());
            }

            // Buat intent untuk AlarmReceiver
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarm_time", alarm.getTime());
            intent.putExtra("alarm_label", alarm.getLabel());
            intent.putExtra("alarm_id", alarm.getAlarmId());

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarm.getAlarmId(),
                    intent,
                    flags
            );

            // Set alarm berdasarkan versi Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ - cek permission exact alarm
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "Alarm set with setExactAndAllowWhileIdle (Android 12+)");
                } else {
                    // Fallback untuk Android 12+ tanpa permission
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "Alarm set with setAndAllowWhileIdle (fallback)");
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "Alarm set with setExactAndAllowWhileIdle");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Android 4.4+
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "Alarm set with setExact");
            } else {
                // Android < 4.4
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "Alarm set with set");
            }

            Log.d(TAG, "Alarm scheduled successfully: " + alarm.getTime() +
                    " ID: " + alarm.getAlarmId() +
                    " Trigger time: " + alarmTime.getTime());

        } catch (Exception e) {
            Log.e(TAG, "Error setting alarm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void cancelAlarm(Context context, int alarmId) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }

            Intent intent = new Intent(context, AlarmReceiver.class);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    intent,
                    flags
            );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

            Log.d(TAG, "Alarm cancelled successfully: " + alarmId);

        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void restoreAlarmsAfterBoot(Context context) {
        Log.d(TAG, "Restoring alarms after boot...");

        List<Alarm> alarms = getAlarmsFromStorage(context);
        int restoredCount = 0;

        for (Alarm alarm : alarms) {
            if (alarm.isActive()) {
                // Set ulang alarm yang aktif
                setAlarm(context, alarm);
                restoredCount++;
                Log.d(TAG, "Restored alarm: " + alarm.getTime() + " - " + alarm.getLabel());
            }
        }

        Log.d(TAG, "Alarm restoration completed. " + restoredCount + " alarms restored.");
    }

    private static List<Alarm> getAlarmsFromStorage(Context context) {
        List<Alarm> alarms = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
        String alarmsJson = prefs.getString("alarms_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(alarmsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Alarm alarm = new Alarm();
                alarm.setTime(jsonObject.getString("time"));
                alarm.setLabel(jsonObject.getString("label"));
                alarm.setDays(jsonObject.getString("days"));
                alarm.setActive(jsonObject.getBoolean("active"));

                // Load alarmId jika ada, jika tidak generate baru
                if (jsonObject.has("alarmId")) {
                    alarm.setAlarmId(jsonObject.getInt("alarmId"));
                } else {
                    // Generate ID yang unik
                    alarm.setAlarmId((int) System.currentTimeMillis() + i);
                }

                alarms.add(alarm);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing alarms from storage: " + e.getMessage());
            e.printStackTrace();
        }

        return alarms;
    }

    // Method untuk menghapus semua alarm (cleanup)
    public static void cancelAllAlarms(Context context) {
        List<Alarm> alarms = getAlarmsFromStorage(context);

        for (Alarm alarm : alarms) {
            if (alarm.isActive()) {
                cancelAlarm(context, alarm.getAlarmId());
                Log.d(TAG, "Cancelled alarm: " + alarm.getAlarmId());
            }
        }

        Log.d(TAG, "All alarms cancelled");
    }

    // Method untuk cek apakah alarm dengan ID tertentu aktif
    public static boolean isAlarmActive(Context context, int alarmId) {
        List<Alarm> alarms = getAlarmsFromStorage(context);

        for (Alarm alarm : alarms) {
            if (alarm.getAlarmId() == alarmId && alarm.isActive()) {
                return true;
            }
        }

        return false;
    }
}