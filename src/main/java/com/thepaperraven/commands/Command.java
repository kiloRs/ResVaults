package com.thepaperraven.commands;

import com.thepaperraven.ResourceVaults;
import com.thepaperraven.ai.player.PlayerData;
import com.thepaperraven.ai.vault.VaultInstance;
import com.thepaperraven.ai.vault.VaultPDContainer;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
        } else if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            return new com.thepaperraven.commands.OpenVaultCommand(((ResourceVaults) ResourceVaults.getPlugin())).onCommand(sender, command, label, args);
        } else if (args[0].equalsIgnoreCase("create") && Arrays.stream(ResourceVaults.validMaterials).toList().contains(Material.matchMaterial(args[1])) || args.length==1 && args[0].equalsIgnoreCase("create")) {
            if (sender instanceof Player player) {
                return new CreateVaultCommand().onCommand(player, command, label, args);
            }
        } else if (args[0].equalsIgnoreCase("delete") && NumberUtils.isDigits(args[1])) {
            if (sender instanceof Player player) {
                return new DeleteVaultCommand().onCommand(player, command, label, args);
            }
        } else {
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


                    VaultInstance vault = getVaultOf(block);

                    if (vault == null) {
                        sender.sendMessage("This is not a Vault.");
                        return true;
                    }

                    if (!vault.getMetadata().getOwnerUUID().equals(((Player) sender).getUniqueId()) && !sender.isOp()) {
                        sender.sendMessage("You cannot view the index of someone else's Vault.");
                        return true;
                    }

                    sender.sendMessage("This is Vault #" + vault.getContainer().getVaultIndex() + ".");
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

                    if (indexToAdd == 0 || amountToAdd == 0) {
                        sender.sendMessage("Invalid Index or Amount! Bad Arguments!");
                        return false;
                    }
                    VaultInstance vaultToAdd = PlayerData.get(playerToAdd.getUniqueId()).getVault(indexToAdd);
                    if (vaultToAdd == null) {
                        sender.sendMessage(playerToAdd.getName() + " does not have a Vault at index #" + indexToAdd + ".");
                        return true;
                    }

                    vaultToAdd.getInventory().add(amountToAdd);
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
                    VaultInstance vaultToRemoveFrom = playerDataToRemoveFrom.getVault(vaultIndex);
                    if (vaultToRemoveFrom == null) {
                        sender.sendMessage(ChatColor.RED + "Vault not found.");
                        return true;
                    }
                    boolean valid = false;
                    Player player = null;

                    if (!sender.isOp() && !(sender instanceof Player p)) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to remove items from this vault.");
                        return true;
                    } else if (sender instanceof Player p) {
                        player = p;
                        if (player.getUniqueId().equals(playerDataToRemoveFrom.getUuid())) {
                            valid = true;
                        }
                    } else if (sender.isOp()) {
                        valid = true;
                    }
                    if (valid) {
                        if (vaultIndex > 0) {
                            if (playerDataToRemoveFrom.hasVault(vaultIndex)) {
                                playerDataToRemoveFrom.getVault(vaultIndex).getInventory().remove(amountToRemove);
                            }
                            sender.sendMessage(ChatColor.GREEN + String.format("Removed %d items from vault at index %d for player %s.", amountToRemove, vaultIndex, playerToRemoveFrom.getName()));
                            return true;
                        } else {
                            sender.sendMessage("Invalid Vault Index: " + vaultIndex);
                        }
                    } else {
                        sender.sendMessage("Error: This vault is invalid or not owned by you!");

                    }
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
            return false;
        }
        return false;
    }
    private static VaultInstance getVaultOf(Block block) {
        if (block.getState() instanceof InventoryHolder holder){
            VaultPDContainer pdc = new VaultPDContainer(holder);
            if (pdc.hasKeys()){
                UUID owner = pdc.getOwner();
                int index = pdc.getVaultIndex();
                return PlayerData.get(owner).getVault(index);
            }
        }
        return null;
    }
}
