package com.thepaperraven.ai;


import com.thepaperraven.ai.gui.VaultInventory;
import org.bukkit.block.Block;

import java.util.List;

public interface VaultInstance extends VaultLock {
    VaultInventory getVaultInventory();
    VaultMetadata getMetadata();
    boolean isActive();
    boolean hasSign();
    boolean hasOwner();
    boolean isLocked();

    @Override
    List<Block> lockedBlocks();

    /**
     * Gets the exact amount of items within the Vault (count the amount of itemstacks per slot).
     *
     * @return the amount of items within the Vault
     */
    public int getAmount();

    /**
     * Adds the specified amount of items to the VaultInventory, creating new ItemStack(vault.getMetadata().getAllowedMaterial)
     * and adding them to the first not-max-size slots, until the inventory is full.
     *
     * @param amount the amount of items to add
     * @return the amount of items overflowing from the inventory
     */
    public int add(int amount);
}


//    public default boolean equals(VaultInstance other){
//        return this.getOwnerUUID().equals(other.getOwnerUUID())&&this.getChestLocation1().toBlockLocation().equals(other.getChestLocation1().toBlockLocation());
//    }
//}