package com.example.tugaspts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    private LinearLayout alarmContainer;
    private TextView tvNoAlarm;
    private ImageView fabAdd;
    private ImageView ivProfile;
    private View fab_bg;
    private List<Alarm> alarmsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initViews();
        setupClickListeners();
        loadAlarms();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initViews() {
        alarmContainer = findViewById(R.id.alarm_container);
        tvNoAlarm = findViewById(R.id.tv_no_alarm);
        fabAdd = findViewById(R.id.fab_add);
        ivProfile = findViewById(R.id.iv_profile);
    }

    private void setupClickListeners() {
        // FAB Add Alarm
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity7.class);
            startActivityForResult(intent, 1);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Profile icon click - cek status login
        ivProfile.setOnClickListener(v -> {
            checkLoginStatusAndNavigate();
        });

        setupBottomNavigation();
    }

    // Method baru untuk cek status login
    private void checkLoginStatusAndNavigate() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            // Jika sudah login, pergi ke profile page (MainActivity8)
            Intent intent = new Intent(MainActivity2.this, MainActivity8.class);

            // Pass user data ke profile activity
            String username = prefs.getString("username", "");
            String userEmail = prefs.getString("user_email", "");

            if (!username.isEmpty()) {
                intent.putExtra("user_name", username);
            }
            if (!userEmail.isEmpty()) {
                intent.putExtra("user_email", userEmail);
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            // Jika belum login, pergi ke login page (MainActivity)
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    private void setupBottomNavigation() {
        findViewById(R.id.nav_alarm_container).setOnClickListener(v -> {
            // Sudah di halaman alarm
        });

        findViewById(R.id.nav_world_container).setOnClickListener(v -> {
            navigateToActivity(MainActivity3.class);
        });

        findViewById(R.id.nav_timer_container).setOnClickListener(v -> {
            navigateToActivity(MainActivity4.class);
        });

        findViewById(R.id.nav_stopwatch_container).setOnClickListener(v -> {
            navigateToActivity(MainActivity5.class);
        });

        findViewById(R.id.nav_setting_container).setOnClickListener(v -> {
            // Untuk bottom navigation, selalu cek status login
            checkLoginStatusAndNavigate();
        });
    }

    private void navigateToActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity2.this, cls);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void loadAlarms() {
        ScrollView scrollViewAlarms = findViewById(R.id.scroll_view_alarms);

        if (alarmContainer != null) {
            alarmContainer.removeAllViews();
        }

        alarmsList = getAlarmsFromStorage();

        if (alarmsList.isEmpty()) {
            if (tvNoAlarm != null) {
                tvNoAlarm.setVisibility(View.VISIBLE);
            }
            if (scrollViewAlarms != null) {
                scrollViewAlarms.setVisibility(View.GONE);
            }
        } else {
            if (tvNoAlarm != null) {
                tvNoAlarm.setVisibility(View.GONE);
            }
            if (scrollViewAlarms != null) {
                scrollViewAlarms.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < alarmsList.size(); i++) {
                addAlarmItemToContainer(alarmsList.get(i), i);
            }
        }
    }

    private List<Alarm> getAlarmsFromStorage() {
        List<Alarm> alarms = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
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
                alarms.add(alarm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return alarms;
    }

    private void addAlarmItemToContainer(Alarm alarm, int position) {
        View alarmItemView = getLayoutInflater().inflate(R.layout.activity_item_alarm, alarmContainer, false);

        TextView tvTime = alarmItemView.findViewById(R.id.tv_time);
        TextView tvLabel = alarmItemView.findViewById(R.id.tv_label);
        TextView tvDays = alarmItemView.findViewById(R.id.tv_days);
        ImageButton btnSwitch = alarmItemView.findViewById(R.id.btn_switch);

        tvTime.setText(alarm.getTime());
        tvLabel.setText(alarm.getLabel());
        tvDays.setText(alarm.getDays());

        // Set switch state
        btnSwitch.setImageResource(alarm.isActive() ?
                R.drawable.switch_on : R.drawable.switch_off);

        // Toggle switch
        btnSwitch.setOnClickListener(v -> {
            alarm.setActive(!alarm.isActive());
            btnSwitch.setImageResource(alarm.isActive() ?
                    R.drawable.switch_on : R.drawable.switch_off);
            updateAlarmInStorage(position, alarm.isActive());
        });

        // Klik item untuk edit
        alarmItemView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity2.this, MainActivity7.class);
            intent.putExtra("alarm_index", position);
            intent.putExtra("alarm_time", alarm.getTime());
            intent.putExtra("alarm_label", alarm.getLabel());
            intent.putExtra("alarm_days", alarm.getDays());
            intent.putExtra("alarm_active", alarm.isActive());
            startActivityForResult(intent, 2);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Long click untuk hapus
        alarmItemView.setOnLongClickListener(v -> {
            deleteAlarm(position);
            return true;
        });

        if (alarmContainer != null) {
            alarmContainer.addView(alarmItemView);
        }
    }

    private void updateAlarmInStorage(int position, boolean isActive) {
        if (position >= 0 && position < alarmsList.size()) {
            alarmsList.get(position).setActive(isActive);
            saveAllAlarmsToStorage();
        }
    }

    private void deleteAlarm(int position) {
        if (position >= 0 && position < alarmsList.size()) {
            alarmsList.remove(position);
            saveAllAlarmsToStorage();
            loadAlarms(); // Refresh list
        }
    }

    private void saveAllAlarmsToStorage() {
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);

        try {
            JSONArray jsonArray = new JSONArray();
            for (Alarm alarm : alarmsList) {
                JSONObject alarmJson = new JSONObject();
                alarmJson.put("time", alarm.getTime());
                alarmJson.put("label", alarm.getLabel());
                alarmJson.put("days", alarm.getDays());
                alarmJson.put("active", alarm.isActive());
                jsonArray.put(alarmJson);
            }

            prefs.edit().putString("alarms_list", jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK) {
            loadAlarms(); // Refresh list alarm baik dari tambah atau edit
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data jika diperlukan
        loadAlarms();
    }

    // Handle back button press
    @Override
    public void onBackPressed() {
        // Keluar dari aplikasi atau minimize
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Inner class Alarm
    public static class Alarm {
        private String time;
        private String label;
        private String days;
        private boolean active;

        public Alarm() {}

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getDays() { return days; }
        public void setDays(String days) { this.days = days; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}