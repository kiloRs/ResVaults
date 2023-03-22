package com.thepaperraven.ai.gui;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.thepaperraven.ai.ResourceVaults;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.ai.VaultKeys;
import com.thepaperraven.ai.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VaultGUI implements InventoryHolder, Listener {

    private final Player player;
    private final int page;
    private final Inventory inventory;
    private final List<Vault> playerVaults;

    private static final int PAGE_SIZE = 54;

    /**
     * @param player Player to open Vault GUI!
     *               Opens the VaultGUI as page 1 by default!
     */
    public VaultGUI(Player player){
        this(player,1);
    }
    public VaultGUI(Player player, int page) {
        this.player = player;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, PAGE_SIZE, "Vaults (Page " + page + ")");

        // Get all the player's vaults and sort them by index
        this.playerVaults = ResourceVaults.getVaultManager().getVaults(player.getUniqueId());
        this.playerVaults.sort((v1, v2) -> Integer.compare(v1.getIndex(), v2.getIndex()));

        // Calculate the range of vaults to display on this page
        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, playerVaults.size());

        // Add each vault as an itemstack to the inventory
        for (int i = startIndex; i < endIndex; i++) {
            Vault vault = playerVaults.get(i);
            VaultIcon vaultItem = VaultIcon.get(vault);

            // Add the vault PDC to the itemstack's metadata
            PersistentDataContainer pdc = vaultItem.getItemStack().getItemMeta().getPersistentDataContainer();
            pdc.set(VaultKeys.getIndexKey(), PersistentDataType.INTEGER, vault.getIndex());
            pdc.set(VaultKeys.getLocationKey(), DataType.LOCATION, vault.getLocation());

            inventory.setItem(vaultItem.getVault().getIndex() % PAGE_SIZE, vaultItem.getItemStack());
        }

        // Add pagination buttons if there are multiple pages
        if (playerVaults.size() > PAGE_SIZE) {
            // Add a "Previous Page" button if this isn't the first page
            if (page > 1) {
                ItemStack previousPageItem = new ItemStack(Material.ARROW);
                ItemMeta m = previousPageItem.getItemMeta();
                m.getPersistentDataContainer().set(VaultKeys.getBackKey(),DataType.BOOLEAN,true);
                m.setDisplayName(ChatColor.WHITE+"Previous Page");
                previousPageItem.setItemMeta(m);

                inventory.setItem(PAGE_SIZE - 9, previousPageItem);
            }

            // Add a "Next Page" button if there are more pages
            if (endIndex < playerVaults.size()) {
                ItemStack nextPageItem = new ItemStack(Material.ARROW);
                ItemMeta m = nextPageItem.getItemMeta();
                m.getPersistentDataContainer().set(VaultKeys.getNextKey(),DataType.BOOLEAN,true);
                m.setDisplayName(ChatColor.WHITE+"Next Page");
                nextPageItem.setItemMeta(m);
                inventory.setItem(PAGE_SIZE - 1, nextPageItem);
            }
        }
    }

    public void open(boolean log) {
        player.openInventory(inventory);
        if (log) {
            ResourceVaults.log("Opening VaultGUI Inventory!");
        }}

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicked inventory is the VaultGUI and cancel the event to prevent item moving
        if (event.getInventory().getHolder() instanceof VaultGUI) {
            event.setCancelled(true);

            // Get the clicked item's PDC and open the corresponding vault
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.CHEST) {
                PersistentDataContainer pdc = clickedItem.getItemMeta().getPersistentDataContainer();
                int vaultId = pdc.getOrDefault(VaultKeys.getIndexKey(), PersistentDataType.INTEGER,event.getSlot() + 1);

                Vault vault = VaultManager.getVaultById(vaultId,player);
                if (vault != null) {
                    vault.open();
                }
            }

            // Check for page buttons
            if (event.getCurrentItem() != null && (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(VaultKeys.getBackKey()) || event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(VaultKeys.getNextKey()))) {
                int currentPage = Integer.parseInt(ChatColor.stripColor(event.getInventory().getItem(49).getItemMeta().getDisplayName()).replace("Page ", ""));
                int numPages = (int) Math.ceil((double) playerVaults.size() / 45);
                int nextPage = currentPage;
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(VaultKeys.getNextKey()) && currentPage < numPages) {
                    nextPage++;
                } else if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(VaultKeys.getBackKey()) && currentPage > 1) {
                    nextPage--;
                } else {
                    return;
                }
                openPage(nextPage, ((Player) event.getWhoClicked()));
            }
        }
    }
    public static void openPage(int page, Player player) {
        VaultGUI newGUI = new VaultGUI(player, page);
        newGUI.open(true);
    }
}