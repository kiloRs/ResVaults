package com.thepaperraven.listeners;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.PDC;
import com.thepaperraven.ai.vault.Vault;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class VaultSignProtectionListener implements Listener {

    private final Plugin plugin;

    public VaultSignProtectionListener(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VaultSignProtectionListener that = (VaultSignProtectionListener) o;

        return new EqualsBuilder().append(plugin, that.plugin).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(plugin).toHashCode();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null ) {
            return;
        }
        if (block.getState() instanceof Sign sign){
            if (isVault(sign.getBlock())){
                if (isOwner(event.getPlayer(),sign.getBlock())){
                    ResourceVaults.error("Should open Vault?");
                }
                event.setCancelled(true);
                return;
            }
        }
        if (!(block.getState() instanceof Container container)){
            return;
        }
        if (isVault(block) && isOwner(event.getPlayer(), block)) {
            event.setCancelled(true);
            PDC pdc = PDC.get(container);
            Vault vault = PlayerData.get(event.getPlayer().getUniqueId()).getVaults().get(pdc.getVaultIndex());

            vault.getVaultInventory().open();
            ResourceVaults.log("Opening due to interaction...");
        }
    }

    private boolean isOwner(Player player, Block block) {
        if (isVault(block)) {
            UUID owner = PDC.get(((Container) block.getState())).getOwner();

            return player.getUniqueId().equals(owner);

        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST,ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (event.getBlock().getState() instanceof Container container){
            if (isVault(block) && isOwner(event.getPlayer(), block)) {
                if (block.getState() instanceof Sign sign){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Please break the container!");
                    return;
                }
                PDC pdc = PDC.get(container);
                PlayerData.get(event.getPlayer().getUniqueId()).removeVault(pdc.getVaultIndex());
                event.getPlayer().sendMessage(ChatColor.RED + "Removed Vault: " + pdc.getVaultIndex() + " (" + pdc.getMaterialKey().name() + ")");
            }
            else if (!isOwner(event.getPlayer(), block)){
                event.getPlayer().sendMessage(ChatColor.RED + "You do not own this Vault!");
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW,ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        BlockFace againstFace = event.getBlockAgainst().getFace(event.getBlockPlaced());
        if (isVault(block) && isOwner(event.getPlayer(),block) && againstFace==BlockFace.UP ) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot put blocks right above the Vault!!");
        } else if (isVault(block)) {
            if (againstFace == BlockFace.NORTH || againstFace == BlockFace.SOUTH || againstFace == BlockFace.EAST || againstFace == BlockFace.WEST) {
                return;
            }
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "This area is protected by the Vault owned by another player!");
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.blockList()==null||event.blockList().isEmpty()){
            return;
        }
        for (Block block : event.blockList()) {
            if (!isVault(block)) {
                continue;
            }
            if (block.getState() instanceof Sign sign){
                event.blockList().remove(block);
            }
            else if (block.getState() instanceof Container container){
                PDC pdc = PDC.get(container);

                if (pdc.hasSecondChest()){
                    event.blockList().remove(pdc.getRight().getBlock());
                }
                event.blockList().remove(pdc.getLeft().getBlock());
                ResourceVaults.log("Explosion was going to effect a vault known as " + pdc.getVaultIndex() + " of " + Bukkit.getPlayer(pdc.getOwner()).getName());

            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getBlock();
        if (isVault(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isVault(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isVault(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    public static boolean isVault(Block block) {
        if (!(block.getState() instanceof Container container)){
            if (block.getState() instanceof Sign sign){
                if (sign.getBlockData() instanceof WallSign directional){
                    Block backSide = block.getRelative(directional.getFacing().getOppositeFace());

                    if (backSide.getState() instanceof Container container){
                        PDC pdc = PDC.get(container);
                        return pdc.hasKeys();
                    }
                }
            }
            return false;
        }
        PDC pdc = PDC.get(container);
        return pdc.hasKeys();
    }
}
