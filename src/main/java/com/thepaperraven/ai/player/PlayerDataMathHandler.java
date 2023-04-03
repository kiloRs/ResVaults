package com.thepaperraven.ai.player;

import com.thepaperraven.ai.vault.Vault;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static com.thepaperraven.ResourceVaults.getPlayerData;

public class PlayerDataMathHandler {
    public static boolean deposit(Player player, Material material, int amount) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        Map<Integer, Vault> vaults = playerData.getVaults();

        // Check if player has enough items in inventory
        ItemStack[] inventory = player.getInventory().getContents();
        int found = 0;
        for (ItemStack item : inventory) {
            if (item != null && item.getType() == material) {
                found += item.getAmount();
                if (found >= amount) {
                    break;
                }
            }
        }
        if (found < amount) {
            return false;
        }

        // Check if there's enough space in the matching vaults to store items
        int space = 0;
        for (Vault vault : vaults.values()) {
            if (vault.getMaterial() == material) {
                space += vault.getRemainingSpace();
            }
        }
        if (space < amount) {
            return false;
        }

        // Move items from inventory to matching vaults
        int moved = 0;
        for (Vault vault : vaults.values()) {
            if (vault.getMaterial() == material) {
                int remaining = amount - moved;
                int movedToVault = vault.add(remaining);
                moved += movedToVault;
                if (moved == amount) {
                    break;
                }
            }
        }
        for (ItemStack item : inventory) {
            if (item != null && item.getType() == material) {
                int amountToRemove = Math.min(item.getAmount(), amount - moved);
                item.setAmount(item.getAmount() - amountToRemove);
                moved += amountToRemove;
                if (moved == amount) {
                    break;
                }
            }
        }
        player.getInventory().setContents(inventory);
        return true;
    }

    public static boolean withdraw(Player player, Material material, int amount) {
        PlayerData playerData = getPlayerData(player.getUniqueId());
        Map<Integer, Vault> vaults = playerData.getVaults();

        // Check if there's enough items in the matching vaults to withdraw
        int found = 0;
        for (Vault vault : vaults.values()) {
            if (vault.getMaterial() == material) {
                found += vault.getAmount();
                if (found >= amount) {
                    break;
                }
            }
        }
        if (found < amount) {
            return false;
        }

        // Check if there's enough space in the player's inventory to receive items
        int space = 0;
        ItemStack[] inventory = player.getInventory().getContents();
        for (ItemStack item : inventory) {
            if (item == null) {
                space += 64;
            } else if (item.getType() == material) {
                space += 64 - item.getAmount();
            }
        }
        if (space < amount) {
            return false;
        }

        // Move items from matching vaults to player's inventory
        int moved = 0;
        for (Vault vault : vaults.values()) {
            if (vault.getMaterial() == material) {
                int remaining = amount - moved;
                int movedFromVault = vault.take(remaining);
                moved += movedFromVault;
                if (moved == amount) {
                    break;
                }
            }
        }
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item == null) {
                inventory[i] = new ItemStack(material, 0);
            }
            if (item != null && item.getType() == material) {
                int amountToAdd = Math.min(64 - item.getAmount(), amount - moved);
                item.setAmount(item.getAmount() + amountToAdd);
                moved += amountToAdd;
                if (moved == amount) {
                    break;
                }
            }
        }
        player.getInventory().setContents(inventory);
        return true;
    }
}