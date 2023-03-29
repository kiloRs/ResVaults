package com.thepaperraven.commands;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.PlayerData;
import com.thepaperraven.ai.Vault;
import com.thepaperraven.utils.VaultUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Command implements CommandExecutor {

    public Command(){

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("Vault Command Help:");
            sender.sendMessage("/rv index - Shows the vault index of the Chest / Sign the command sender is looking at, if it is a Vault owned by that player, or if the player isOp.");
            sender.sendMessage("/rv add <amount> <player> <index> - Add this amount of items to the specified player's Vault at the given index.");
            sender.sendMessage("/rv remove <amount> <player> <index> - Remove this amount of items from the specified player's Vault at the given index.");
            sender.sendMessage("/rv delete <player> <index> - Break the blocks involved in this vault, if the person who issued the command isOP or is the owner of the Vault.");
            sender.sendMessage("/rv reload - Reload the plugin in entirety, and reload all the Vaults for all the players.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "index":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be used by players.");
                    return true;
                }

                Block block = ((Player) sender).getTargetBlockExact(5);
                if (block == null) {
                    sender.sendMessage("You must be looking at a block to use this command.");
                    return true;
                }

                Vault vault = null;

                if (block.getState() instanceof Sign sign || block.getState() instanceof Chest chest){
                    TileState state = (TileState) block.getState();
                    if (VaultUtil.hasIndex(state) && VaultUtil.hasOwner(state) && VaultUtil.hasMaterial(state)){
                        UUID owner = VaultUtil.getOwner(block);
                        int index = VaultUtil.getIndex(block);

                        vault = (Vault) VaultUtil.getVault(Bukkit.getPlayer(owner),index);
                    }
                }
                if (vault == null) {
                    sender.sendMessage("This is not a Vault.");
                    return true;
                }

                if (!vault.getMetadata().getOwnerUUID().equals(((Player) sender).getUniqueId()) && !sender.isOp()) {
                    sender.sendMessage("You cannot view the index of someone else's Vault.");
                    return true;
                }

                sender.sendMessage("This is Vault #" + vault.getMetadata().getVaultIndex() + ".");
                break;

            case "add":
                if (args.length < 4) {
                    sender.sendMessage("Usage: /rv add <amount> <player> <index>");
                    return true;
                }

                int amountToAdd;
                try {
                    amountToAdd = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid amount specified.");
                    return true;
                }

                Player playerToAdd = Bukkit.getPlayer(args[2]);
                if (playerToAdd == null) {
                    sender.sendMessage("Invalid player specified.");
                    return true;
                }

                int indexToAdd = 0;
                try {
                    indexToAdd = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid index specified.");
                    return true;
                }

                Vault vaultToAdd = (Vault) PlayerData.get(playerToAdd.getUniqueId()).getVault(indexToAdd);
                if (vaultToAdd == null) {
                    sender.sendMessage(playerToAdd.getName() + " does not have a Vault at index #" + indexToAdd + ".");
                    return true;
                }

                vaultToAdd.add(amountToAdd);
                sender.sendMessage(amountToAdd + " items added to " + playerToAdd.getName() + "'s Vault at index #" + indexToAdd + ".");
                break;
            case "remove":
                if (args.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Invalid command syntax. Correct usage: /rv remove <amount> <player> <index>");
                    return true;
                }
                int amountToRemove;
                try {
                    amountToRemove = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
                    return true;
                }
                Player playerToRemoveFrom = Bukkit.getPlayer(args[2]);
                if (playerToRemoveFrom == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                int vaultIndex;
                try {
                    vaultIndex = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid vault index specified.");
                    return true;
                }
                PlayerData playerDataToRemoveFrom = ResourceVaults.getPlayerData(playerToRemoveFrom.getUniqueId());
                Vault vaultToRemoveFrom = (Vault) playerDataToRemoveFrom.getVault(vaultIndex);
                if (vaultToRemoveFrom == null) {
                    sender.sendMessage(ChatColor.RED + "Vault not found.");
                    return true;
                }
                boolean valid = false;
                if (!sender.isOp() && !(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to remove items from this vault.");
                    return true;
                }
                else if (sender instanceof Player player){

                    if (player.getUniqueId().equals(playerDataToRemoveFrom.getUuid())){
                        valid = true;
                    }
                }
                if (valid) {
                    PlayerData playerData = ResourceVaults.getPlayerData(playerDataToRemoveFrom.getUuid());
                    if (vaultIndex > 0) {
                        playerData.removeVault(vaultIndex);
                        sender.sendMessage(ChatColor.GREEN + String.format("Removed %d items from vault at index %d for player %s.", amountToRemove, vaultIndex, playerToRemoveFrom.getName()));
                        return true;
                    } else {
                        sender.sendMessage("Invalid Vault Index: " + vaultIndex);
                    }
                }
                else {
                    sender.sendMessage("Error: This vault is invalid or not owned by you!");

                }
            case "delete":
                if (!sender.isOp()) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to delete this vault.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /rv delete <player> <index>");
                    return true;
                }
                String target = args[1];
                int index = 0;
                try {
                    index = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid index specified.");
                    return true;
                }
                if (index<= 0){
                    sender.sendMessage(ChatColor.RED + "Invalid Chest Index! Indexes Start with 1.");
                }
                PlayerData targetData = new PlayerData(Bukkit.getPlayer(target).getUniqueId());
                if (targetData.getVaults().remove(index) == null) {
                    sender.sendMessage(ChatColor.RED + "Vault not found for specified index.");
                    return true;
                }
                Location chest1 = ((Vault) targetData.getVaults().get(index)).getChestLocations().get(0);
                chest1.getBlock().setType(Material.AIR);
                Location chest2 = ((Vault) targetData.getVaults().get(index)).getChestLocations().get(1);
                if (chest2 != null) {
                    chest2.getBlock().setType(Material.AIR);
                }
                Location signLoc = ((Vault) targetData.getVaults().get(index)).getSignLocation();
                signLoc.getBlock().setType(Material.AIR);
                targetData.getConfig().saveToFile();
                sender.sendMessage(ChatColor.GREEN + "Vault deleted successfully.");
                return true;
            case "reload":
                if (!sender.isOp()) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to reload the plugin.");
                    return true;
                }
                ResourceVaults.reloadPlugin(true);
                sender.sendMessage(ChatColor.GREEN + "Plugin reloaded successfully.");
                return true;
            case "help":
            default:
                sender.sendMessage(ChatColor.GOLD + "/rv index - Shows the vault index of the Chest / Sign you're looking at, if it is a Vault owned by you or if you're OP.");
                sender.sendMessage(ChatColor.GOLD + "/rv add <amount> <player> <index> - Adds this amount of items to the specified vault.");
                sender.sendMessage(ChatColor.GOLD + "/rv remove <amount> <player> <index> - Removes this amount of items from the specified vault.");
                sender.sendMessage(ChatColor.GOLD + "/rv delete <player> <index> - Deletes the specified vault.");
                sender.sendMessage(ChatColor.GOLD + "/rv reload - Reloads the plugin and all vaults for all players.");
                return true;

        }
        return false;    }
}
