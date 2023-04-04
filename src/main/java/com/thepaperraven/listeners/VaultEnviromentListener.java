package com.thepaperraven.listeners;

import com.thepaperraven.utils.VaultUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class VaultEnviromentListener implements Listener {

    private final VaultUtil vaultUtil;
    public VaultEnviromentListener(VaultUtil vaultUtil) {
        this.vaultUtil = vaultUtil;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (vaultUtil.isVaultSign(block) || vaultUtil.isVaultChest(block)) {
            if (!vaultUtil.isProtected(block)) {
                return;
            }
            if (event.getPlayer().getUniqueId().equals(vaultUtil.getOwner(block))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null && (vaultUtil.isVaultSign(block) || vaultUtil.isVaultChest(block))) {
            if (!vaultUtil.isProtected(block)) {
                return;
            }
            if (!event.getPlayer().getUniqueId().equals(vaultUtil.getOwner(block))) {
                event.setCancelled(true);
            }
        }
    }
}