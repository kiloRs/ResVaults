package com.thepaperraven.ai.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import static com.thepaperraven.ai.ResourceVaults.getPlugin;

public class LockUtils {

    private static final NamespacedKey LOCKED_KEY = new NamespacedKey(getPlugin(), "locked");

    private static final Plugin plugin = Bukkit.getPluginManager().getPlugin("ResourceVaults");

    public static boolean isLocked(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) {
            return false;
        }
        PersistentDataContainer pdc = chest.getPersistentDataContainer();
        return pdc.has(LOCKED_KEY, PersistentDataType.STRING);
    }
    public static void unlockIfPassMatches(Block block, String pass){
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) {
            return;
        }
        PersistentDataContainer pdc = chest.getPersistentDataContainer();
        String passUse = pdc.get(LOCKED_KEY, PersistentDataType.STRING);
        if (pdc.has(LOCKED_KEY, PersistentDataType.STRING) && passUse.equalsIgnoreCase(pass)) {
            pdc.remove(LOCKED_KEY);
        }
        chest.setLock(null);
    }

    public static void lock(Block block, String password) {
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) {
            return;
        }
        PersistentDataContainer pdc = chest.getPersistentDataContainer();
        pdc.set(LOCKED_KEY, PersistentDataType.STRING, password);
        chest.setLock(password);

    }

    public static void unlock(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) {
            return;
        }
        PersistentDataContainer pdc = chest.getPersistentDataContainer();
        if (pdc.has(LOCKED_KEY, PersistentDataType.STRING)) {
            pdc.remove(LOCKED_KEY);
        }
        chest.setLock(null);
    }

}
