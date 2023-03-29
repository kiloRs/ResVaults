package com.thepaperraven.listeners;

import com.thepaperraven.ai.Vault;
import com.thepaperraven.utils.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class VaultBlockListener implements Listener {


    public VaultBlockListener(Plugin plugin) {
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        Block chestBlock = placedBlock.getRelative(BlockFace.DOWN);

        UUID owner = VaultUtil.getOwner(chestBlock);
        Vault vault = (Vault) VaultUtil.getVault(Bukkit.getPlayer(owner), VaultUtil.getIndex(chestBlock));
        if (VaultUtil.isVault(chestBlock.getLocation()) && vault != null && vault.getBlocks().contains(chestBlock)) {
            Player player = event.getPlayer();
            Material placedMaterial = placedBlock.getType();

            if (placedMaterial == Material.CHEST || placedMaterial == Material.TRAPPED_CHEST) {
                // Prevent placing chests directly above the Vault's chest
                event.setCancelled(true);
                player.sendMessage("You can't place a chest on top of this Vault's chest!");
            } else if (placedMaterial == Material.HOPPER) {
                // Prevent placing hoppers directly above the Vault's chest
                event.setCancelled(true);
                player.sendMessage("You can't place a hopper on top of this Vault's chest!");
            } else if (placedMaterial == Material.DISPENSER || placedMaterial == Material.DROPPER) {
                // Prevent placing dispensers or droppers directly above the Vault's chest
                event.setCancelled(true);
                player.sendMessage("You can't place a dispenser or dropper on top of this Vault's chest!");
            }
            else if (placedMaterial.createBlockData() instanceof Slab slab && slab.getType() != Slab.Type.DOUBLE){
                return;
            }
            else {
                if (placedMaterial.isSolid()) {
                    event.setCancelled(true);
                    player.sendMessage("You can't place a solid block on top of this Vault's chest!");

                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(BlockDamageEvent event) {
        Block block = event.getBlock();
        UUID ownerUUID = VaultUtil.getOwner(block);
        if (block.getState() instanceof Sign sign) {
            String[] lines = sign.getLines();

            if (lines.length > 0 && lines[0].equals("[Resources]")) {
                Player player = event.getPlayer();
                if (VaultUtil.isVault(block.getLocation())) {
                    Vault vault = ((Vault) VaultUtil.getVault(Bukkit.getPlayer(ownerUUID), VaultUtil.getIndex(block)));

                    if (vault == null || !vault.getMetadata().isOwner(player)){
                        return;
                    }
                    event.setCancelled(true);
                }
            }
        }
        else if (block.getState() instanceof Chest chest){
            if (VaultUtil.isVault(chest.getBlock())){
                Vault vault = ((Vault) VaultUtil.getVault(Bukkit.getPlayer(ownerUUID), VaultUtil.getIndex(block)));

                if (vault != null && !vault.getMetadata().isOwner(event.getPlayer())){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();
        blocks.removeIf(block -> VaultUtil.isVault(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (VaultUtil.isVault(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (VaultUtil.isVault(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
