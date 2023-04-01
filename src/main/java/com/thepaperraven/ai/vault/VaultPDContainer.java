package com.thepaperraven.ai.vault;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.utils.LocationUtils;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.thepaperraven.config.VaultKeys.*;
import static com.thepaperraven.utils.InventoryUtil.createSign;
import static com.thepaperraven.utils.InventoryUtil.getFacing;

@Getter
public class VaultPDContainer{
    private final Container left;
    private Chest right = null;
    private final InventoryHolder blockHolder;
    private final boolean valid = true;
    private final boolean signsExisting = false;


    public VaultPDContainer(@NotNull InventoryHolder holder) {
        if (holder instanceof DoubleChest doubleChest) {
            this.blockHolder = doubleChest;
            this.left = ((Chest) doubleChest.getLeftSide().getInventory().getLocation().getBlock().getState());
            if (doubleChest.getRightSide() != null) {
                this.right = ((Chest) doubleChest.getRightSide().getInventory().getLocation().getBlock().getState());
            }
        } else if (holder instanceof Chest chestInventoryHolder) {
            this.blockHolder = chestInventoryHolder;
            this.left = ((Chest) chestInventoryHolder.getBlock().getState());
        } else if (holder instanceof Container container) {
            this.blockHolder = container;
            this.left = ((Container) container.getBlock().getState());
        } else {
            throw new RuntimeException("Invalid InventoryHolder for PDContainer!");
        }
    }

    public static VaultPDContainer get(Container container) {
        return new VaultPDContainer(container);
    }

    public void setMaterialKey(Material material) {
        left.getPersistentDataContainer().set(getMaterialTypeKey(), PersistentDataType.STRING, material.name());
        if (hasSecondChest()) {
            right.getPersistentDataContainer().set(getMaterialTypeKey(), PersistentDataType.STRING, material.name());
        }
    }

