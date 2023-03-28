package com.thepaperraven.ai;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class VaultAmounts {
    private final PlayerData playerData;

    public VaultAmounts(Player player) {
        this.playerData = ResourceVaults.getPlayerData(player.getUniqueId());
    }

    public int getTotalMaterialInAllVaults(Material material) {
        int total = 0;

        for (VaultInstance vault : playerData.getVaults().values()) {
            if (vault.getMetadata().getAllowedMaterial() == material) {
                total += vault.getAmount();
            }
        }

        return total;
    }

    public int getTotalItemsInAllVaults() {
        int total = 0;

        for (VaultInstance vault : playerData.getVaults().values()) {
            total += vault.getAmount();
        }

        return total;
    }

    public int getTotalFromPlayer() {
        int total = 0;
        Player player = playerData.getPlayer();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                total += item.getAmount();
            }
        }

        return total;
    }

    public int getTotalFromPlayer(Material material) {
        int total = 0;
        Player player = playerData.getPlayer();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }

        return total;
    }

    public int getTotalVaults() {
        return playerData.getVaults().size();
    }

    public int getTotalVaults(Material material) {
        int total = 0;

        for (VaultInstance vault : playerData.getVaults().values()) {
            if (vault.getMetadata().getAllowedMaterial() == material) {
                total++;
            }
        }

        return total;
    }

}
