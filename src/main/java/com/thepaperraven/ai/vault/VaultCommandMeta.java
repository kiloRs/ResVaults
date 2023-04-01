package com.thepaperraven.ai.vault;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.utils.InventoryUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@Getter
public class VaultCommandMeta {
    @NotNull
    private Material allowedMaterial = Material.WHEAT;
    private final UUID ownerUUID;
    private final int vaultIndex;
    private final boolean valid = true;

    public static VaultCommandMeta getMeta(String id, UUID uuid){
        Material material = Material.matchMaterial(id);
        if (material== null){
            ResourceVaults.error("Error w/ Material.");
            return null;
        }
        if (InventoryUtil.isValidMaterial(material)){
            return new VaultCommandMeta(material,uuid,PlayerData.get(uuid).getNextIndex());
        }
        return null;
    }
    public VaultCommandMeta(@NotNull Material allowedMaterial, @NotNull UUID ownerUUID, int vaultIndex) {
        this.allowedMaterial = allowedMaterial;
        this.ownerUUID = ownerUUID;
        this.vaultIndex = vaultIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VaultCommandMeta that = (VaultCommandMeta) o;
        return vaultIndex == that.vaultIndex && allowedMaterial == that.allowedMaterial && Objects.equals(ownerUUID, that.ownerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedMaterial, ownerUUID, vaultIndex);
    }
}
