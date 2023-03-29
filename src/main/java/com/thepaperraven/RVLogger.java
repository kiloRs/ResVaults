package com.thepaperraven;

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
        return Math.max(0, Math.min(5, getConfig().getInt("log.level", 2)));
    }

    private String getLogLevelPrefix(int level) {
        switch (level) {
            case 0:
                return "[OFF] ";
            case 1:
                return "[SEVERE] ";
            case 2:
                return "[WARNING] ";
            case 3:
                return "[INFO] ";
            case 4:
                return "[CONFIG] ";
            case 5:
                return "[FINE] ";
            default:
                return "";
        }
    }

    private static ConfigurationSection getConfig() {
        return Bukkit.getPluginManager().getPlugin("ResourceVaults").getConfig();
    }
}
