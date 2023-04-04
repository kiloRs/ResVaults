package com.thepaperraven.utils;

import com.thepaperraven.ResourceVaults;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class RVLogger {
    private final Logger logger;
    public RVLogger() {
        this.logger = Bukkit.getLogger();
    }

    public void log(String messaging){
        log(getConfigLogLevel(),messaging);
    }
    public void log(int level, String message) {
        if (level <= getConfigLogLevel()) {
            String prefix = getLogLevelPrefix(level);
            logger.info(prefix + message);
        }
    }

    private int getConfigLogLevel() {
        return Math.max(0, Math.min(5, ResourceVaults.getInstance().getConfig().getInt("log.level", 0)));
    }

    private String getLogLevelPrefix(int level) {
        return switch (level) {
            case 0 -> "[*ResourceVaults*] ";
            case 1 -> "[SEVERE] ";
            case 2 -> "[WARNING] ";
            case 3 -> "[INFO] ";
            case 4 -> "[CONFIG] ";
            case 5 -> "[FINE] ";
            default -> "";
        };
    }

}
