package com.example.timedcommands;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimedCommandMod implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("timed-commands");
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private static TimedCommandMod instance;
    private ConfigManager configManager;
    private TaskManager taskManager;
    private ScheduledExecutorService scheduler;
    private MinecraftServer server;
    private final Map<String, ScheduledFuture<?>> taskSchedules = new HashMap<>();
    
    @Override
    public void onInitializeServer() {
        instance = this;
        
        LOGGER.info("Initializing Timed Commands mod...");
        
        // Initialize config manager
        configManager = new ConfigManager();
        configManager.loadConfig();
        
        // Set up server lifecycle event listeners
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Server started, initializing Timed Commands functionality...");
            
            // Save server instance
            this.server = server;
            
            // Check if mod is enabled globally
            if (!configManager.getConfig().getGlobal().isEnabled()) {
                LOGGER.info("Timed Commands mod is disabled in config. Exiting...");
                return;
            }
            
            // Initialize task manager with server instance
            taskManager = new TaskManager(server);
            
            // Set up scheduler for task scheduling
            scheduler = Executors.newScheduledThreadPool(1);
            
            // Schedule all tasks
            scheduleAllTasks();
            
            LOGGER.info("Timed Commands mod initialized successfully!");
        });
        
        // Set up server stopping event listener to clean up resources
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            onShutdown();
        });
    }
    
    private void onShutdown() {
        LOGGER.info("Shutting down Timed Commands mod...");
        
        // Cancel all scheduled tasks
        for (ScheduledFuture<?> future : taskSchedules.values()) {
            future.cancel(false);
        }
        taskSchedules.clear();
        
        // Shutdown the scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Close the config manager resources
        if (configManager != null) {
            configManager.close();
        }
        
        LOGGER.info("Timed Commands mod shutdown completed.");
    }
    
    /**
     * Schedule all tasks from the config
     */
    public void scheduleAllTasks() {
        // Cancel existing schedules
        for (ScheduledFuture<?> future : taskSchedules.values()) {
            future.cancel(false);
        }
        taskSchedules.clear();
        
        // Check if mod is enabled globally
        if (!configManager.getConfig().getGlobal().isEnabled()) {
            LOGGER.debug("Mod is disabled globally, skipping task scheduling");
            return;
        }
        
        // Check if server or task manager is null
        if (server == null || taskManager == null) {
            LOGGER.debug("Server or task manager not initialized, skipping task scheduling");
            return;
        }
        
        Config config = configManager.getConfig();
        LOGGER.debug("Scheduling {} tasks", config.getTasks().size());
        
        for (TimedTask task : config.getTasks()) {
            if (task.isEnabled()) {
                scheduleTask(task);
            }
        }
    }
    
    /**
     * Schedule a single task
     */
    private void scheduleTask(TimedTask task) {
        LOGGER.debug("Scheduling task: {} (time: {}, days: {})", 
            task.getName(), task.getTime(), task.getDays());
        
        // Calculate delay until next execution
        long delay = calculateNextExecutionDelay(task);
        
        if (delay > 0) {
            // Schedule the task
            ScheduledFuture<?> future = scheduler.schedule(() -> {
                try {
                    executeTask(task);
                } finally {
                    // Reschedule the task for next week
                    scheduleTask(task);
                }
            }, delay, TimeUnit.SECONDS);
            
            // Store the future for cancellation
            taskSchedules.put(task.getName(), future);
            
            LOGGER.debug("Task {} scheduled to run in {} seconds", task.getName(), delay);
        } else {
            LOGGER.debug("Task {} has no valid execution time, skipping", task.getName());
        }
    }
    
    /**
     * Calculate delay until next execution for a task
     */
    private long calculateNextExecutionDelay(TimedTask task) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime taskTime = LocalTime.parse(task.getTime(), TIME_FORMATTER);
        
        // Find the next day of week that matches
        for (int i = 0; i < 7; i++) {
            LocalDateTime candidate = now.plusDays(i);
            DayOfWeek day = candidate.getDayOfWeek();
            String dayShortName = day.name().substring(0, 3);
            
            if (task.getDays().contains(dayShortName)) {
                LocalDateTime executionTime = candidate.with(taskTime);
                
                // If this time is in the future, calculate delay
                if (executionTime.isAfter(now)) {
                    return ChronoUnit.SECONDS.between(now, executionTime);
                }
            }
        }
        
        // If no valid time found in the next 7 days, return -1
        return -1;
    }
    
    /**
     * Execute a task
     */
    private void executeTask(TimedTask task) {
        LOGGER.info("Executing task: {}", task.getName());
        taskManager.executeTask(task);
    }
    
    public static TimedCommandMod getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public MinecraftServer getServer() {
        return server;
    }
}