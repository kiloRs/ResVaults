package com.thepaperraven.ai;

import com.thepaperraven.ai.events.VaultCreateEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;

public class VaultListener implements Listener {

    private final VaultManager vaultManager;

    public VaultListener(VaultManager vaultManager) {
        this.vaultManager = vaultManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase("[Resources]")) {
            Block attachedBlock = event.getBlock().getRelative(((org.bukkit.material.Sign)event.getBlock().getState().getData()).getAttachedFace());
            if (vaultManager.vaultExists(attachedBlock)) {
                event.setCancelled(true);
                return;
            }

            Material allowedMaterial = Material.WHEAT;
            if (!event.getLine(1).isEmpty()) {
                try {
                    allowedMaterial = Material.valueOf(event.getLine(1).toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid material specified on the second line of the sign
                }
            }

            VaultCreateEvent vaultCreationEvent = new VaultCreateEvent(event.getPlayer(), event.getBlock().getLocation(), attachedBlock.getLocation(), allowedMaterial);
            vaultManager.createVault(vaultCreationEvent);
            if (vaultCreationEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block clickedBlock = event.getClickedBlock();
            Player player = event.getPlayer();

            if (clickedBlock.getState() instanceof Sign) {
                Sign sign = (Sign) clickedBlock.getState();
                if (sign.getLine(0).equalsIgnoreCase("[Resources]")) {
                    Vault vault = vaultManager.getVault(clickedBlock.getLocation());
                    if (vault != null) {
                        event.setCancelled(true);

                        if (vault.getOwnerUUID().equals(player.getUniqueId().toString())) {
                            if (clickedBlock.getRelative(BlockFace.DOWN).getState() instanceof InventoryHolder) {
                                InventoryHolder chest = (InventoryHolder) clickedBlock.getRelative(BlockFace.DOWN).getState();
                                if (vault.getChestLocation2() != null) {
                                    chest.getInventory().setContents(((DoubleChestInventory) vaultManager.getDoubleChestInventory(vault)).getContents());
                                } else {
                                    chest.getInventory().setContents(vaultManager.getChestInventory(vault).getContents());
                                }
                                player.openInventory(chest.getInventory());
                            }
                        }
                    }
                }
            }
        }
    }
}
