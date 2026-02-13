package com.example.timedcommands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskManager {
    private static final Logger LOGGER = LogManager.getLogger("timed-commands");
    private final MinecraftServer server;
    
    public TaskManager(MinecraftServer server) {
        this.server = server;
        LOGGER.info("TaskManager initialized with server instance");
    }
    
    public void executeTask(TimedTask task) {
        LOGGER.info("Executing task: {}", task.getName());
        
        for (String command : task.getCommands()) {
            LOGGER.info("Scheduling command for execution: {}", command);
            
            // Schedule command execution on the main thread
            server.execute(() -> {
                try {
                    LOGGER.info("Executing command on main thread: {}", command);
                    
                    // Get the command source stack from the server
                    CommandSourceStack commandSource = server.createCommandSourceStack();
                    LOGGER.debug("Created command source stack: {}", commandSource);
                    
                    // Execute the command using the server's command dispatcher
                    server.getCommands().performPrefixedCommand(commandSource, command);
                    LOGGER.info("Successfully executed command: {}", command);
                } catch (Exception e) {
                    LOGGER.error("Error executing command: {}", command, e);
                }
            });
        }
    }
}