package com.thepaperraven;


import com.jeff_media.jefflib.NumberUtils;
import com.thepaperraven.data.vault.Vault;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class VaultManager {

    public static Vault fromConfig(ConfigurationSection config) {
        Integer number = NumberUtils.parseInteger(config.getName());
        if (number == null){
            throw new RuntimeException("Invalid Key Read of Index!");
        }
        int index = number.intValue();
        UUID uuid = UUID.fromString(config.getParent().getName());
        Material material = Material.getMaterial(config.getString("material",ResourceVaults.getConfiguration().getDefaultVaultMaterial().name().toUpperCase()));
        World world = Bukkit.getWorld(config.getString("location.chest.world","world"));
        int x = config.getInt("location.chest.x",0);
        int y = config.getInt("location.chest.y",0);
        int z = config.getInt("location.chest.z",0);
        if (x == 0 && y == 0 && z == 0){
            throw new RuntimeException("Invalid Coordinates!");
        }
        Location chestLocation = new Location(world, x, y, z);
        BlockState chestState = chestLocation.getBlock().getState();
        if (!(chestState instanceof Chest chest)) {
            throw new IllegalStateException("Vault chest block is not a chest: " + chestLocation);
        }
        if (material == null){
            throw new RuntimeException("No Valid Material Found for " + index);
        }
        return new Vault(index, uuid, material, chest);
    }

}
