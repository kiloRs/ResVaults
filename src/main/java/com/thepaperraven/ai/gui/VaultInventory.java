package com.thepaperraven.ai.gui;

import com.thepaperraven.ai.Vault;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public class VaultInventory implements InventoryHolder {
    private final Inventory inventory;
    @Getter
    private final Vault vault;
    @Getter
    private final Location chestLocation;


    public VaultInventory(Vault vaultInstance) {
        this.vault = vaultInstance;
        this.chestLocation = vault.getMainChest().getLocation();

        BlockState chestBlock = chestLocation.getBlock().getState();
        if (chestBlock instanceof Chest chest) {
            this.inventory = chest.getInventory();
        } else if (chestBlock instanceof DoubleChest doubleChest) {
            this.inventory = doubleChest.getInventory();
        } else {
            throw new IllegalArgumentException("Block at " + chestLocation + " is not a chest");
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public boolean canAdd(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return true;
        }

        int maxStackSize = itemStack.getMaxStackSize();
        int amount = itemStack.getAmount();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                return true;
            }

            if (item.getType() == itemStack.getType() && item.getAmount() + amount <= maxStackSize) {
                return true;
            }
        }

        return false;
    }

    public int add(int amount) {
        int overflowAmount = 0;
        Material allowedMaterial = vault.getMetadata().getAllowedMaterial();
        ItemStack itemStack = new ItemStack(allowedMaterial, amount);

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack currentStack = inventory.getItem(i);
            if (currentStack == null || currentStack.getType() == Material.AIR) {
                // Add item to empty slot
                inventory.setItem(i, itemStack);
                return overflowAmount;
            } else if (currentStack.getType() == allowedMaterial && currentStack.getAmount() < allowedMaterial.getMaxStackSize()) {
                // Add item to existing stack
                int maxStackSize = currentStack.getMaxStackSize();
                int freeSpace = maxStackSize - currentStack.getAmount();
                if (freeSpace >= amount) {
                    currentStack.setAmount(currentStack.getAmount() + amount);
                    return overflowAmount;
                } else {
                    currentStack.setAmount(maxStackSize);
                    amount -= freeSpace;
                }
            }
        }

        // If we reach this point, the inventory is full
        if (amount > 0) {
            overflowAmount = amount;
        }

        return overflowAmount;
    }

    public void setSlot(int slot, int amount) {
        Material allowedMaterial = vault.getMetadata().getAllowedMaterial();
        ItemStack itemStack = new ItemStack(allowedMaterial, amount);
        inventory.setItem(slot, itemStack);
    }

    public void remove(int amount) {
        Material allowedMaterial = vault.getMetadata().getAllowedMaterial();
        ItemStack itemStack = new ItemStack(allowedMaterial, amount);
        inventory.removeItem(itemStack);
    }

}
