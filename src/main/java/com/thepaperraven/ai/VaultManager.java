package com.thepaperraven.ai;

import com.thepaperraven.ai.events.VaultCreateEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class VaultManager {
    private final PlayerData playerData;

    public VaultManager(PlayerData playerData) {
        this.playerData = playerData;
    }


    public void addVault(VaultInstance vault) {
        UUID ownerUUID = vault.getOwnerUUID();
        List<VaultInstance> vaults = playerData.getVaults(ownerUUID);
        vaults.add(vault);
        playerData.setVaults(ownerUUID, vaults);
    }


    public void removeVault(VaultInstance vault) {
        UUID ownerUUID = vault.getOwnerUUID();
        List<VaultInstance> vaults = playerData.getVaults(ownerUUID);
        vaults.remove(vault);
        playerData.setVaults(ownerUUID, vaults);
    }


    public List<VaultInstance> getVaults(UUID ownerUUID) {
        return playerData.getVaults(ownerUUID);
    }


    public int getTotalVaultCount() {
        int count = 0;
        for (UUID uuid : playerData.getUUIDs()) {
            count += playerData.getVaults(uuid).size();
        }
        return count;
    }


    public int getTotalVaultCount(Material material) {
        int count = 0;
        for (UUID uuid : playerData.getUUIDs()) {
            List<VaultInstance> vaults = playerData.getVaults(uuid);
            for (VaultInstance vault : vaults) {
                if (vault.getAllowedMaterial() == material) {
                    count++;
                }
            }
        }
        return count;
    }


    public int getTotalMaterialAmount(Material material) {
        int totalAmount = 0;
        for (UUID uuid : playerData.getUUIDs()) {
            List<VaultInstance> vaults = playerData.getVaults(uuid);
            for (VaultInstance vault : vaults) {
                if (vault.getAllowedMaterial() == material) {
                    int amount = vault.getMetadata().getAmount();
                    totalAmount += amount;
                }
            }
        }
        return totalAmount;
    }


    public void updateVaultMetadata(VaultInstance vault, VaultMetadata metadata) {
        UUID ownerUUID = vault.getOwnerUUID();
        List<VaultInstance> vaults = playerData.getVaults(ownerUUID);
        int index = vaults.indexOf(vault);
        VaultInstance updatedVault = new Vault(metadata, vault.getSignLocation(), vault.getChestLocations(), vault.getAllowedMaterial());
        vaults.set(index, updatedVault);
        playerData.setVaults(ownerUUID, vaults);
    }


}
