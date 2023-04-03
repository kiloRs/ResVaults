package com.thepaperraven.ai.vault;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ContainerType;
import com.thepaperraven.VaultManager;
import com.thepaperraven.config.VaultKeys;
import com.thepaperraven.utils.LocationUtils;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

import static com.thepaperraven.config.VaultKeys.*;

@Getter
public class PDC {
    private final Container left;
    private Chest right = null;
    private final InventoryHolder blockHolder;
    private final boolean valid = true;
    private final boolean signsExisting = false;
    private ContainerType containerType;


    public static PDC getPDCOfDoubleChest(@NotNull Location one,@NotNull Location two){
        if (one.toBlockLocation().getBlock().getState() instanceof Chest container && two.toBlockLocation().getBlock().getState() instanceof Chest chest){
            if (container.getBlockInventory().getHolder() instanceof DoubleChest doubleChest && chest.getBlockInventory().getHolder() instanceof Chest chest1){
                return new PDC(doubleChest);
            }
        }
        return null;
    }
    public PDC(@NotNull InventoryHolder holder) {
        if (holder instanceof DoubleChest doubleChest) {
            this.blockHolder = doubleChest;
            this.left = ((Chest) doubleChest.getLeftSide().getInventory().getLocation().getBlock().getState());
            if (doubleChest.getRightSide() != null) {
                this.right = ((Chest) doubleChest.getRightSide().getInventory().getLocation().getBlock().getState());
            }
        } else if (holder instanceof Chest chestInventoryHolder) {
            this.blockHolder = chestInventoryHolder;
            this.left = ((Chest) chestInventoryHolder.getBlock().getState());
            this.containerType = ContainerType.CHEST;
        } else if (holder instanceof Container container) {
            this.blockHolder = container;
            this.left = ((Container) container.getBlock().getState());
        } else {
            throw new RuntimeException("Invalid InventoryHolder for PDContainer!");
        }
    }

    public static PDC get(Container container) {
        return new PDC(container);
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
    public void setSignsExisting(boolean exists) {
        left.getPersistentDataContainer().set(getSignsKey(), DataType.BOOLEAN, exists);
        if (hasSecondChest()) {
            right.getPersistentDataContainer().set(getSignsKey(), DataType.BOOLEAN, exists);
        }
    }

    public boolean hasSigns() {
//        boolean hasSigns = left.getPersistentDataContainer().has(getSignsKey(), DataType.BOOLEAN);
//        if (hasSecondChest()) {
//            hasSigns &= right.getPersistentDataContainer().has(getSignsKey(), DataType.BOOLEAN)
//                    && right.getPersistentDataContainer().get(getSignsKey(), DataType.BOOLEAN) == left.getPersistentDataContainer().get(getSignsKey(), DataType.BOOLEAN);
//        }
//        return hasSigns ||;

        return VaultManager.hasResourcesSign(left.getBlock());

    }

    public boolean signsExist() {
        return left.getPersistentDataContainer().getOrDefault(getSignsKey(), DataType.BOOLEAN, false);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isDoubleChest() {
        return blockHolder instanceof DoubleChest;
    }

    public Chest getRightChest() {
        return right;
    }

    public void setRightChest(Chest chest) {
        right = chest;
    }

    public boolean equals(Object other) {
        if (!(other instanceof PDC pdc)) {
            return false;
        }
        return new EqualsBuilder()
                .append(left.getLocation(), pdc.left.getLocation())
                .append(right == null ? null : right.getLocation(), pdc.right == null ? null : pdc.right.getLocation())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(left.getLocation())
                .append(right == null ? null : right.getLocation())
                .toHashCode();
    }


    public void unlock(){
        left.setLock(null);
        if (hasSecondChest()){
            right.setLock(null);
        }
    }
    public void update() {
        left.setLock(getOwner().toString());
        left.update();
        if (hasSecondChest()){
            right.setLock(getOwner().toString());
            right.update();
        }
    }

    public void setCreatedDate(long createdDate) {
        this.left.getPersistentDataContainer().set(VaultKeys.getCreatedDateKey(),DataType.DATE, new Date(createdDate));
        if (hasSecondChest()){
            this.right.getPersistentDataContainer().set(VaultKeys.getCreatedDateKey(),DataType.DATE,new Date(createdDate));
        }
    }
}