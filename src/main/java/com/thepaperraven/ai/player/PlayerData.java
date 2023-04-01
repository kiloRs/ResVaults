package com.thepaperraven.ai.player;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.config.PlayerConfiguration;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

import static com.thepaperraven.ai.player.PlayerDataFileHandler.save;

@Getter
@SerializableAs("playerData")
public class PlayerData implements ConfigurationSerializable{
    private final Map<Integer, VaultInstance> vaults = new HashMap<>();
    @Getter
    private final PlayerDataMathHandler mathHandler;
    private final UUID uuid;
    private final boolean loadOnCreate;
    private final PlayerConfiguration config;
    private final Player player;

    // Other fields and methods for the PlayerData class
    public PlayerData(UUID uuid) {
        this(uuid, true);
    }

    public PlayerData(@NotNull UUID uuid, boolean loadOnCreate) {
        this.uuid = uuid;
        this.loadOnCreate = loadOnCreate;
        this.config = new PlayerConfiguration(uuid);
        this.player = Bukkit.getPlayer(uuid);
        this.mathHandler = new PlayerDataMathHandler() {
            private int total;
            private int totalVaults;
            @Override
            public int getTotalItems() {
                total = 0;
                vaults.forEach((integer, vaultInstance) -> {
                    total +=vaultInstance.getInventory().getCount();
                });
                return total;
            }

            @Override
            public int getTotalItems(Material material) {
                total = 0;
                vaults.forEach((integer, vaultInstance) -> {
                    if (material == vaultInstance.getContainer().getMaterialKey()){
                        total +=vaultInstance.getInventory().getCount();
                    }
                });
                return total;
            }

            @Override
            public int getTotalVaults() {
                totalVaults = vaults.size();
                return totalVaults;
            }

            @Override
            public int getTottalVaults(Material material) {
                totalVaults = 0;
                vaults.forEach((integer, vaultInstance) -> {
                    if (vaultInstance.getContainer().getMaterialKey()==material){
                        totalVaults++;
                    }
                });

                return totalVaults;
            }
        };
    }

    public static PlayerData get(UUID uniqueId) {
        return new PlayerData(uniqueId);
    }


    public VaultInstance getVault(int index) {
        return vaults.get(index);
    }

    public boolean hasVault(int index) {
        return vaults.containsKey(index);
    }

    public boolean hasVault(VaultInstance i){
        return vaults.containsValue(i);
    }
    public int getNextIndex() {
        int index = 1;
        while (vaults.containsKey(index)) {
            index++;
        }
        return index;
    }

    public int getIndexOf(VaultInstance instance) {
        if (!vaults.containsValue(instance)) {
            return 0;
        }
        int returnValue = 0;
        for (Map.Entry<Integer, VaultInstance> entry : vaults.entrySet()) {
            Integer integer = entry.getKey();
            VaultInstance vaultInstance = entry.getValue();
            if (vaultInstance.equals(instance)) {
                returnValue = integer;
                continue;
            }
        }
        if (returnValue > 0) {
            return returnValue;
        }

        ResourceVaults.error("No Index found for " + instance.getMetadata().getOwnerUUID());

        return returnValue;
    }

    public Map<Integer, VaultInstance  > getVaults() {
        return vaults;
    }

    public List<VaultInstance> getVaultsByMaterial(Material material) {
        List<VaultInstance> matching = new ArrayList<>();
        for (Map.Entry<Integer, VaultInstance> entry : vaults.entrySet()) {
            Integer integer = entry.getKey();
            VaultInstance vaultInstance = entry.getValue();
            if (vaultInstance.getMetadata().getAllowedMaterial()==material || vaultInstance.getMetadata().getAllowedMaterial().equals(material)){
                matching.set(integer,  vaultInstance);
            }
        }
        return matching;
    }

    /**
     * @param index The VaultMetadata index (note: always starts at 1, instead of 0 which is natural, so please enter the Vaults internal index.
     */
    public void removeVault(int index, boolean invalidate) {
        if (index>0) {
            index = index - 1;
        }
        if (vaults.containsKey(index)) {
            VaultInstance vault = vaults.remove(index);
            vault.removeFromBlock(true,true);
            if (invalidate){
                vault.invalidate();
                ResourceVaults.log("Invalidating....");
            }
            ResourceVaults.log("Removing Vault: " + vault.getMetadata().getVaultIndex()+ " from " + Bukkit.getPlayer(vault.getMetadata().getOwnerUUID()).getName());
            save(this);
        }
    }
    public File getFile(){
        return new PlayerConfiguration(this.uuid).getFile();
    }
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> vaultsData = new ArrayList<>();

        for (VaultInstance vault : vaults.values()) {
            int indexOf = getIndexOf(vault);
            if (indexOf>0) {
                vaultsData.set(indexOf, vault.serialize());
            }
        }

        data.put("vaults", vaultsData);

        return data;
    }


    public static void registerVault(VaultInstance vault) {
        if (vault.save()) {
            ResourceVaults.log("Registration Complete: " + vault.getMetadata().getVaultIndex());
            save(vault.getOwnerData());
            return;
        }
        ResourceVaults.log("No Registration Occurrence?");
    }
    public void setVaults(Map<Integer, VaultInstance> vaults) {
        this.vaults.putAll(vaults);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PlayerData that = (PlayerData) o;

        return new EqualsBuilder().append(uuid, that.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }

    public boolean register(VaultInstance i) {
        if (vaults.containsKey(i.getMetadata().getVaultIndex())) {
            ResourceVaults.error("PlayerData already contains a storage for " + i.getMetadata().getVaultIndex() + " in " + player.getName() + "'s Map");
            return false;
        }
        vaults.put(i.getMetadata().getVaultIndex(),i);
        return true;
    }

}
