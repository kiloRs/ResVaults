package com.thepaperraven.ai;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public interface VaultInstance{
    UUID getOwnerUUID();
    Location getSignLocation();
    Location getChestLocation1();
    Location getChestLocation2();
    Material getAllowedMaterial();
    boolean isDoubleChest();
    InventoryHolder getHolder();
    VaultMetadata getVaultMetadata();
    boolean isValid();
    public boolean isActive();
    void toConfig(FileConfiguration config, String path);
    static VaultInstance fromConfig(FileConfiguration config, String path) {
        return Vault.fromConfig(config, path);
    }
}