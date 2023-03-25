package com.thepaperraven.ai;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PlayerData {

    private UUID uuid;
    private Map<Integer, Vault> vaults = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void addVault(Vault vault) {
        vaults.put(vault.getIndex(), vault);
    }

    public void removeVault(Vault vault) {
        vaults.remove(vault.getIndex());
    }

    public Vault getVault(int index) {
        return vaults.get(index);
    }

    public boolean hasVault(int index) {
        return vaults.containsKey(index);
    }

    public boolean hasVaultAt(Location chestLocation) {
        return vaults.values().stream()
                .anyMatch(vault -> vault.getChestLocation().equals(chestLocation));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof PlayerData that)) return false;

        return new EqualsBuilder().append(getUuid(), that.getUuid()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getUuid()).toHashCode();
    }
}
