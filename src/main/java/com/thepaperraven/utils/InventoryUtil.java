package com.thepaperraven.utils;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.data.vault.Vault;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

public class InventoryUtil {

    public static boolean isVault(Inventory inventory){
        return inventory.getHolder() instanceof Vault vault;
    }
    public static boolean isVault(Chest chest){
        return chest.getInventory().getHolder() instanceof Vault vault;
    }

public static @Nullable BlockFace getFacing(Block block) {
        if (block.getState() instanceof org.bukkit.material.Directional directional){
            ResourceVaults.log("Directional: " + directional.getClass());
            return directional.getFacing();
        }
        if (block.getBlockData() instanceof Directional directional) {
            ResourceVaults.log("Directional-2: " + directional.getClass());

            return directional.getFacing();
        }
        if (block.getState().getBlockData() instanceof Rotatable rotatable) {
            return rotatable.getRotation();
        }
        if (block.getType().name().endsWith("WALL_SIGN")) {
            BlockData data = block.getBlockData();
            if (data instanceof WallSign wallSign) {
                return wallSign.getFacing();
            }
        }
        return null;
    }

}

