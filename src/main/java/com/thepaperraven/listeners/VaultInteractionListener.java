package com.thepaperraven.listeners;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.ai.vault.VaultPDContainer;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

import static com.thepaperraven.ai.vault.VaultPDContainer.getVaultContainerByBlock;

public class VaultInteractionListener implements Listener {

    private final Plugin plugin;

    public VaultInteractionListener(Plugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Container container) {
            VaultPDContainer vaultContainer = getVaultContainerByBlock(player,container.getBlock(), !player.isOp());
            if (vaultContainer != null && vaultContainer.hasKeys() && vaultContainer.isValid()) {
                UUID owner = vaultContainer.getOwner();
                int index = vaultContainer.getVaultIndex();
                if (!owner.equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot access this vault as it belongs to another player.");
                    return;
                }
                player.sendMessage(ChatColor.AQUA + "Accessing Vault: " + index);
                event.setCancelled(true);
                PlayerData pData = PlayerData.get(player.getUniqueId());
                VaultInstance vault = pData.getVault(index);

                if (vault.isValid()) {
                    vault.getInventory().open();
                }
                else {
                    ResourceVaults.error("Invalid Vault Instance: Not Valid on Open");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof VaultInventory vaultInventory) {
            VaultPDContainer vaultContainer = vaultInventory.getContainer();
            if (vaultContainer != null && vaultContainer.hasOwner() && vaultContainer.hasVaultIndex() && vaultContainer.hasMaterialKey()) {
                UUID owner = vaultContainer.getOwner();
                int index = vaultContainer.getVaultIndex();
                if (!owner.equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot interact with this vault as it belongs to another player.");
                }
                if (event.getCurrentItem() != null){
                    if (event.isShiftClick()){
                        if (!(event.getClickedInventory().getHolder() instanceof VaultInstance v)){
                            if (event.getCurrentItem().getType()!=vaultContainer.getMaterialKey()){
                                event.setCancelled(true);
                            }
                        }
                    }
                    else {
                        if (event.getView().getTopInventory().getHolder() instanceof VaultInventory v){
                            if (event.getCursor() != null) {
                                if (v.getContainer().getMaterialKey() != event.getCursor().getType()){
                                    event.setCancelled(true);
                                }
                            }
                            if (event.getCurrentItem() != null){
                                if (v.getContainer().getMaterialKey() != event.getCursor().getType()){
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                }
                if (event.getClick()== ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT){
                    if (!(event.getClickedInventory().getHolder() instanceof VaultInstance v)){
                        if (event.getCurrentItem().getType()!=vaultContainer.getMaterialKey()){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            BlockState state = clickedBlock.getState();
            if (state instanceof InventoryHolder holder) {
                VaultPDContainer vaultContainer = getVaultContainerByBlock(player,clickedBlock,!player.isOp());
                if (vaultContainer != null && vaultContainer.hasKeys()) {
                    UUID owner = vaultContainer.getOwner();
                    int index = vaultContainer.getVaultIndex();
                    if (!owner.equals(player.getUniqueId())) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You cannot interact with this vault as it belongs to another player.");
                        return;
                    }
                    PlayerData.get(owner).getVault(index).getInventory().open();
                    player.sendMessage(ChatColor.AQUA + "Accessing Vault: " + index);
                    return;
                }
            }
        }
    }

}
