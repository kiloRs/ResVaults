package com.thepaperraven.data.gui;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.data.vault.Vault;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
@Data

public class VaultHolder implements InventoryHolder {

    private final Map<Integer, ItemStack> cache;
    private final Vault vault;
    private final @NotNull(exception = RuntimeException.class,value = "Major Missing Inventory!") Inventory inventory;
    private boolean rejectCache = false;
    private int saves = 0;

    public VaultHolder(Vault vault) {
        this.vault = vault;
        this.cache = new HashMap<>();
        this.inventory = Bukkit.createInventory(this, vault.getCapacity(), ChatColor.BLUE + "Vault of" + vault.getMaterial().name() + " (" + vault.getIndex() + ")!");
        this.rejectCache = !vault.isRegisteredToMap();

    }

    public Vault getVault() {
        return vault;
    }

    public Map<Integer, ItemStack> getCache() {
        return cache;
    }

    @Override
    public @NotNull(exception = RuntimeException.class,value = "Missing Inventory?") Inventory getInventory() {
        return inventory;
    }


    public void saveToCache() {
        if (rejectCache){
            ResourceVaults.log("Rejecting Cache! - Vault Instance Not Registered Yet!");
            return;
        }
        Inventory inventory = vault.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                cache.put(i, item.clone());
            } else {
                cache.remove(i);
            }
        }
        ++saves;
    }

    public void loadtoInventory() {
        if (!rejectCache){
            ResourceVaults.log("Rejecting Loading Cache! - Vault Instance Not Registered Yet!");
        }
        Inventory inventory = vault.getInventory();
        for (Map.Entry<Integer, ItemStack> entry : cache.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
    }

    public void clearSaveCounter(int setNewAmount){
        this.saves = setNewAmount;
    }

}
