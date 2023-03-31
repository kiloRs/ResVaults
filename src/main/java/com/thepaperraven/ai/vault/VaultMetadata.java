package com.thepaperraven.ai.vault;

import com.thepaperraven.ai.player.PlayerData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@SerializableAs("metadata")
@Getter
public class VaultMetadata implements ConfigurationSerializable, Invalidatable {
    @NotNull
    private Material allowedMaterial = Material.WHEAT;
    private UUID ownerUUID;
    private int vaultIndex;
    private boolean valid = true;

    public VaultMetadata(Material material, PlayerData playerData, int index) {
        this(material,playerData.getUuid(),index);
    }

    public VaultMetadata(@NotNull Material allowedMaterial, @NotNull UUID ownerUUID, int vaultIndex) {
        this.allowedMaterial = allowedMaterial;
        this.ownerUUID = ownerUUID;
        this.vaultIndex = vaultIndex;;
    }

    public boolean isOwner(UUID uuid){
        Player player = Bukkit.getPlayer(uuid);
        if (player == null){
            return false;
        }
        return isOwner(player);
    }
    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(this.getOwnerUUID());
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> m = new HashMap<>();
        m.put("index",vaultIndex);
        m.put("owner",ownerUUID);
        m.put("material",allowedMaterial);
        return m;
    }

    public void syncTo(VaultPDContainer container){
        container.setOwner(getOwnerUUID());
        container.setVaultIndex(getVaultIndex());
        container.setMaterialKey(getAllowedMaterial());
    }

    public void sync(){
        syncFrom(PlayerData.get(ownerUUID).getVault(vaultIndex).getContainer());
    }
    public void syncFrom(VaultPDContainer c){
        if (c.hasVaultIndex()) {
            vaultIndex = c.getVaultIndex();
        }
        if (c.hasMaterialKey()){
            allowedMaterial = c.getMaterialKey();
        }
        if (c.hasOwner()){
            ownerUUID = c.getOwner();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VaultMetadata that = (VaultMetadata) o;
        return vaultIndex == that.vaultIndex && allowedMaterial == that.allowedMaterial && Objects.equals(ownerUUID, that.ownerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedMaterial, ownerUUID, vaultIndex);
    }

    @Override
    public void invalidate(){
        this.vaultIndex = -1;
        this.allowedMaterial = Material.AIR;
        this.ownerUUID = null;
        this.valid = false;;
    }

    @Override
    public boolean isValid() {
        return valid;
    }
}
