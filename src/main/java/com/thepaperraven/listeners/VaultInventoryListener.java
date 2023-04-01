package com.thepaperraven.listeners;


import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.ai.vault.VaultPDContainer;
import com.thepaperraven.utils.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import static com.thepaperraven.utils.InventoryUtil.acceptsMaterial;
import static com.thepaperraven.utils.InventoryUtil.isVaultInventory;

public class VaultInventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Bukkit.getLogger().info("Click event in VaultInventory detected!");

        if (event.getCurrentItem() == null){
            Bukkit.getLogger().info("No Current Item!!");
            return;
        }
        boolean is = isVaultInventory(event.getInventory());
        if (event.getCursor() != null){
            Bukkit.getLogger().info("Item In Cursor!!");
            if (is) {
                BlockState state = event.getInventory().getLocation().getBlock().getState();
                if (!(state instanceof Container container)){
                    Bukkit.getLogger().info("No Container System");
                    event.setCancelled(true);
                    return;
                }
                boolean accepts = acceptsMaterial((container), event.getCursor().getType());
                if (accepts) {
                    Bukkit.getLogger().severe("Accepts Cursor!");
                    return;
                }
                event.setCancelled(true);
            }
            else {
                ResourceVaults.log("Not an Inventory from Vault!");
            }
        }
        if (is) {
            VaultInventory inventory = (VaultInventory) event.getInventory().getHolder();
            if (event.isShiftClick()){
                Material type = event.getCurrentItem().getType();
                if (acceptsMaterial(inventory,type)){
                    Bukkit.getLogger().info("Valid Material!");
                    return;
                }
                event.setCancelled(true);
                return;
            }
            if (acceptsMaterial(inventory,event.getCurrentItem().getType())){
                Bukkit.getLogger().info("Valid Material!");
                return;
            }
            event.setCancelled(true);
            // Handle VaultInventory click event here
            Bukkit.getLogger().info("Click event in VaultInventory detected!");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
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
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (InventoryUtil.isVaultInventory(event.getInventory())) {
            VaultInventory inventory = (VaultInventory) event.getInventory().getHolder();
            // Handle VaultInventory interact event here
            Bukkit.getLogger().info("Interact event in VaultInventory detected!");
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (InventoryUtil.isVaultInventory(event.getDestination())) {
            VaultInventory inventory = (VaultInventory) event.getDestination().getHolder();
            // Handle VaultInventory move item event here
            Bukkit.getLogger().info("Move item event in VaultInventory detected!");
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Container x){
            VaultPDContainer p = new VaultPDContainer(x);

            if (p.hasKeys()){
                if (p.getOwner()==event.getPlayer().getUniqueId()){
                    VaultInstance vault = PlayerData.get(p.getOwner()).getVault(p.getVaultIndex());

                    if (vault == null){
                        throw new RuntimeException("Vault error");
                    }
                    event.setCancelled(true);
                    vault.getInventory().open();
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
                return;
            }
            if (inventory.getContainer().hasKeys()){
                if (inventory.getContainer().getOwner()==event.getPlayer().getUniqueId()){
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