    public boolean hasMaterialKey() {
        boolean hasMaterial = left.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING);
        if (hasSecondChest()) {
            hasMaterial &= right.getPersistentDataContainer().has(getMaterialTypeKey(), PersistentDataType.STRING)
                    && right.getPersistentDataContainer().get(getMaterialTypeKey(), PersistentDataType.STRING).equals(left.getPersistentDataContainer().get(getMaterialTypeKey(), PersistentDataType.STRING));
        }
        return hasMaterial;
    }

    public Material getMaterialKey() {
        if (!hasMaterialKey()) {
            return null;
        }
        return Material.getMaterial(left.getPersistentDataContainer().getOrDefault(getMaterialTypeKey(),
                PersistentDataType.STRING, "WHEAT"));
    }

    public void setOwner(UUID owner) {
        left.getPersistentDataContainer().set(getOwnerKey(), DataType.UUID, owner);
        if (hasSecondChest()) {
            right.getPersistentDataContainer().set(getOwnerKey(), DataType.UUID, owner);
        }
    }

    public boolean hasOwner() {
        boolean hasOwner = left.getPersistentDataContainer().has(getOwnerKey(), DataType.UUID);
        if (hasSecondChest()) {
            hasOwner &= right.getPersistentDataContainer().has(getOwnerKey(), PersistentDataType.STRING)
                    && right.getPersistentDataContainer().get(getOwnerKey(), PersistentDataType.STRING).equals(left.getPersistentDataContainer().get(getOwnerKey(), PersistentDataType.STRING));
        }
        return hasOwner;
    }

    public UUID getOwner() {
        return left.getPersistentDataContainer().get(getOwnerKey(), DataType.UUID);
    }

    public void setVaultIndex(int index) {
        left.getPersistentDataContainer().set(getIndexKey(), PersistentDataType.INTEGER, index);
        if (hasSecondChest()) {
            right.getPersistentDataContainer().set(getIndexKey(), PersistentDataType.INTEGER, index);
        }
    }

    public boolean hasVaultIndex() {
        boolean hasIndex = left.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER);
        if (hasSecondChest()) {
            hasIndex &= right.getPersistentDataContainer().has(getIndexKey(), PersistentDataType.INTEGER)
                    && right.getPersistentDataContainer().get(getIndexKey(), PersistentDataType.INTEGER).intValue() == left.getPersistentDataContainer().get(getIndexKey(), PersistentDataType.INTEGER).intValue();
        }
        return hasIndex;
    }


    public int getVaultIndex() {
        if (!hasVaultIndex()) {
            return -1;
        }
        Integer leftIndex = left.getPersistentDataContainer().getOrDefault(getIndexKey(), PersistentDataType.INTEGER, 0);
        Integer rightIndex = right == null ? leftIndex : right.getPersistentDataContainer().getOrDefault(getIndexKey(), PersistentDataType.INTEGER, 0);
        return rightIndex.intValue() == leftIndex.intValue() ? leftIndex : 0;
    }

    public Inventory getInventory() {
        return left.getInventory();
    }

    public InventoryHolder getInventoryHolder() {
        return blockHolder;
    }

    public boolean hasSecondChest() {
        return right != null && right.getBlock().getState() instanceof Chest chest;

    }

    public boolean hasKeys() {
        return hasKeys(LocationUtils.isDoubleChest(left.getBlock()));
    }

    public boolean hasKeys(boolean requireDoubleChest) {
        if (hasSecondChest() && requireDoubleChest) {
            return hasOwner() && hasMaterialKey() && hasVaultIndex();
        }
        return hasMaterialKey() && hasOwner() && hasVaultIndex();
    }
    public static VaultPDContainer getVaultContainerByBlock(Player player, Block block, boolean checkRegistered) {
        if (block.getState() instanceof InventoryHolder holder && !checkRegistered) {
            return new VaultPDContainer(holder);
        } else if (checkRegistered) {
            for (VaultInstance vaultInstance : PlayerData.get(player.getUniqueId()).getVaults().values()) {
                if (block.getState() instanceof Container container) {
                    if (vaultInstance.getContainer().hasSecondChest() && !(container.getInventory().getHolder() instanceof DoubleChest doubleChest)) {
                        continue;
                    }
                    if (container.getInventory().equals(vaultInstance.getContainer().getInventory())) {
                        return vaultInstance.getContainer();
                    }
                    continue;
                }
            }
        }
        return null;
    }


    public void saveToBlock(@NotNull VaultCommandMeta vaultCommandMetaMetadata){
        setVaultIndex(vaultCommandMetaMetadata.getVaultIndex());
        setOwner(vaultCommandMetaMetadata.getOwnerUUID());
        setMaterialKey(vaultCommandMetaMetadata.getAllowedMaterial());

        BlockFace f = getFacing(left.getBlock());
        BlockFace facing = f !=null? f :BlockFace.NORTH;

        if (hasSecondChest()){
            createSign(right.getBlock().getRelative(facing), vaultCommandMetaMetadata.getAllowedMaterial().name(), Bukkit.getPlayer(vaultCommandMetaMetadata.getOwnerUUID()).getName(), vaultCommandMetaMetadata.getVaultIndex(),facing);
        }
        createSign(left.getBlock().getRelative(facing), vaultCommandMetaMetadata.getAllowedMaterial().name(), Bukkit.getPlayer(vaultCommandMetaMetadata.getOwnerUUID()).getName(), vaultCommandMetaMetadata.getVaultIndex(),facing);

        }
    public void removeFromBlock() {
        left.getPersistentDataContainer().remove(getOwnerKey());
        left.getPersistentDataContainer().remove(getIndexKey());
        left.getPersistentDataContainer().remove(getMaterialTypeKey());
        if (hasSecondChest()){
            right.getPersistentDataContainer().remove(getOwnerKey());
            right.getPersistentDataContainer().remove(getIndexKey());
            right.getPersistentDataContainer().remove(getMaterialTypeKey());
        }


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VaultPDContainer that = (VaultPDContainer) o;

        return new EqualsBuilder().append(left, that.left).append(right, that.right).append(blockHolder, that.blockHolder).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(left).append(right).append(blockHolder).toHashCode();
    }
}