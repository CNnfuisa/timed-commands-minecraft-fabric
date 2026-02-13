package com.example.timedcommands;

import java.util.List;

public class Config {
    private Global global;
    private List<TimedTask> tasks;
    
    public Config() {
        this.global = new Global();
        this.tasks = List.of(
            new TimedTask(
                "morning-announcement",
                "08:00:00",
                List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
                List.of("say Good morning! Welcome to the server!", "weather clear"),
                true
            ),
            new TimedTask(
                "weekend-event",
                "14:00:00",
                List.of("SAT", "SUN"),
                List.of("say Weekend event starting now!", "xp set @a 1000 points"),
                true
            ),
            new TimedTask(
                "nightly-backup",
                "02:00:00",
                List.of("MON", "WED", "FRI"),
                List.of("say Server backup starting...", "save-all", "say Backup completed successfully!"),
                true
            )
        );
    }
    
    public Global getGlobal() {
        return global;
    }
    
    public void setGlobal(Global global) {
        this.global = global;
    }
    
    public List<TimedTask> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<TimedTask> tasks) {
        this.tasks = tasks;
    }
    
    public static class Global {
        private boolean enabled;
        private String log_level;
        
        public Global() {
            this.enabled = true;
            this.log_level = "INFO";
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getLog_level() {
            return log_level;
        }
        
        public void setLog_level(String log_level) {
            this.log_level = log_level;
        }
    }
}
