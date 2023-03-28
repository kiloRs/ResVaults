package com.thepaperraven.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ResourceVaultAddCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /rv add <amount> <player> <indexOrMaterial>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount specified.");
            return true;
        }

        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player specified.");
            return true;
        }

        int vaultIndex;
        VaultInstance vaultInstance;
        String indexOrMaterial = args[2];
        if (StringUtils.isNumeric(indexOrMaterial)) {
            vaultIndex = Integer.parseInt(indexOrMaterial);
            PlayerData playerData = ResourceVaults.getInstance().getPlayerDataManager().getPlayerData(player);
            if (vaultIndex < 0 || vaultIndex >= playerData.getVaults().size()) {
                sender.sendMessage(ChatColor.RED + "Invalid vault index specified.");
                return true;
            }
            vaultInstance = playerData.getVaults().get(vaultIndex);
        } else {
            Material material = Material.matchMaterial(indexOrMaterial);
            if (material == null) {
                sender.sendMessage(ChatColor.RED + "Invalid material specified.");
                return true;
            }
            PlayerData playerData = ResourceVaults.getInstance().getPlayerDataManager().getPlayerData(player);
            Optional<VaultInstance> optionalVault = playerData.getVaults().stream()
                    .filter(vault -> vault.getMetadata().getAllowedMaterial() == material)
                    .findFirst();
            if (!optionalVault.isPresent()) {
                sender.sendMessage(ChatColor.RED + "No vaults found for specified material.");
                return true;
            }
            vaultInstance = optionalVault.get();
        }

        // Call the custom event to allow other plugins to modify the behavior of this command.
        VaultAddItemEvent event = new VaultAddItemEvent(player, vaultInstance, amount);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return true;
        }
        amount = event.getAmount(); // Allow other plugins to modify the amount being added

        // Add the items to the vault
        VaultInventory vaultInventory = vaultInstance.getInventory();
        int remainingAmount = vaultInventory.add(amount);

        // If there is remaining overflow, try to pass it on to another vault of the same material
        if (remainingAmount > 0) {
            VaultPassover passover = new VaultPassover(vaultInstance, player);
            remainingAmount = passover.passover(remainingAmount);

            // If there is still remaining overflow, drop it on the ground
            if (remainingAmount > 0) {
                Location dropLocation = vaultInstance.getChestLocation().clone().add(0.5, 1.5, 0.5);
                ItemStack itemStack = new ItemStack(vaultInstance.getMetadata().getAllowedMaterial(), remainingAmount);
                player.getWorld().dropItem(dropLocation, itemStack);
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Added " + (amount - remainingAmount) + " items to the vault.");
        return true;
    }
}
}
