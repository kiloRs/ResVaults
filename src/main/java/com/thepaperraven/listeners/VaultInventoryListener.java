package com.thepaperraven.listeners;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.data.gui.VaultHolder;
import com.thepaperraven.data.player.PlayerDataManager;
import com.thepaperraven.data.vault.Vault;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class VaultInventoryListener implements Listener {

    private final PlayerDataManager playerDataManager;

    public VaultInventoryListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof VaultHolder) {
            Vault vault = ((VaultHolder) holder).getVault();
            if (vault.isLocked()) {
                UUID lockOwner = UUID.fromString(vault.getLock());
                if (!lockOwner.equals(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }
            vault.setInventoryState(true);
            ResourceVaults.log("Saving Vault to Player: " + event.getPlayer().getName() + " (" + vault.getIndex() + ")");
            playerDataManager.saveVault(event.getPlayer().getUniqueId(),vault);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof VaultHolder vaultHolder) {
            Vault vault = vaultHolder.getVault();
            vault.getHolder().saveToCache();
            ResourceVaults.log("Saving Vault to Player: " + event.getPlayer().getName() + " (" + vault.getIndex() + ")");

            playerDataManager.saveVault(event.getPlayer().getUniqueId(),vault);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof VaultHolder) {
            Vault vault = ((VaultHolder) holder).getVault();
            String invalidMessage = ChatColor.RED + "Invalid Click: Either this is the wrong chest, or the wrong item type!";
            if (vault.isLocked()) {
                UUID lockOwner = UUID.fromString(vault.getLock());
                if (!lockOwner.equals(event.getWhoClicked().getUniqueId())) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(invalidMessage);
                    return;
                }
            }
            if (!event.isShiftClick()) {
                int slot = event.getSlot();
                if (slot >= 0 && slot < vault.getSlots()) {
                    if (event.getCurrentItem() != null) {
                        if (!event.getCurrentItem().getType().equals(vault.getMaterial())) {
                            event.getWhoClicked().sendMessage(invalidMessage);
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        ItemStack cursor = event.getCursor();
                        if (cursor != null) {
                            if (!cursor.getType().equals(vault.getMaterial())) {
                                event.getWhoClicked().sendMessage(invalidMessage);
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            } else {
                int start = event.getRawSlot() < event.getView().getTopInventory().getSize() ? 0 : event.getView().getTopInventory().getSize();
                int end = event.getView().getTopInventory().getSize() + event.getView().getBottomInventory().getSize() - 1;
                for (int i = start; i <= end; i++) {
                    if (i >= 0 && i < vault.getSlots()) {
                        ItemStack item = event.getInventory().getItem(i);
                        if (item != null) {
                            if (!item.getType().equals(vault.getMaterial())) {
                                event.getWhoClicked().sendMessage(invalidMessage);
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

}