package com.thepaperraven.listeners;

import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultPDContainer;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

import static com.thepaperraven.ai.vault.VaultPDContainer.getVaultContainerByBlock;

public class VaultBreakListener implements Listener {



    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Block clickedBlock = event.getBlock();
            BlockState state = clickedBlock.getState();
            if (state instanceof InventoryHolder holder) {
                remove(event, player, clickedBlock,true);
            }
            else if (state instanceof Sign sign && sign.getBlockData() instanceof WallSign wallSign){
                Block other = sign.getBlock().getRelative(wallSign.getFacing().getOppositeFace());
                if (other.getState() instanceof InventoryHolder inventoryHolder){
                    remove(event, player, other,false);
                }
    }
    }

    private static void remove(BlockBreakEvent event, Player player, Block clickedBlock,boolean invalidate) {
        VaultPDContainer vaultContainer = getVaultContainerByBlock(player, clickedBlock,!player.isOp());
        if (vaultContainer != null && vaultContainer.hasKeys()) {
            UUID owner = vaultContainer.getOwner();
            int index = vaultContainer.getVaultIndex();
            if (!owner.equals(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot interact with this vault as it belongs to another player.");
                return;
            }
            player.sendMessage(ChatColor.AQUA + "Breaking Vault: " + index);
            PlayerData.get(player.getUniqueId()).removeVault(index,invalidate);
            return;

    }
    }
}
