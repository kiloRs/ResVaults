package com.thepaperraven.ai.gui;

import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.Vault;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class VaultsGUI implements InventoryHolder {

    private final Player player;
    private final Map<Integer, Vault> vaults;
    private final int totalPages;
    private final ItemStack nextPageIcon;
    private final ItemStack prevPageIcon;
    private final ItemStack currentPageIcon;
    private final String guiName;

    private int currentPage;

    public VaultsGUI(Player player) {
        this.player = player;
        this.vaults = PlayerData.get(player.getUniqueId()).getVaults();
        this.guiName = ChatColor.YELLOW + "Vaults";
        this.nextPageIcon = IconUtil.getIcon("next-page");
        this.prevPageIcon = IconUtil.getIcon("prev-page");
        this.currentPageIcon = IconUtil.getIcon("curr-page");

        this.totalPages = (int) Math.ceil((double) vaults.size() / 45);
        this.currentPage = 0;
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 54, guiName);

        int startIndex = currentPage * 45;
        int endIndex = Math.min(startIndex + 45, vaults.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = new ItemStack(vaults.get(i).getMaterial());
            if (item != null) {
                inv.setItem(slot, item);
            }
            slot++;
        }

        if (currentPage > 0) {
            inv.setItem(45, prevPageIcon);
        }

        inv.setItem(46, currentPageIcon);

        if (currentPage < totalPages - 1) {
            inv.setItem(47, nextPageIcon);
        }

        return inv;
    }

    public void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            player.openInventory(getInventory());
        }
    }

    public void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            player.openInventory(getInventory());
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
