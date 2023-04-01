package com.thepaperraven.ai.player;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.config.PlayerConfiguration;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

@Getter
public class PlayerData{
    private final Map<Integer, VaultInstance> vaults;
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
        this.vaults = new HashMap<>();
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
            public int getTotalVaults(Material material) {
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
        return vaults.getOrDefault(index,null);
    }

    public boolean hasVault(int index) {
        return vaults.containsKey(index);
    }

    public boolean hasVault(VaultInstance i){
        return vaults.containsValue(i);
    }
    public int getNextIndex() {
        return vaults.size() + 1;
    }

    /**
     * @param index The VaultMetadata index (note: always starts at 1, instead of 0 which is natural, so please enter the Vaults internal index.
     */
    public void removeVault(int index) {
        if (index>0) {
            index = index - 1;
        }
        if (vaults.containsKey(index)) {
            VaultInstance vault = vaults.remove(index);
            vault.removeFromBlock();
            ResourceVaults.log("Removing Vault: " + vault.getMetadata().getVaultIndex()+ " from " + Bukkit.getPlayer(vault.getMetadata().getOwnerUUID()).getName());
        }
    }
    public File getFile(){
        return new PlayerConfiguration(this.uuid).getFile();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PlayerData that = (PlayerData) o;

        return new EqualsBuilder().append(uuid, that.uuid).append(this.vaults,that.vaults).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).append(this.vaults).toHashCode();
    }


}
