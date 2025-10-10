package com.example.tugaspts;

public class Alarm {
    private String time;
    private String label;
    private String days;
    private boolean active;

    // Constructor
    public Alarm() {}

    public Alarm(String time, String label, String days, boolean active) {
        this.time = time;
        this.label = label;
        this.days = days;
        this.active = active;
    }

    // Getters and setters
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getDays() { return days; }
    public void setDays(String days) { this.days = days; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}