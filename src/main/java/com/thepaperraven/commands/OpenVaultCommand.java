package com.thepaperraven.commands;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.gui.VaultInventory;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class OpenVaultCommand {
    private final ResourceVaults plugin;

    public OpenVaultCommand(ResourceVaults plugin) {
        this.plugin = plugin;
    }


    public boolean onCommand(@NotNull CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by a player!");
            return true;
        }

        if (args.length <= 1) {
            player.sendMessage("Usage: /rv open <index>");
            return true;
        }

        if (!args[0].equalsIgnoreCase("open")){
            player.sendMessage("Missing Open?");
            return true;
        }

        int index = 1;
        try {
            index = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("The specified index is not a valid number!");
            return true;
        }

        PlayerData playerData = PlayerData.get(player.getUniqueId());
        if (index <= 0 || index > playerData.getVaults().size()) {
            player.sendMessage("The specified index is out of bounds!");
            return true;
        }

        if (!playerData.hasVault(index)){
            player.sendMessage("You do not have this index of Vault in your Map!");
            return true;
        }
        VaultInstance vaultInstance = playerData.getVault(index);
        if (vaultInstance == null) {
            player.sendMessage("No vault found at the specified index!");
            return true;
        }

        VaultInventory vaultInventory = vaultInstance.getInventory();
        if (vaultInventory == null) {
            player.sendMessage("Failed to open vault inventory!");
            return true;
        }

        Inventory inventory = vaultInventory.getInventory();
        player.openInventory(inventory);

        return true;
    }
}