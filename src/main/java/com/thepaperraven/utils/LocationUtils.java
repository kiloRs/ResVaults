package com.thepaperraven.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtils {

    private static final String DELIMITER = ",";

    public static Location getLocationFromString(String serializedLocation) {
        String[] parts = serializedLocation.split(DELIMITER);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid serialized location: " + serializedLocation);
        }
        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public static String getStringOfLocation(Location location) {
        String worldName = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        return worldName + DELIMITER + x + DELIMITER + y + DELIMITER + z;
    }
}