package com.thepaperraven.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

public class LocationUtils {

    private static final String DELIMITER = ",";

    public DoubleChest getDoubleChest(Location location){
        if (isDoubleChest(location.getBlock())) {
            return ((Chest) location.getBlock().getState()).getBlockInventory().getHolder() instanceof DoubleChest doubleChest?doubleChest:null;
        }
        return null;
    }
    public static boolean isDoubleChest(Block block) {
        if (block.getState() instanceof Chest chest) {
            InventoryHolder holder = chest.getInventory().getHolder();

            return holder instanceof DoubleChest;
        }

        return false;
    }
    public static Location getLocationFromString(String serializedLocation) {
        String[] parts = serializedLocation.split(DELIMITER);
        if (parts.length != 4) {
            return null;
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