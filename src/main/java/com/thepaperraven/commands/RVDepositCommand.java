package com.thepaperraven.commands;

import com.thepaperraven.data.player.PlayerDataMathHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RVDepositCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rvdeposit <material> <amount>");
            return true;
        }
        Material material = null;
        try {
            material = Material.matchMaterial(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid material.");
            return true;
        }
        if (material == null){
            player.sendMessage("No Material Found by Name [" + args[0] + "]!");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount.");
            return true;
        }
        if (amount <= 0) {
            player.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
            return true;
        }
        if (!PlayerDataMathHandler.deposit(player, material, amount)) {
            player.sendMessage(ChatColor.RED + "You do not have enough of that item in your inventory or there is not enough space in your vaults.");
            return true;
        }
        player.sendMessage(ChatColor.GREEN + "Deposited " + amount + " " + material.name().toLowerCase() + ".");
        return true;
    }
}

