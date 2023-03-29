package com.thepaperraven.ai.gui;

import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultInstance;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VaultsInventoryGUI implements InventoryHolder, Listener {

    @Getter
    private final Player player;
    @Getter
    private final List<VaultInstance> vaults;
    @Getter
    private final int pageSize = 45;
    @Getter
    private final int lastPageIndex;
    @Getter
    private int currentPageIndex = 0;

    public VaultsInventoryGUI(Player player) {
        this.player = player;
        this.vaults = PlayerData.get(player.getUniqueId()).getVaults().values().stream().toList();
        this.lastPageIndex = (int) Math.ceil((double) vaults.size() / pageSize) - 1;
    }

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, "Your Vaults");

        // Add vault items to inventory
        for (int i = currentPageIndex * pageSize; i < (currentPageIndex + 1) * pageSize && i < vaults.size(); i++) {
            Vault vault = ((Vault) vaults.get(i));
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("ID: " + vault.getMetadata().getVaultIndex());
            lore.add("Location: " + vault.getChestLocations().get(0));
            boolean doubleChest = false;
            Location second = vault.getChestLocations().get(1);
            if (second != null){
                lore.add("Second Chest: " + vault.getChestLocations().get(1));

                doubleChest = true;
            }
            lore.add("Locked: " + (vault.isLocked() ? "Yes" : "No"));
            lore.add(vault.getMetadata().getAllowedMaterial().getKey().getKey());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        // Add pagination buttons to last row
        ItemStack prevPageItem = new ItemStack(Material.ARROW);
        ItemMeta prevPageMeta = prevPageItem.getItemMeta();
        //Add custom model data!;

        prevPageMeta.setDisplayName("Previous Page");
        prevPageItem.setItemMeta(prevPageMeta);
        ItemStack currPageItem = new ItemStack(Material.MAP);
        ItemMeta currPageMeta = currPageItem.getItemMeta();
        //Add custom model data!;
        currPageMeta.setDisplayName("Page " + (currentPageIndex + 1) + " of " + (lastPageIndex + 1));
        currPageItem.setItemMeta(currPageMeta);
        ItemStack nextPageItem = new ItemStack(Material.ARROW);
        ItemMeta nextPageMeta = nextPageItem.getItemMeta();
        //Add custom model data!;

        nextPageMeta.setDisplayName("Next Page");
        nextPageItem.setItemMeta(nextPageMeta);
        inv.setItem(48, currentPageIndex == 0 ? null : prevPageItem);
        inv.setItem(49, currPageItem);
        inv.setItem(50, currentPageIndex == lastPageIndex ? null : nextPageItem);

        return inv;
    }

    @EventHandler(priority = EventPriority.LOW,ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getView().getTopInventory().getHolder() instanceof VaultsInventoryGUI holder) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            int slot = event.getSlot();


            // Handle pagination buttons
            if (slot == holder.getLastPageButtonSlot()) {
                if (holder.getCurrentPage() > 1) {
                    holder.setCurrentPage(holder.getCurrentPage() - 1);
                    player.openInventory(holder.getInventory());
                }
                return;
            } else if (slot == holder.getCurrentPageButtonSlot()) {
                return;
            } else if (slot == holder.getNextPageButtonSlot()) {
                if (holder.getCurrentPage() < holder.getTotalPages()) {
                    holder.setCurrentPage(holder.getCurrentPage() + 1);
                    player.openInventory(holder.getInventory());
                }
                return;
            }

            // Calculate the index of the clicked vault
            int index = (holder.getCurrentPage() - 1) * holder.getSlotsPerPage() + slot;

            // Check if the index is valid
            if (index >= 0 && index < holder.getVaults().size()) {
                // Handle clicking on a vault
                Vault vault = (Vault) holder.getVaults().get(index);
                if (vault.isLocked()) {
                    player.sendMessage("This vault is locked!");
                    return;
                }

                // Teleport the player to the vault's location
                player.teleport(vault.getSignLocation().toBlockLocation());

                // Play a sound effect
                player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f);

                // Close the GUI
                player.closeInventory();
            }
        }
    }
    public int getLastPageButtonSlot() {
        return 46;
    }

    public int getCurrentPageButtonSlot(){
        return 50;
    }
    public int getCurrentPage() {
        return currentPageIndex;
    }

    public int getTotalPages() {
        return lastPageIndex + 1;
    }

    public int getSlotsPerPage() {
        return pageSize;
    }

    public int getNextPageButtonSlot() {
        return 54;
    }

    public void setCurrentPage(int page) {
        currentPageIndex = page;
    }

}
