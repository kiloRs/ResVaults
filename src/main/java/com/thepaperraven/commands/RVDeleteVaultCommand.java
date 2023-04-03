package com.thepaperraven.commands;

import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.Vault;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.thepaperraven.ResourceVaults.PREFIX;

public class RVDeleteVaultCommand implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(PREFIX + ChatColor.RED + "Please specify a vault index to delete. Usage: /rvdelete <index>");
            return true;
        }

        int index = -1;

        try {
            index = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(PREFIX + ChatColor.RED + "The vault index must be a number.");
            return true;
        }

        if (index<=0){
            player.sendMessage(PREFIX + ChatColor.RED + "Invalid Index Number of Vault.");
            return true;
        }
        Vault container = PlayerData.get(player.getUniqueId()).getVaults().get(index);

        if (container == null) {
            player.sendMessage(PREFIX + ChatColor.RED + "There is no vault with that index.");
            return true;
        }

        if (!container.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(PREFIX + ChatColor.RED + "You can only delete your own vaults.");
            return true;
        }

        PlayerData.get(player.getUniqueId()).removeVault(index);

        player.sendMessage(PREFIX + ChatColor.GREEN + "Vault deleted successfully!");

        return true;
    }

}
