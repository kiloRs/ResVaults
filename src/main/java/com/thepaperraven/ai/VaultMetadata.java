package com.thepaperraven.ai;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ResourceVaults;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

@Getter
public class VaultMetadata {
    private final Material allowedMaterial;
    private final UUID ownerUUID;
    private final int vaultIndex;

    public VaultMetadata(Material material, PlayerData playerData, int index) {
        this(material,playerData.getUuid(),index);
    }

    public static VaultMetadata get(Player player, int vaultIndex){
        return new VaultMetadata(Material.WHEAT,player.getUniqueId(),vaultIndex);
    }
    public static VaultMetadata get(Material material, Player player, int vaultIndex){
        return new VaultMetadata(material,player.getUniqueId(),vaultIndex);
    }
    public VaultMetadata(Material allowedMaterial, UUID ownerUUID, int vaultIndex) {
        this.allowedMaterial = allowedMaterial;
        this.ownerUUID = ownerUUID;
        this.vaultIndex = vaultIndex;
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

    public boolean isMatchingMaterial(ItemStack itemStack) {
        return itemStack.getType() == this.allowedMaterial;
    }

    public void saveToPDC(Block block) {
        PersistentDataContainer pdc = block.getState() instanceof TileState tileState ? tileState.getPersistentDataContainer() : null;
        if (pdc == null) {
            ResourceVaults.log("No Container to Save: " + block.getLocation().toBlockLocation());
            return;
        }
        pdc.set(VaultKeys.getOwnerKey(), DataType.UUID, getOwnerUUID());
        pdc.set(VaultKeys.getMaterialTypeKey(), PersistentDataType.STRING, allowedMaterial.getKey().getKey());
        pdc.set(VaultKeys.getIndexKey(), PersistentDataType.INTEGER, vaultIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof VaultMetadata that)) return false;

        return new EqualsBuilder().append(getVaultIndex(), that.getVaultIndex()).append(getAllowedMaterial(), that.getAllowedMaterial()).append(getOwnerUUID(), that.getOwnerUUID()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getAllowedMaterial()).append(getOwnerUUID()).append(getVaultIndex()).toHashCode();
    }

}
