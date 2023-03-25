package com.thepaperraven.ai.listeners;

import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultKeys;
import com.thepaperraven.ai.VaultManager;
import com.thepaperraven.ai.events.VaultCreateEvent;
import com.thepaperraven.config.resources.Resource;
import io.lumine.mythic.lib.api.util.EnumUtils;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static com.thepaperraven.ai.VaultManager.findAttachedChest;

public class VaultListener implements Listener {

    public VaultListener() {

    }
    @EventHandler
    public void onCreateVault(VaultCreateEvent e){
        ResourceVaults.log("Vault Create From Event for " + e.getPlayer().getName());
        ResourceVaults.log(e.getVault().toString());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String[] lines = event.getLines();
        String firstLine = ChatColor.stripColor(lines[0]);

        if (firstLine.equalsIgnoreCase("[Resources]") &&
                (player.hasPermission("RS.Create") || player.isOp())) {

            InventoryHolder holder = null;

            if (block.getState() instanceof Chest chest) {
                holder = chest;
            } else if (block.getState() instanceof DoubleChest doubleChest) {
                holder = doubleChest;
            }

            if (holder != null) {
                UUID ownerId = player.getUniqueId();
                Resource resource = Resource.WHEAT;

                if (!lines[1].isEmpty()) {
                    Resource lineResource = EnumUtils.getIfPresent(Resource.class, lines[1]).orElse(null);
                    if (lineResource != null) {
                        resource = lineResource;
                    }
                }

                Block attachedBlock = findAttachedChest(block);
                if (attachedBlock != null && attachedBlock.getState() instanceof Chest chest) {

                    if (vaultManager.getVault(chest.getBlock()) == null) {
                        int index = 1;
                        Vault v = Vault.fromBlock(chest.getBlock());

                        if (v != null) {
                            ResourceVaults.log("Vault exists currently! Failure?");
                            return;
                        }

                        if (resource == Resource.NONE) {
                            throw new RuntimeException("Error: MATERIAL RESOURCE ISSUE!");
                        }

                        v = new Vault(index, ownerId, chest.getLocation(), resource);

                        VaultCreateEvent vaultCreateEvent = new VaultCreateEvent(v);
                        Bukkit.getPluginManager().callEvent(vaultCreateEvent);

                        if (vaultCreateEvent.isCancelled()) {
                            event.setCancelled(true);
                            ResourceVaults.log("Event of creation was cancel!");
                            return;
                        }

                        vaultManager.addVault(player.getUniqueId(), v);

                        event.setLine(0, ChatColor.GREEN + firstLine);
                        player.sendMessage(ChatColor.GREEN + "Vault created!");
                    }
                }

            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null){
            return;
        }
        if (!VaultManager.isVault(clickedBlock)) {
            return;
        }
        ResourceVaults.log("Locate Vault: TRUE on INTERACT EVENT");
        Vault vault = vaultManager.getVault(clickedBlock);
        if (!vault.getOwnerId().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "This Resource Storage is not yours!");
            return;
        }
        Material materialType = vault.getMaterialType();
        ItemStack itemInHand = event.getItem();
        if (itemInHand != null && itemInHand.getType() != materialType) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can only store " + materialType + " in this Resource Storage!");
            return;
        }
        Bukkit.getScheduler().runTaskLater(ResourceVaults.getPlugin(), () -> {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            player.openInventory(vault.getChest().getBlockInventory());
            ResourceVaults.log("Opening Vault for " + player.getName() +" [" + + vault.getIndex() + " - " + vault.getLocation().toBlockLocation() + " ]");
            }, 1L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!(block.getState() instanceof Chest chest)) {
            return;
        }
        if (!VaultManager.isVault(chest.getBlock())) {
            return;
        }
        Vault vault = vaultManager.getVault(chest.getLocation().getBlock());
        if (!vault.getOwnerId().equals(player.getUniqueId()) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "This Resource Storage is not yours!");
            return;
        }
        if (vaultManager.removeVault(player.getUniqueId(), vault)) {
            chest.getPersistentDataContainer().remove(VaultKeys.getVaultKey());
            chest.getPersistentDataContainer().remove(VaultKeys.getLocationKey());
            chest.getPersistentDataContainer().remove(VaultKeys.getMaterialTypeKey());
            chest.getPersistentDataContainer().remove(VaultKeys.getDoubleChestLocationKey());
            chest.getPersistentDataContainer().remove(VaultKeys.getIndexKey());
            chest.getPersistentDataContainer().remove(VaultKeys.getOwnerKey());
            player.sendMessage(ChatColor.GREEN + "Resource Vault Removed!");
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getDestination().getType() == InventoryType.CHEST) {
            Chest chest = (Chest) event.getDestination().getHolder();
            Vault vault = vaultManager.getVault(chest.getLocation().clone().add(0, -1, 0).getBlock());
            if (vault != null && event.getItem().getType() != vault.getMaterialType()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getCurrentItem();

        if (event.getClickedInventory() == null){
            return;
        }
        if (event.getClickedInventory().getType() != InventoryType.PLAYER){
            ResourceVaults.log("Not a Player Inventory Click");
            return;
        }
        // Check if the clicked inventory is a Vault
        if (event.getInventory().getType() == InventoryType.CHEST && event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder && Vault.fromBlock(blockInventoryHolder.getBlock())!=null){
            Vault vault = Vault.fromBlock(blockInventoryHolder.getBlock());
            if (!vault.isOwner(((Player) event.getWhoClicked()).getPlayer())){
                event.setCancelled(true);
                //Check result
                event.getWhoClicked().sendMessage("Not owner of this vault!");
                return;
            }
            if (item.getType()!= vault.getMaterialType()){
                event.setCancelled(true);
            }
        }
        if (event.getInventory().getHolder() instanceof Vault vault) {

            // Check if the item being moved matches the Vault's material type
            if (item != null && item.getType() != vault.getMaterialType()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only store " + vault.getMaterialType() + " in this Vault.");
                return;
            }

            // Check for shift-click behavior
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                Inventory otherInv = event.getClickedInventory() == player.getInventory() ? event.getView().getTopInventory() : player.getInventory();

                for (ItemStack otherItem : otherInv.getContents()) {
                    if (otherItem != null && otherItem.getType() != vault.getMaterialType()) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You can only store " + vault.getMaterialType() + " in this Vault.");
                        return;
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            if (VaultManager.isVault(block)) {
                Vault vault = Vault.fromBlock(block);

                if (vault == null){
                    continue;
                }

                if (vault.isActive()) {
                    event.blockList().remove(block);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        Player player = e.getPlayer();

        if (!ResourceVaults.getVaultManager().getVaults(player.getUniqueId()).isEmpty()) {
            ResourceVaults.getVaultManager().saveVaultsToFile(player.getUniqueId());
            ResourceVaults.log("Saving Vaults to File of " + player.getName());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        try {
            ResourceVaults.getVaultManager().loadVaultsFromFile(e.getPlayer().getUniqueId());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
    public void onSwing(PlayerArmSwingEvent e){
        Block block = e.getPlayer().getTargetBlock(10);
        if (block == null){
            return;
        }
        if (!VaultManager.isVault(block)){
            return;
        }

        Vault vault = Vault.fromBlock(block);

        if (vault == null){
            return;
        }

        if (!vault.isOwner(e.getPlayer())) {
            return;
        }

        vault.open();
    }

}
