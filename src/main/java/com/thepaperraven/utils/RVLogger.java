package com.thepaperraven.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

public class RVLogger {
    private static final RVLogger instance = new RVLogger();
    private final Logger logger;

    public static RVLogger getInstance() {
        return instance;
    }

    private RVLogger() {
        this.logger = Bukkit.getLogger();
    }

    public void log(int level, String message) {
        if (level <= getConfigLogLevel()) {
            String prefix = getLogLevelPrefix(level);
            logger.info(prefix + message);
        }
    }

    private int getConfigLogLevel() {
        return Math.max(0, Math.min(5, getConfig().getInt("log.level", 3)));
    }

    private String getLogLevelPrefix(int level) {
        return switch (level) {
            case 0 -> "[OFF] ";
            case 1 -> "[SEVERE] ";
            case 2 -> "[WARNING] ";
            case 3 -> "[INFO] ";
            case 4 -> "[CONFIG] ";
            case 5 -> "[FINE] ";
            default -> "";
        };
    }

    private static ConfigurationSection getConfig() {
        return Bukkit.getPluginManager().getPlugin("ResourceVaults").getConfig();
    }
}
