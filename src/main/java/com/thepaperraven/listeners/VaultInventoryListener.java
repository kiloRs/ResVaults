package com.thepaperraven.listeners;


import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.PDC;
import com.thepaperraven.ai.vault.Vault;
import com.thepaperraven.utils.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VaultInventoryListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        // Check if clicked inventory is a VaultInventory
        if (!(topInventory.getHolder() instanceof VaultInventory vaultInventory)) {
            return;
        }

        ResourceVaults.error("VaultInventory Click: ");
        PDC container = vaultInventory.getContainer();
        Material allowedMaterial = container.getMaterialKey();

        ItemStack cursorItem = event.getCursor();
        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
            // Cancel cursor to vault item move if material doesn't match allowed material
            if (cursorItem.getType() != allowedMaterial) {
                event.setCancelled(true);
                return;
            }
        }

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && currentItem.getType() != Material.AIR) {
            // Cancel shift click from another inventory to vault if material doesn't match allowed material
            if (event.isShiftClick() && currentItem.getType() != allowedMaterial) {
                event.setCancelled(true);
                return;
            }

            // Cancel vault to cursor item move if material doesn't match allowed material
            if (!event.isShiftClick() && event.getClick().isCreativeAction() && currentItem.getType() != allowedMaterial) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Container container){
            if (PDC.get(container).hasKeys()){
                Bukkit.getLogger().info("Close event in VaultInventory's Container detected!");
            }
        }
        if (InventoryUtil.isVaultInventory(event.getInventory())) {
            VaultInventory inventory = (VaultInventory) event.getInventory().getHolder();
            // Handle VaultInventory close event here
            Bukkit.getLogger().info("Close event in VaultInventory detected!");
        }
    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (InventoryUtil.isVaultInventory(event.getInventory())) {
            VaultInventory inventory = (VaultInventory) event.getInventory().getHolder();
            // Handle VaultInventory creative event here
            Bukkit.getLogger().info("Creative event in VaultInventory detected!");
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (InventoryUtil.isVaultInventory(event.getInventory())) {
            VaultInventory inventory = (VaultInventory) event.getInventory().getHolder();
            // Handle VaultInventory drag event here
            Bukkit.getLogger().info("Drag event in VaultInventory detected!");

            if (event.getCursor() != null && inventory != null){
            if (event.getCursor().getType() != inventory.getContainer().getMaterialKey()){
                event.setResult(Event.Result.DENY);
            }
        }
    }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (InventoryUtil.isVaultInventory(event.getDestination())) {
            VaultInventory inventory = (VaultInventory) event.getDestination().getHolder();

            if (inventory == null){
                ResourceVaults.log("Missing Inventory on MoveItemEvent");
                return;
            }
            boolean matchingMaterial = event.getItem().getType() == inventory.getContainer().getMaterialKey();
            // Handle VaultInventory move item event here
            Bukkit.getLogger().info("Move item event in VaultInventory detected!");

            if (!matchingMaterial){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Container x){
            PDC p = new PDC(x);

            ResourceVaults.log("Checking for Keys...");
            if (p.hasKeys()){
                if (p.getOwner()==event.getPlayer().getUniqueId()){
                    Vault vault = PlayerData.get(p.getOwner()).getVaults().get(p.getVaultIndex());

                    if (vault == null){
                        ResourceVaults.log("No Vault on Open Event in Error Causign Way (Has Keys)");
                        return;
                    }
                    event.setCancelled(true);
                    vault.getVaultInventory().open();
                    ResourceVaults.log("Opening VaultInventory");
                    return;
                }
                ResourceVaults.log("Error UUID!");
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (InventoryUtil.isVaultInventory(event.getInventory())) {
            VaultInventory inventory = (VaultInventory) event.getInventory().getHolder();

            if (inventory == null){
                ResourceVaults.error("No VaultInventory Instance Available!");
                return;
            }
            if (inventory.getContainer().hasKeys()){
                if (inventory.getContainer().getOwner()==event.getPlayer().getUniqueId()){
                    Bukkit.getLogger().info("Open event in VaultInventory detected with valid owner!!");

                    return;
                }
                event.setCancelled(true);
                ResourceVaults.error("Error 4 Player Match");
                return;
            }
            // Handle VaultInventory open event here
            Bukkit.getLogger().info("Open event in VaultInventory detected w no keys!");
        }
    }
}
