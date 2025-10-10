package com.example.tugaspts;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity7 extends AppCompatActivity {
    private TextView tvHour, tvMinute;
    private EditText etNama;
    private String selectedTime = "00:00";
    private String selectedLabel = "Alarm";
    private StringBuilder selectedDays = new StringBuilder();
    private Map<String, Boolean> daysState = new HashMap<>();
    private boolean isEditMode = false;
    private int editAlarmIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);

        initViews();
        initDaysState();
        checkEditMode();
        setupClickListeners();
        applyCoolTimeStyle();
    }

    private void initViews() {
        tvHour = findViewById(R.id.tv_hour);
        tvMinute = findViewById(R.id.tv_minute);
        etNama = findViewById(R.id.et_nama);

        tvHour.setText("00");
        tvMinute.setText("00");
        etNama.setText("Alarm");
    }

    private void initDaysState() {
        String[] days = {"senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "setiap_hari"};
        for (String day : days) {
            daysState.put(day, false);
        }
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("alarm_index")) {
            isEditMode = true;
            editAlarmIndex = intent.getIntExtra("alarm_index", -1);

            selectedTime = intent.getStringExtra("alarm_time");
            selectedLabel = intent.getStringExtra("alarm_label");
            String days = intent.getStringExtra("alarm_days");
            boolean isActive = intent.getBooleanExtra("alarm_active", true);

            String[] timeParts = selectedTime.split(":");
            if (timeParts.length == 2) {
                tvHour.setText(timeParts[0]);
                tvMinute.setText(timeParts[1]);
            }
            etNama.setText(selectedLabel);

            if (days != null && !days.isEmpty()) {
                parseSelectedDays(days);
            }
        }
    }

    private void parseSelectedDays(String days) {
        if (days != null && !days.isEmpty()) {
            String[] dayArray = days.split(",");
            for (String day : dayArray) {
                day = day.trim().toLowerCase();
                if (daysState.containsKey(day)) {
                    daysState.put(day, true);
                    updateSwitchAppearance(day, getSwitchButton(day));
                }
            }
            updateSelectedDays();
        }
    }

    private ImageButton getSwitchButton(String dayName) {
        switch (dayName) {
            case "senin": return findViewById(R.id.switch_senin);
            case "selasa": return findViewById(R.id.switch_selasa);
            case "rabu": return findViewById(R.id.switch_rabu);
            case "kamis": return findViewById(R.id.switch_kamis);
            case "jumat": return findViewById(R.id.switch_jumat);
            case "sabtu": return findViewById(R.id.switch_sabtu);
            case "setiap_hari": return findViewById(R.id.switch_setiap_hari);
            default: return null;
        }
    }

    private void applyCoolTimeStyle() {
        if (tvHour != null) {
            tvHour.setTextSize(42);
            tvHour.setShadowLayer(4, 2, 2, 0x80000000);
        }

        if (tvMinute != null) {
            tvMinute.setTextSize(42);
            tvMinute.setShadowLayer(4, 2, 2, 0x80000000);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_confirm).setOnClickListener(v -> saveAlarm());

        LinearLayout timeContainer = findViewById(R.id.time_container);
        if (timeContainer != null) {
            timeContainer.setOnClickListener(v -> showTimePicker());
        }

        tvHour.setOnClickListener(v -> showTimePicker());
        tvMinute.setOnClickListener(v -> showTimePicker());

        findViewById(R.id.tv_delete).setOnClickListener(v -> deleteAlarm());
        findViewById(R.id.iv_delete).setOnClickListener(v -> deleteAlarm());

        if (!isEditMode) {
            findViewById(R.id.tv_delete).setVisibility(View.GONE);
            findViewById(R.id.iv_delete).setVisibility(View.GONE);
        }

        etNama.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                selectedLabel = etNama.getText().toString().trim();
                if (selectedLabel.isEmpty()) {
                    selectedLabel = "Alarm";
                    etNama.setText("Alarm");
                }
            }
        });

        setupDaySwitches();
    }

    private void setupDaySwitches() {
        int[] daySwitchIds = {
                R.id.switch_senin, R.id.switch_selasa, R.id.switch_rabu,
                R.id.switch_kamis, R.id.switch_jumat, R.id.switch_sabtu,
                R.id.switch_setiap_hari
        };

        String[] dayNames = {
                "senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "setiap_hari"
        };

        for (int i = 0; i < daySwitchIds.length; i++) {
            int switchId = daySwitchIds[i];
            String dayName = dayNames[i];

            ImageButton switchButton = findViewById(switchId);
            if (switchButton != null) {
                switchButton.setOnClickListener(v -> toggleDaySwitch(dayName, switchButton));
            }
        }
    }

    private void toggleDaySwitch(String dayName, ImageButton switchButton) {
        Boolean currentState = daysState.get(dayName);
        if (currentState == null) {
            currentState = false;
        }
        boolean isActive = !currentState;

        if (dayName.equals("setiap_hari")) {
            // Jika "Setiap Hari" dinyalakan, nyalakan semua hari
            if (isActive) {
                setAllDaysState(true);
            } else {
                // Jika "Setiap Hari" dimatikan, matikan semua hari
                setAllDaysState(false);
            }
        } else {
            // Jika hari spesifik diklik
            daysState.put(dayName, isActive);

            // Cek apakah semua hari spesifik sudah dinyalakan
            boolean allDaysSelected = checkIfAllDaysSelected();

            // Jika semua hari spesifik dinyalakan, nyalakan "Setiap Hari"
            // Jika ada yang dimatikan, matikan "Setiap Hari"
            daysState.put("setiap_hari", allDaysSelected);
            updateSwitchAppearance("setiap_hari", findViewById(R.id.switch_setiap_hari));

            updateSwitchAppearance(dayName, switchButton);
        }

        updateSelectedDays();
    }

    private void setAllDaysState(boolean state) {
        String[] allDays = {"senin", "selasa", "rabu", "kamis", "jumat", "sabtu", "setiap_hari"};
        int[] daySwitchIds = {
                R.id.switch_senin, R.id.switch_selasa, R.id.switch_rabu,
                R.id.switch_kamis, R.id.switch_jumat, R.id.switch_sabtu,
                R.id.switch_setiap_hari
        };

        for (int i = 0; i < allDays.length; i++) {
            daysState.put(allDays[i], state);
            updateSwitchAppearance(allDays[i], findViewById(daySwitchIds[i]));
        }
    }

    private boolean checkIfAllDaysSelected() {
        String[] weekDays = {"senin", "selasa", "rabu", "kamis", "jumat", "sabtu"};
        for (String day : weekDays) {
            Boolean state = daysState.get(day);
            if (state == null || !state) {
                return false;
            }
        }
        return true;
    }

    private void updateSwitchAppearance(String dayName, ImageButton switchButton) {
        Boolean isActive = daysState.get(dayName);
        if (isActive == null) {
            isActive = false;
        }
        switchButton.setImageResource(isActive ? R.drawable.switch_on : R.drawable.switch_off);
    }

    private void updateSelectedDays() {
        selectedDays.setLength(0);

        Boolean setiapHari = daysState.get("setiap_hari");
        if (setiapHari != null && setiapHari) {
            selectedDays.append("senin,selasa,rabu,kamis,jumat,sabtu");
            return;
        }

        String[] dayNames = {"senin", "selasa", "rabu", "kamis", "jumat", "sabtu"};
        boolean first = true;

        for (String day : dayNames) {
            Boolean isActive = daysState.get(day);
            if (isActive != null && isActive) {
                if (!first) selectedDays.append(",");
                selectedDays.append(day);
                first = false;
            }
        }
    }

    private void showTimePicker() {
        int currentHour = Integer.parseInt(tvHour.getText().toString());
        int currentMinute = Integer.parseInt(tvMinute.getText().toString());

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                    updateTimeDisplay(hourOfDay, minute);
                    animateTimeChange();
                },
                currentHour, currentMinute, true
        );

        timePickerDialog.setTitle("Pilih Waktu Alarm");
        timePickerDialog.show();
    }

    private void updateTimeDisplay(int hourOfDay, int minute) {
        tvHour.setText(String.format("%02d", hourOfDay));
        tvMinute.setText(String.format("%02d", minute));
    }

    private void animateTimeChange() {
        ScaleAnimation scaleHours = new ScaleAnimation(1.0f, 1.3f, 1.0f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleHours.setDuration(200);
        scaleHours.setRepeatCount(1);
        scaleHours.setRepeatMode(Animation.REVERSE);
        tvHour.startAnimation(scaleHours);

        ScaleAnimation scaleMinutes = new ScaleAnimation(1.0f, 1.3f, 1.0f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleMinutes.setDuration(200);
        scaleMinutes.setStartOffset(100);
        scaleMinutes.setRepeatCount(1);
        scaleMinutes.setRepeatMode(Animation.REVERSE);
        tvMinute.startAnimation(scaleMinutes);
    }

    private void saveAlarm() {
        if (selectedDays.length() == 0) {
            Toast.makeText(this, "Pilih setidaknya satu hari", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedLabel = etNama.getText().toString().trim();
        if (selectedLabel.isEmpty()) {
            selectedLabel = "Alarm";
        }

        Alarm newAlarm = new Alarm();
        newAlarm.setTime(selectedTime);
        newAlarm.setLabel(selectedLabel);
        newAlarm.setDays(selectedDays.toString());
        newAlarm.setActive(true);

        if (isEditMode) {
            updateAlarmInStorage(newAlarm);
            Toast.makeText(this, "Alarm berhasil diupdate", Toast.LENGTH_SHORT).show();
        } else {
            saveAlarmToStorage(newAlarm);
            Toast.makeText(this, "Alarm berhasil ditambahkan", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }

    private void deleteAlarm() {
        if (isEditMode && editAlarmIndex != -1) {
            deleteAlarmFromStorage();
            Toast.makeText(this, "Alarm berhasil dihapus", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void saveAlarmToStorage(Alarm alarm) {
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        String alarmsJson = prefs.getString("alarms_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(alarmsJson);
            JSONObject alarmJson = new JSONObject();
            alarmJson.put("time", alarm.getTime());
            alarmJson.put("label", alarm.getLabel());
            alarmJson.put("days", alarm.getDays());
            alarmJson.put("active", alarm.isActive());

            jsonArray.put(alarmJson);
            prefs.edit().putString("alarms_list", jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error menyimpan alarm", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAlarmInStorage(Alarm updatedAlarm) {
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        String alarmsJson = prefs.getString("alarms_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(alarmsJson);
            if (editAlarmIndex >= 0 && editAlarmIndex < jsonArray.length()) {
                JSONObject alarmJson = jsonArray.getJSONObject(editAlarmIndex);
                alarmJson.put("time", updatedAlarm.getTime());
                alarmJson.put("label", updatedAlarm.getLabel());
                alarmJson.put("days", updatedAlarm.getDays());
                alarmJson.put("active", updatedAlarm.isActive());

                prefs.edit().putString("alarms_list", jsonArray.toString()).apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error mengupdate alarm", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAlarmFromStorage() {
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        String alarmsJson = prefs.getString("alarms_list", "[]");

        try {
            JSONArray jsonArray = new JSONArray(alarmsJson);
            if (editAlarmIndex >= 0 && editAlarmIndex < jsonArray.length()) {
                jsonArray.remove(editAlarmIndex);
                prefs.edit().putString("alarms_list", jsonArray.toString()).apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error menghapus alarm", Toast.LENGTH_SHORT).show();
        }
    }

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