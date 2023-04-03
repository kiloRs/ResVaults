package com.thepaperraven.ai.player;

import com.thepaperraven.ai.vault.Vault;
import com.thepaperraven.config.PlayerConfiguration;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PlayerData {

    private final UUID playerUUID;
    private final TotalHandler totalHandler;
    private final Map<Integer, Vault> vaults;
    private final PlayerConfiguration config;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.vaults = new HashMap<>();
        this.config = new PlayerConfiguration(this.playerUUID);

        this.totalHandler = new TotalHandler() {
            @Override
            public int getTotalItems() {
                int total = 0;
                for (Map.Entry<Integer, Vault> entry : vaults.entrySet()) {
                    Vault vault = entry.getValue();
                    total = +vault.getAmount();
                }
                return total;
            }

            @Override
            public int getTotalItems(Material material) {
                int total = 0;
                for (Map.Entry<Integer, Vault> entry : vaults.entrySet()) {
                    if (entry.getValue().getMaterial()==material) {
                        Vault vault = entry.getValue();
                        total = +vault.getAmount();
                    }
                }
                return total;
            }

            @Override
            public int getTotalVaults() {
                return vaults.size();
            }

            @Override
            public int getTotalVaults(Material material) {
                int total = getTotalVaults();
                for (Map.Entry<Integer, Vault> entry : vaults.entrySet()) {
                    Vault vault = entry.getValue();
                    if (vault.getMaterial() == material) {
                        continue;
                    }
                    total--;
                }
                return total;
            }
        };
    }

    public static PlayerData get(UUID owner) {
        return new PlayerData(owner);
    }
    public void addVault(Vault vault) {
        vaults.put(vault.getVaultIndex(), vault);
    }
    public void removeVault(int index){
        if (!vaults.containsKey(index)) {
            return;
        }
        vaults.remove(index);
    }
    public void removeVaultIfMatches(int index, Vault vault){
        if (vaults.containsKey(index) && vaults.get(index).equals(vault)){
            vaults.remove(index,vault);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PlayerData that = (PlayerData) o;

        return new EqualsBuilder().append(playerUUID, that.playerUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(playerUUID).toHashCode();
    }
}
