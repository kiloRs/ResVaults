package com.thepaperraven.ai.gui;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.PDC;
import com.thepaperraven.ai.vault.Vault;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class VaultInventory implements InventoryHolder {
    private final Inventory inventory;
    private final PDC container;
    private final Map<Integer,ItemStack> cache = new HashMap<Integer, ItemStack>();
    private final boolean valid = true;


    public VaultInventory(Vault vault) {
        this.inventory = Bukkit.createInventory(this, getContainer().hasSecondChest()?54:27, "Vault " + vault.getVaultIndex());
        this.container = vault.getContainer();
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
        Material allowedMaterial = container.getMaterialKey();
        ItemStack itemStack = new ItemStack(allowedMaterial, amount);

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack currentStack = inventory.getItem(i);
            if (currentStack == null || currentStack.getType() == Material.AIR) {
                // Add item to empty slot
                inventory.setItem(i, itemStack);
                return overflowAmount;
            } else if (currentStack.getType() == allowedMaterial && currentStack.getAmount() < allowedMaterial.getMaxStackSize()) {
                int spaceLeft = allowedMaterial.getMaxStackSize() - currentStack.getAmount();
                if (amount <= spaceLeft) {
                    // The entire stack will fit into the current slot
                    currentStack.setAmount(currentStack.getAmount() + amount);
                    return overflowAmount;
                } else {
                    // Only part of the stack will fit into the current slot
                    currentStack.setAmount(allowedMaterial.getMaxStackSize());
                    amount -= spaceLeft;
                }
            }
        }

        // If there is any amount left, it means the inventory is full
        return amount;
    }

    public int getRemainingAmountOfFreeSlots(){
        return getRemainingAmountOfFreeSpace() / inventory.getMaxStackSize();
    }
    public int getRemainingAmountOfFreeSpace(){
        return getMaxSize() - getCount();
    }
    public int getMaxSize(){
        return inventory.getSize() * inventory.getMaxStackSize();
    }
    public int getCount(){
        int count = 0;
        cache.clear();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null){
                continue;
            }
            if (container.getMaterialKey() != itemStack.getType()){
                inventory.remove(itemStack);
                continue;
            }
            ++count;
            cache.put(i,itemStack);
            }
            return count;
    }

    public void open(){
        if (container.hasOwner()) {
            UUID owner = container.getOwner();
            Bukkit.getPlayer(owner).openInventory(getInventory());
        }
    }
    public void close(){
        UUID owner = container.getOwner();
        Bukkit.getPlayer(owner).closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }

    public static void open(Player player, int x){
        Vault vault = PlayerData.get(player.getUniqueId()).getVaults().get(x);
        if (vault == null){
            ResourceVaults.error("No Open Allowed");
            return;
        }
        vault.getVaultInventory().open();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VaultInventory that = (VaultInventory) o;

        return new EqualsBuilder().appendSuper(true).append(inventory, that.inventory).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(inventory).toHashCode();
    }
}

