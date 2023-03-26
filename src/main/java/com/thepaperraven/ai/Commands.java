package com.thepaperraven.ai;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands implements CommandExecutor {

    private ResourceVaults plugin;

    public Commands(ResourceVaults plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /vault index | deposit <index> <amount> | withdraw <index> <amount> [force]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "index":
                int index = VaultUtil.getVaultIndex(player);
                if (index > 0) {
                    player.sendMessage(ChatColor.GREEN + "Vault Index: " + index);
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have a vault nearby.");
                }
                break;
            case "deposit":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /vault deposit <index> <amount>");
                    return true;
                }
                int depositIndex;
                int depositAmount;
                try {
                    depositIndex = Integer.parseInt(args[1]);
                    depositAmount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid index or amount.");
                    return true;
                }
                if (depositAmount < 1) {
                    player.sendMessage(ChatColor.RED + "You must deposit at least one item.");
                    return true;
                }
                Vault depositVault = VaultUtil.getVault(player, depositIndex);
                if (depositVault == null) {
                    player.sendMessage(ChatColor.RED + "Vault not found.");
                    return true;
                }
                ItemStack depositItem = new ItemStack(depositVault.getMaterial(), depositAmount);
                if (!depositVault.isValidMaterial(depositItem)) {
                    player.sendMessage(ChatColor.RED + "You can only deposit " + depositVault.getMaterial().toString().toLowerCase() + " in this vault.");
                    return true;
                }
                if (player.getInventory().containsAtLeast(depositItem, depositAmount)) {
                    player.getInventory().removeItem(depositItem);
                    depositVault.deposit(depositAmount);
                    player.sendMessage(ChatColor.GREEN + "Deposited " + depositAmount + " " + depositVault.getMaterial().toString().toLowerCase() + " into Vault #" + depositIndex + ".");
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have enough " + depositVault.getMaterial().toString().toLowerCase() + " to deposit into Vault #" + depositIndex + ".");
                }
                break;
            case "withdraw":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /vault withdraw <index> <amount> [true/false]");
                    return true;
                }
                int withdrawIndex;
                int withdrawAmount;
                boolean forceWithdraw = false;
                try {
                    withdrawIndex = Integer.parseInt(args[1]);
                    withdrawAmount = Integer.parseInt(args[2]);
                    if (args.length == 4) {
                        forceWithdraw = Boolean.parseBoolean(args[3]);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid index or amount entered.");
                    return true;
                }
                Player playerWithdraw = (Player) sender;
                PlayerData playerWithdrawData = plugin.getPlayerData(playerWithdraw);
                if (playerWithdrawData == null) {
                    sender.sendMessage(ChatColor.RED + "Unable to get player data.");
                    return true;
                }
                Vault withdrawVault = playerWithdrawData.getVault(withdrawIndex);
                if (withdrawVault == null) {
                    sender.sendMessage(ChatColor.RED + "Vault with index " + withdrawIndex + " not found.");
                    return true;
                }
                Material withdrawMaterial = withdrawVault.getMaterial();
                if (withdrawMaterial == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid material for vault with index " + withdrawIndex + ".");
                    return true;
                }
                int itemsInVault = withdrawVault.countItems();
                if (itemsInVault == 0) {
                    sender.sendMessage(ChatColor.RED + "Vault with index " + withdrawIndex + " is empty.");
                    return true;
                }
                int itemsToWithdraw = Math.min(withdrawAmount, itemsInVault);
                int withdrawnAmount = withdrawVault.withdraw(withdrawMaterial, itemsToWithdraw);
                if (withdrawnAmount == 0) {
                    sender.sendMessage(ChatColor.RED + "Vault with index " + withdrawIndex + " does not contain enough items.");
                    return true;
                }
                if (forceWithdraw) {
                    sender.sendMessage(ChatColor.GREEN + "Successfully withdrew " + withdrawnAmount + " items from vault with index " + withdrawIndex + ".");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Successfully withdrew " + withdrawnAmount + " items from vault with index " + withdrawIndex + ". (Vault contains " + (itemsInVault - withdrawnAmount) + " items)");
                }
                return true;
        }
        return false;
    }
}