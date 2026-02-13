package com.example.timedcommands;

import java.util.List;

public class TimedTask {
    private String name;
    private String time;
    private List<String> days;
    private List<String> commands;
    private boolean enabled;
    
    // Default constructor for SnakeYAML
    public TimedTask() {
        this.name = "";
        this.time = "00:00:00";
        this.days = List.of();
        this.commands = List.of();
        this.enabled = true;
    }
    
    public TimedTask(String name, String time, List<String> days, List<String> commands, boolean enabled) {
        this.name = name;
        this.time = time;
        this.days = days;
        this.commands = commands;
        this.enabled = enabled;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public List<String> getDays() {
        return days;
    }
    
    public void setDays(List<String> days) {
        this.days = days;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
