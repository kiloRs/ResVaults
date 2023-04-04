package com.thepaperraven.commands;

import com.thepaperraven.data.player.PlayerDataMathHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RVWithdrawCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rvwithdraw <material> <amount>");
            return true;
        }
        Material material;
        try {
            material = Material.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid material.");
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
        if (!PlayerDataMathHandler.withdraw(player, material, amount)) {
            player.sendMessage(ChatColor.RED + "You do not have enough of that item in your vaults.");
            return true;
        }
        player.sendMessage(ChatColor.GREEN + "Withdrew " + amount + " " + material.name().toLowerCase() + ".");
        return true;
    }
}
