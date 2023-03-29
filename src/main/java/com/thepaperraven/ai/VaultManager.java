package com.thepaperraven.ai;

import com.thepaperraven.ResourceVaults;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.thepaperraven.ResourceVaults.getPlayerData;

public class VaultManager implements VaultManagerSystem {
    private final Plugin storedPlugin;
    private static boolean initialized = false;

    public VaultManager(Plugin plugin) {
        this.storedPlugin = plugin;

        if (initialized&&plugin!= ResourceVaults.getPlugin()){
            throw new RuntimeException("You cannot reinitialize the Vault Manager! Please use ResourceVaults.getVaultManager()");
        }
        initialized = true;
    }

    @Override
    public Vault createVault(UUID ownerUUID, Material material, Location signLocation, List<Location> chestLocations) {
        PlayerData playerData = getPlayerData(ownerUUID);
        int vaultIndex = playerData.getNextIndex();
        Vault vaultInstance = new Vault(VaultMetadata.get(material, playerData.getPlayer(), vaultIndex), chestLocations,signLocation);
        playerData.addVault(vaultInstance);
        vaultInstance.updatePDC();
        return vaultInstance;
    }

    @Override
    public Vault getVault(UUID ownerUUID, int index, Material material) {
        VaultInstance vault = getPlayerData(ownerUUID).getVault(index);
        return vault.getMetadata().getAllowedMaterial()==material? ((Vault) vault):null;
    }

    @Override
    public Vault getVault(UUID ownerUUID, int index) {
        PlayerData playerData = getPlayerData(ownerUUID);
        return (Vault) playerData.getVault(index);
    }


    @Override
    public void deleteVault(Vault vault) {
        PlayerData playerData = getPlayerData(vault.getMetadata().getOwnerUUID());
        playerData.removeVault(vault.getMetadata().getVaultIndex());
    }

    @Override
    public List<Vault> getVaults(UUID ownerUUID) {
        List<Vault> vaults = new ArrayList<>();
        PlayerData playerData = getPlayerData(ownerUUID);
        for (Map.Entry<Integer,VaultInstance> vaultsLoaded : playerData.getVaults().entrySet()) {
            vaults.add(((Vault) vaultsLoaded.getValue()));
        }
        return vaults;
    }

    @Override
    public List<Vault> getVaults(Material material, UUID owner) {
        PlayerData playerData = getPlayerData(owner);
        return playerData.getVaultsByMaterial(material);
    }

    @Override
    public int getBalance(UUID ownerUUID) {
        PlayerData playerData = getPlayerData(ownerUUID);
        int total = 0;
        for (Map.Entry<Integer,VaultInstance> vaultsLoaded : playerData.getVaults().entrySet()) {
            int amount = vaultsLoaded.getValue().getAmount();
            total = amount + total;
        }
        return total;
    }

    /**
     * @param material the material type
     * @return all known vaults in existence!
     */
    public List<Vault> getVaults(Material material) {
        List<Vault> vaults = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = ResourceVaults.getPlayerData(player.getUniqueId());
            for (VaultInstance vaultInstance : playerData.getVaults().values()) {
                if (vaultInstance.getMetadata().getAllowedMaterial() == material) {
                    vaults.add(((Vault) vaultInstance));
                }
            }
        }
        return vaults;
    }

    @Override
    public int getBalance(UUID ownerUUID, Material material) {
        PlayerData playerData = getPlayerData(ownerUUID);
        return playerData.getVaultAmounts().getTotalMaterialInAllVaults(material);
    }

    @Override
    public int getTotalVaults(UUID ownerUUID) {
        PlayerData playerData = getPlayerData(ownerUUID);
        return playerData.getVaultAmounts().getTotalVaults();
    }

    @Override
    public int getTotalVaults(Material material, UUID uuid) {
        return ResourceVaults.getPlayerData(uuid).getVaultsByMaterial(material).size();
    }

    @Override
    public PlayerData getOwner(VaultInstance vault) {
        return ResourceVaults.getPlayerData(vault.getMetadata().getOwnerUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof VaultManager that)) return false;

        return new EqualsBuilder().append(storedPlugin, that.storedPlugin).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(storedPlugin).toHashCode();
    }
}