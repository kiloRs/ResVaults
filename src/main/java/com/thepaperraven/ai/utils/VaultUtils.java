package com.thepaperraven.ai.utils;

import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class VaultUtils {
    /**
     * Returns the Vault object corresponding to the block that was placed
     * @param block The block that was placed
     * @param player The player who placed the block
     * @return The Vault object, or null if the block isn't a chest or the player is not the owner
     */
    public static Vault getVaultFromBlock(Block block, Player player) {
        // Make sure the block is a chest
        if (!isChest(block)) {
            return null;
        }

        // Make sure the player is the owner
        Vault vault = Vault.fromBlock(block);
        if (vault != null && vault.isOwner(player)) {
            return vault;
        } else {
            return null;
        }
    }

    /**
     * Checks if a block is a chest
     * @param block The block to check
     * @return true if the block is a chest, false otherwise
     */
    private static boolean isChest(Block block) {
        return block.getState() instanceof Chest chest;
    }

    /**
     * Teleports a player to a vault
     * @param player The player to teleport
     * @param vault The vault to teleport to
     */
    public static void teleportToVault(Player player, Vault vault) {
        Location location = vault.getLocation();
        location.setX(location.getX() + 0.5);
        location.setY(location.getY() + 0.5);
        location.setZ(location.getZ() + 0.5);
        player.teleport(location);
    }

    public static void removeItemsFrom(Player player, Material type, int amount) {
        PlayerData playerData = ResourceVaults.getPlayer(player);

        Map<Integer, Vault> map = playerData.getDataMap();
        if (map.isEmpty()) {
            return;
        }

        int count = 0;
        for (Map.Entry<Integer, Vault> entry : map.entrySet()) {
            Vault vault = entry.getValue();

            if (vault.getMaterialType() == type) {
                Inventory inventory = vault.getChest().getBlockInventory();

                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() == type) {
                        int itemAmount = item.getAmount();
                        if (count + itemAmount <= amount) {
                            inventory.removeItem(item);
                            count += itemAmount;
                        } else {
                            item.setAmount(amount - count);
                            count = amount;
                            break;
                        }
                    }
                    if (count == amount) {
                        return;
                    }
                }
            }
        }
    }
}
